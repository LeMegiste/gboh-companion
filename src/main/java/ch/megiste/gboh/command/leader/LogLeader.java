package ch.megiste.gboh.command.leader;

import ch.megiste.gboh.army.Leader;

public class LogLeader extends LeaderCommand {
	public static final String LL = "LL";

	public LogLeader() {
		super("Logs the status of a leader");
	}

	@Override
	public void execute(final Leader leader) {
		console.logNL(logLeaderWithDetails(leader));
	}

	public static String logLeader(final Leader leader) {
		StringBuffer sb = new StringBuffer();
		sb.append(leader.getCode()).append(" ").append(leader.getName()).append(" (").append(leader.getInitiative())
				.append(")");
		return sb.toString();
	}

	public static String logLeaderWithDetails(final Leader leader) {
		StringBuffer sb = new StringBuffer();
		sb.append(logLeader(leader)).append(" - ");
		if (leader.isFinished()) {
			sb.append("FINISHED - ");
		}
		if (leader.getNbActivations() == 1) {
			sb.append(leader.getNbActivations()).append(" activation");
		} else if (leader.getNbActivations() > 1) {
			sb.append(leader.getNbActivations()).append(" activations");
		}
		if(!leader.isPresent()){
			sb.append(" absent from the battlefield");
		}

		return sb.toString();
	}

	@Override
	public String getKey() {
		return LL;
	}
}
