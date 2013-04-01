package org.zhihanli.hw7;

import java.util.List;


import static com.googlecode.objectify.ObjectifyService.ofy;
import com.googlecode.objectify.Work;

public class DataOperation {

	public static List<Match> getMatchList(String email) {
		return ofy().load().type(Player.class).id(email).get().getMatchList();

	}

	public static void savePlayer(Player player) {
		ofy().save().entities(player);
	}

	public static void deletePlayerWithEmail(String email) {
		ofy().delete().type(Player.class).id(email);
	}

	public static void deleteMatchWithMatchId(String matchid) {
		ofy().delete().type(Match.class).id(matchid);
	}

	public static void newMatchTransaction(String matchId,
			final String p1Email, final String p2Email, String state,
			String turn, String result) {
		final Match newMatch = new Match(matchId, p1Email, p2Email, state,
				turn, result);
		Boolean res = ofy().transact(new Work<Boolean>() {
			public Boolean run() {
				ofy().save().entity(newMatch);

				// load p1 add match
				Player p1 = ofy().load().type(Player.class).id(p1Email).get();
				p1.addMatch(newMatch);
				ofy().save().entity(p1);

				// load p2 add match
				Player p2 = ofy().load().type(Player.class).id(p2Email).get();
				p2.addMatch(newMatch);
				ofy().save().entity(p2);
				return true;
			}
		});
	}
}