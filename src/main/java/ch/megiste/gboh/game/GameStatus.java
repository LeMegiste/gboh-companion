package ch.megiste.gboh.game;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.util.Strings;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.thoughtworks.xstream.XStream;

import ch.megiste.gboh.army.Army;
import ch.megiste.gboh.army.Unit;
import ch.megiste.gboh.army.Unit.MissileType;
import ch.megiste.gboh.army.Unit.SubClass;
import ch.megiste.gboh.army.Unit.UnitKind;
import ch.megiste.gboh.army.UnitStatus;
import ch.megiste.gboh.army.UnitStatus.MissileStatus;
import ch.megiste.gboh.army.UnitStatus.UnitState;
import ch.megiste.gboh.game.PersistableGameState.CommandHistory;
import ch.megiste.gboh.game.PersistableGameState.UnitChange;
import ch.megiste.gboh.util.GbohError;
import ch.megiste.gboh.util.Helper;

public class GameStatus {

	private Army army1;
	private Army army2;

	private Path currentDir;

	private String battleName;

	private PersistableGameState state = new PersistableGameState();

	private XStream xStream = null;
	private String commandText;
	private Properties generalProperties;

	public GameStatus() {
		xStream = new XStream();
		Class<?>[] classes =
				new Class[] { PersistableGameState.class, UnitStatus.class, UnitChange.class, CommandHistory.class };
		XStream.setupDefaultSecurity(xStream);
		xStream.allowTypes(classes);

		xStream.processAnnotations(classes);

	}

	public String getPromptString() {
		return battleName + " " + Helper.toRoman(state.currentTurn) + "." + state.currentCommand;
	}

	public void load(Path battleDir) {
		try {
			if (battleDir == null) {
				final String battleDirString = generalProperties.getProperty("currentBattle");
				if (Strings.isEmpty(battleDirString)) {
					return;
				} else {
					battleDir = Paths.get(battleDirString);
				}
			}
			final Stream<Path> children = Files.list(battleDir);
			final List<Path> armyFiles = children.filter(p -> p.getFileName().toString().startsWith("Army_")).sorted()
					.collect(Collectors.toList());
			if (armyFiles.size() != 2) {
				throw new GbohError("We need two army files in the battle directory.");
			}
			army1 = loadArmy(armyFiles.get(0));
			army2 = loadArmy(armyFiles.get(1));

			//Check armies consistency
			List<String> processedCodes = new ArrayList<>();
			boolean invalidArmies = false;
			List<String> duplicateCodes = new ArrayList<>();
			for (Unit u : getAllUnits()) {
				final String unitCode = u.getUnitCode();

				if (processedCodes.contains(unitCode)) {
					invalidArmies = true;
					if (!duplicateCodes.contains(unitCode)) {
						duplicateCodes.add(unitCode);
					}
				} else {
					processedCodes.add(unitCode);
				}
			}
			Collections.sort(duplicateCodes);
			if (invalidArmies) {
				throw new GbohError(
						"Invalid army. The following codes are duplicated." + Joiner.on(", ").join(duplicateCodes));
			}

			currentDir = battleDir;
			battleName = battleDir.getFileName().toString();

			Path gamePropsPath = getGamePropertiesPath();
			if (Files.exists(gamePropsPath)) {

				state = (PersistableGameState) xStream.fromXML(gamePropsPath.toFile());

				//Replay changes on units.
				for (int command = 0; command <= state.currentCommand; command++) {
					final Optional<CommandHistory> optHist = state.getCommandForIndex(command);
					if (!optHist.isPresent()) {
						continue;
					}
					for (UnitChange uc : optHist.get().getChanges()) {
						Unit u = findUnitByCode(uc.getUnitCode());
						u.setStatus(uc.getAfter());
					}
				}

			}
		} catch (IOException e) {
			throw new GbohError(e);
		}
	}

	Unit findUnitByCode(final String unitCode) {
		final List<Unit> candidates =
				getAllUnits().stream().filter(u -> u.getUnitCode().equals(unitCode)).collect(Collectors.toList());
		Preconditions.checkArgument(candidates.size() > 0, "There should be one unit named:" + unitCode);
		Preconditions.checkArgument(candidates.size() == 1, "There should be no more than one unit named:" + unitCode);

		return candidates.get(0);
	}

	private Path getGamePropertiesPath() {
		return currentDir.resolve("game.xml");
	}

	private Army loadArmy(final Path path) throws IOException {
		String name = path.getFileName().toString().split("\\.")[0].split("_")[1];

		CSVFormat format = buildCsvFormat();
		try (Reader r = Files.newBufferedReader(path)) {
			final List<CSVRecord> records = format.parse(r).getRecords();
			List<Unit> units = records.stream().map(this::fromRecordToUnit).collect(Collectors.toList());

			return new Army(name, units);
		}

	}

	private CSVFormat buildCsvFormat() {
		return CSVFormat.newFormat('\t').withFirstRecordAsHeader().withRecordSeparator("\r\n");
	}

	private Unit fromRecordToUnit(final CSVRecord r) {
		final int tq = Integer.parseInt(r.get(Head.TQ));
		final int size = Integer.parseInt(r.get(Head.Size));

		final UnitKind kind = Helper.readEnum(r.get(Head.Kind), UnitKind.class, UnitKind.LG);
		final SubClass sc = Helper.readEnum(r.get(Head.Subclass), SubClass.class, SubClass.NONE);
		final MissileType missileType = Helper.readEnum(r.get(Head.Missile), MissileType.class, MissileType.NONE);

		final Unit unit =
				new Unit(kind, sc, r.get(Head.Origin), r.get(Head.Number), r.get(Head.UnitCode), tq, size, missileType);
		if (r.isSet(Head.Hits.toString()) && Strings.isNotEmpty(r.get(Head.Hits))) {
			unit.getStatus().hits = Integer.parseInt(r.get(Head.Hits));
		}
		if (r.isSet(Head.State.toString()) && Strings.isNotEmpty(r.get(Head.State))) {
			unit.getStatus().state = UnitState.valueOf(r.get(Head.State));
		}
		if (r.isSet(Head.MissileStatus.toString()) && Strings.isNotEmpty(r.get(Head.MissileStatus))) {
			unit.getStatus().missileStatus = MissileStatus.valueOf(r.get(Head.MissileStatus));
		}
		return unit;
	}

	public void persistGame() {
		persistGame(false);
	}

	public void persistGame(boolean newFile) {
		try {
			final Path gamePropertiesPath;
			if (newFile) {
				final Path basicFile = getGamePropertiesPath();
				gamePropertiesPath = basicFile.getParent()
						.resolve("backup." + state.currentTurn + "." + state.currentCommand + ".xml");
				if (Files.exists(gamePropertiesPath)) {
					Files.deleteIfExists(gamePropertiesPath);
				}
			} else {
				gamePropertiesPath = getGamePropertiesPath();
			}

			xStream.toXML(state, Files.newBufferedWriter(gamePropertiesPath));
		} catch (IOException e) {
			throw new GbohError(e);
		}
	}

	public List<Unit> getAllUnits() {
		final ArrayList<Unit> units = new ArrayList<>();
		units.addAll(army1.getUnits());
		units.addAll(army2.getUnits());
		return units;
	}

	public void nextCommand(String nextCommandText) {
		state.currentCommand++;
		this.commandText = nextCommandText;
	}

	public void persistGeneralProperties() {
		generalProperties.put("currentBattle", currentDir.toAbsolutePath().toString());
		Helper.storeProperties(generalProperties);
	}

	public Properties loadProperties() {
		generalProperties = Helper.loadProperties();
		return generalProperties;

	}

	public void dump() {
		dumpArmy(army1);
		dumpArmy(army2);
	}

	private enum Head {
		TQ, Size, Kind, Subclass, Missile, Origin, Number, UnitCode("Unit code"), Hits, State, MissileStatus;

		private String header;

		Head(final String header) {
			this.header = header;
		}

		Head() {
			this.header = name();
		}

		@Override
		public String toString() {
			return header;
		}
	}

	private void dumpArmy(final Army army) {
		final String armyFileName =
				"Dump_" + state.currentTurn + "_" + state.currentCommand + "_" + army.getName() + ".tsv";
		Path dumpPath = currentDir.resolve(armyFileName);
		try (CSVPrinter printer = new CSVPrinter(new FileWriter(dumpPath.toFile()), buildCsvFormat())) {
			printer.printRecord(Head.Kind, Head.Subclass, Head.Origin, Head.Number, Head.UnitCode, Head.TQ, Head.Size,
					Head.Missile, Head.Hits, Head.State, Head.MissileStatus);

			for (Unit u : army.getUnits()) {
				printer.printRecord(u.getKind(), u.getSubclass(), u.getOrigin(), u.getNumber(), u.getUnitCode(),
						u.getOriginalTq(), u.getSize(), u.getMissile(), u.getHits(), u.getState(),
						u.getMissileStatus());

			}

		} catch (IOException ex) {
			throw new GbohError(ex);
		}
	}

	public static class FindUnitsResult {
		public List<Unit> foundUnits = new ArrayList<>();
		public List<String> unknownValues = new ArrayList<>();
	}

	public FindUnitsResult findUnits(final String unitsQuery) {
		final List<String> queries = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(unitsQuery);

		FindUnitsResult res = new FindUnitsResult();

		for (String q : queries) {
			if(army1.getName().equals(q)){
				res.foundUnits.addAll(army1.getUnits());
				continue;
			}
			if(army2.getName().equals(q)){
				res.foundUnits.addAll(army2.getUnits());
				continue;
			}

			final List<Unit> foundUnits =
					getAllUnits().stream().filter(u -> unitIsMatchingQuery(q, u)).collect(Collectors.toList());
			if (!foundUnits.isEmpty()) {
				res.foundUnits.addAll(foundUnits);
			} else {
				res.unknownValues.add(q);
			}
		}

		return res;

	}

	boolean unitIsMatchingQuery(final String q, final Unit u) {
		return Pattern.matches(q.toLowerCase(), u.getUnitCode().toLowerCase());
	}

	public void endOfTurn() {
		//Routing count

		//Rally unit

		state.currentTurn++;
	}

	public void recordChange(UnitStatus before, Unit u) {
		Preconditions.checkArgument(before != u.getStatus(), "Status before should be different than after");
		Preconditions.checkArgument(!before.equals(u.getStatus()), "Unit should have changed");

		UnitChange uc = new UnitChange(u.getUnitCode(), before, Helper.clone(u.getStatus()));

		final Optional<CommandHistory> optHist = state.getCommandForIndex(state.currentCommand);
		CommandHistory hist;
		if (!optHist.isPresent()) {
			hist = new CommandHistory(state.currentCommand, commandText);
			if (state.commandHistories == null) {
				state.commandHistories = new ArrayList<>();
			}
			state.commandHistories.add(hist);
		} else {
			hist = optHist.get();
		}
		hist.getChanges().add(uc);

	}

	public PersistableGameState getState() {
		return state;
	}

	public Army getArmy1() {
		return army1;
	}

	public Army getArmy2() {
		return army2;
	}
}
