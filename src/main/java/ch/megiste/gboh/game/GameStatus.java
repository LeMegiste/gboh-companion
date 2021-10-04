package ch.megiste.gboh.game;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.util.Strings;

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

	public void load(final Path battleDir) {
		try {
			if (battleDir == null) {
				final String battleDirString = generalProperties.getProperty("currentBattle");
				if (Strings.isEmpty(battleDirString)) {
					return;
				}
			}
			final Stream<Path> children = Files.list(battleDir);
			final List<Path> armyFiles = children.filter(p -> p.getFileName().toString().startsWith("Army_")).sorted()
					.collect(Collectors.toList());
			if (armyFiles.size() != 2) {
				throw new RuntimeException("We need two army files in the battle directory.");
			}
			army1 = loadArmy(armyFiles.get(0));
			army2 = loadArmy(armyFiles.get(1));
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

		CSVFormat format = CSVFormat.newFormat('\t').withFirstRecordAsHeader();
		try (Reader r = Files.newBufferedReader(path)) {
			final List<CSVRecord> records = format.parse(r).getRecords();
			List<Unit> units = records.stream().map(this::fromRecordToUnit).collect(Collectors.toList());

			return new Army(name, units);
		}

	}

	private Unit fromRecordToUnit(final CSVRecord r) {
		final int tq = Integer.parseInt(r.get("TQ"));
		final int size = Integer.parseInt(r.get("Size"));

		final UnitKind kind = Helper.readEnum(r.get("Kind"), UnitKind.class, UnitKind.LG);
		final SubClass sc = Helper.readEnum(r.get("Subclass"), SubClass.class, SubClass.NONE);
		final MissileType missileType = Helper.readEnum(r.get("Missile"), MissileType.class, MissileType.NONE);

		final Unit unit =
				new Unit(kind, sc, r.get("Origin"), r.get("Number"), r.get("Unit code"), tq, size, missileType);
		if (r.isSet("Hits") && Strings.isNotEmpty(r.get("Hits"))) {
			unit.getStatus().hits = Integer.parseInt(r.get("Hits"));
		}
		if (r.isSet("State") && Strings.isNotEmpty(r.get("State"))) {
			unit.getStatus().state = UnitState.valueOf(r.get("State"));
		}
		if (r.isSet("MissileStatus") && Strings.isNotEmpty(r.get("MissileStatus"))) {
			unit.getStatus().missileStatus = MissileStatus.valueOf(r.get("MissileStatus"));
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

	public static class FindUnitsResult {
		public List<Unit> foundUnits = new ArrayList<>();
		public List<String> unknownValues = new ArrayList<>();
	}

	public FindUnitsResult findUnits(final String unitsQuery) {
		final List<String> queries = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(unitsQuery);

		FindUnitsResult res = new FindUnitsResult();

		for (String q : queries) {
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
