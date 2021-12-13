package ch.megiste.gboh.command.game;

import static ch.megiste.gboh.command.leader.LogLeader.logLeader;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;

import ch.megiste.gboh.army.Leader;
import ch.megiste.gboh.command.leader.LogLeader;
import ch.megiste.gboh.game.GameStatus;

public class EndOrderPhase extends GameCommand {
	public EndOrderPhase() {
		super("Marks current leader as finished");
	}

	@Override
	public String getKey() {
		return "EO";
	}

	@Override
	public void execute(final List<String> commandArgs) {
		final GameStatus gs = getGameStatus();
		if (!gs.areLeadersUsed()) {
			console.logNL("This scenario did not define leaders. The command cannot be executed.");
		}

		if (gs.getCurrentLeader() == null) {
			console.logNL("No current leader");
		}
		final Leader l = gs.getCurrentLeader();
		final String leaderName = logLeader(l);
		final String elite = l.usedElite() ? " elite": "";
		console.logFormat("Ending the current%s order phase for %s",elite,leaderName);
		leadersHandler.endOrderPhase(gs.getCurrentLeader());
		if(l.usedElite() && l.getNbActivations()==1){
			gs.activateNextLeader();
			return;
		}
		if (!l.isFinished()) {
			final List<Leader> trumpingLeaders =
					gameStatus.getAllLeaders().stream().filter(l2 -> l2.didTrump()).collect(Collectors.toList());
			Collections.reverse(trumpingLeaders);
			if(trumpingLeaders.size()>0 && l.getInitiative()<trumpingLeaders.get(0).getInitiative()){
				console.logFormat("%s did trump. %s cannot use momentum and is finished", logLeader(trumpingLeaders.get(0)),logLeader(l));
				leadersHandler.markLeaderAsFinished(l);
				gs.activateNextLeader();
				return;
			}


			String val = console.readLine(
					String.format("Do you roll for momentum. Current initiative %d?[y/n]%n>>", l.getInitiative()));
			if ("y".equals(val)) {
				int d = dice.roll();
				if (d <= l.getInitiative()) {
					console.logFormat("Dice rolled [%d]. %s got a momentum and can continue giving orders", d,
							leaderName);
				} else if (d < 9) {
					console.logFormat("Dice rolled [%d]. %s failed his momentum attempt and is now finished",
							d, leaderName);
					leadersHandler.markLeaderAsFinished(l);
					gs.activateNextLeader();
				} else if (d == 9) {

					console.readLine("DIE ROLL OF DOOM!! Press enter to see the consequences!");
					final int doom = dice.roll();
					if (doom > 1 && doom < 9) {
						console.logFormat(
								"Dice rolled [%d]. Nothing happens, the gods are with you. %s is now finished",
								doom, leaderName);
						leadersHandler.markLeaderAsFinished(l);
						gs.activateNextLeader();
					} else if (doom <= 1) {
						console.logFormat(
								"Dice rolled [%d]. A leader of the opposing camp can be activated. %s is now finished. ",
								doom, leaderName);
						gs.getAllLeaders().forEach(ll->console.logNL(logLeader(ll)));
						List<String> leaderCodes =
								gs.getAllLeaders().stream().map(Leader::getCode).collect(Collectors.toList());
						String leaderCodesString = Joiner.on(",").join(leaderCodes);
						String code = console.readLine(
								String.format("Please choose the leader to be activated now?[y/n]%n%s%n>>",
										leaderCodesString));
						while (!leaderCodes.contains(code)) {
							code = console.readLine(
									String.format("Please choose the leader to be activated now?[y/n]%n%s%n>>",
											leaderCodesString));
						}
						leadersHandler.markLeaderAsFinished(l);
						Leader reactivatedLeader = gameStatus.findLeaderByCode(code);
						leadersHandler.reactivateLeader(reactivatedLeader);
					}
				}

			} else {
				leadersHandler.markLeaderAsFinished(l);
				gs.activateNextLeader();
			}
		} else {
			gs.activateNextLeader();
		}

		if (l.isFinished() && gs.computeNextLeader() == null) {
			console.logNL("There is no leader left to activate. The only command possible is END_TURN");
		}

	}
}