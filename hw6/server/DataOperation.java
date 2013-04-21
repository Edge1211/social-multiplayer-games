package org.zhihanli.hw6.server;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.zhihanli.hw8.GamePeriodData;
import org.zhihanli.hw8.Ranking;

import static com.googlecode.objectify.ObjectifyService.ofy;

import com.googlecode.objectify.Work;

public class DataOperation {

	public static String getRankAndRD(String email) {
		PlayerEntity p = ofy().load().type(PlayerEntity.class).id(email).get();
		if (p == null)
			return null;
		return p.rank + " " + p.rd;
	}

	public static String getPlayersInfoWithMatchId(Long id) {
		Match m = ofy().load().type(Match.class).id(id).get();
		if (m == null)
			return null;
		return m.playerOneEmail + "##" + m.playerTwoEmail;

	}

	public static String getStateAndTurnAndPlayerInfoWithMatchId(Long id) {
		Match m = ofy().load().type(Match.class).id(id).get();
		if (m == null)
			return null;
		return m.state + "##" + m.turn + "##" + m.playerOneEmail + "##"
				+ m.playerTwoEmail;
	}

	public static void updateMatch(final Long id, final String state,
			final String turn, final String result) {
		@SuppressWarnings("unused")
		Boolean res = ofy().transact(new Work<Boolean>() {
			public Boolean run() {
				Match m = ofy().load().type(Match.class).id(id).get();
				if (m == null)
					return false;
				m.state = state;
				m.turn = turn;
				m.result = result;
				ofy().save().entity(m);

				String players = getPlayersInfoWithMatchId(id);
				String p1Email = players.split("##")[0];
				String p2Email = players.split("##")[1];

				// load p1 add match
				PlayerEntity p1 = ofy().load().type(PlayerEntity.class)
						.id(p1Email).get();
				if (p1 == null)
					return false;

				p1.matches.remove(m);
				p1.matches.add(m);

				ofy().save().entity(p1);

				// load p2 add match
				PlayerEntity p2 = ofy().load().type(PlayerEntity.class)
						.id(p2Email).get();
				if (p2 == null)
					return false;

				p2.matches.remove(m);
				p2.matches.add(m);

				ofy().save().entity(p2);
				return true;
			}
		});

	}

	public static List<Match> getMatchList(String email) {
		PlayerEntity p = ofy().load().type(PlayerEntity.class).id(email).get();
		if (p == null)
			return null;
		return p.getMatchList();
	}

	public static boolean deleteMatchFromPlayer(final String email,
			final Long matchId) {

		Boolean res = ofy().transact(new Work<Boolean>() {

			public Boolean run() {
				PlayerEntity p = ofy().load().type(PlayerEntity.class)
						.id(email).get();
				if (p == null)
					return false;

				Iterator<Match> itr = p.matches.iterator();
				while (itr.hasNext()) {
					Match m = itr.next();
					if (m.matchID.equals(matchId))
						itr.remove();
				}

				ofy().save().entities(p);
				return true;
			}

		});
		return res;
	}

	public static List<String> getMatchStringList(String email) {

		List<Match> matchList = getMatchList(email);

		List<String> matchStringList = new LinkedList<String>();
		if (matchList == null || matchList.isEmpty()) {
			// matchStringList.add("NOT FOUND");
			return matchStringList;
		}
		for (Match m : matchList) {
			matchStringList.add(m.toString());
		}
		return matchStringList;
	}

	public static PlayerEntity getPlayer(String email) {
		return ofy().load().type(PlayerEntity.class).id(email).get();

	}

	public static void savePlayer(PlayerEntity player) {
		ofy().save().entities(player);
	}

	public static void deletePlayerWithEmail(String email) {
		ofy().delete().type(PlayerEntity.class).id(email);
	}

	public static void deleteMatchWithMatchId(String matchid) {
		ofy().delete().type(Match.class).id(matchid);
	}

	public static void newMatchTransaction(Long matchId, final String p1Email,
			final String p2Email, String state, String turn, String result,
			Date date) {
		final Match newMatch = new Match(matchId, p1Email, p2Email, state,
				turn, result, date);
		@SuppressWarnings("unused")
		Boolean res = ofy().transact(new Work<Boolean>() {
			public Boolean run() {
				ofy().save().entity(newMatch);

				// load p1 add match
				PlayerEntity p1 = ofy().load().type(PlayerEntity.class)
						.id(p1Email).get();
				if (p1 == null)
					p1 = new PlayerEntity(p1Email, null);

				p1.matches.add(newMatch);

				ofy().save().entity(p1);

				// load p2 add match
				PlayerEntity p2 = ofy().load().type(PlayerEntity.class)
						.id(p2Email).get();
				if (p2 == null)
					p2 = new PlayerEntity(p2Email, null);

				p2.matches.add(newMatch);

				ofy().save().entity(p2);
				return true;
			}
		});
	}

	public static void addWaitingPlayer(String email) {
		WaitingPlayers p = new WaitingPlayers(email);
		ofy().save().entity(p);
	}

	public static void delWaitingPlayer(final String email1, final String email2) {
		if (email1 == null || email2 == null)
			return;
		@SuppressWarnings("unused")
		Boolean res = ofy().transact(new Work<Boolean>() {
			public Boolean run() {
				WaitingPlayers p = ofy().load().type(WaitingPlayers.class)
						.id(email1).get();
				if (p != null)
					ofy().delete().entities(p).now();

				p = ofy().load().type(WaitingPlayers.class).id(email2).get();
				if (p != null)
					ofy().delete().entities(p).now();
				return true;
			}
		});

	}

	public static void delWaitingPlayer(final String email) {
		@SuppressWarnings("unused")
		Boolean res = ofy().transact(new Work<Boolean>() {
			public Boolean run() {
				WaitingPlayers p = ofy().load().type(WaitingPlayers.class)
						.id(email).get();
				if (p != null)
					ofy().delete().entities(p).now();

				return true;
			}
		});

	}

	public static String getOpponent(String email) {

		List<WaitingPlayers> playerList = ofy().load()
				.type(WaitingPlayers.class).list();
		for (WaitingPlayers p : playerList) {
			if (!p.emailid.equals(email))
				return p.emailid;
		}
		return null;
	}

	public static String updateGamePeriodData(final String email,
			final String date, final int RD, final double r, final double s) {
		@SuppressWarnings("unused")
		String res = ofy().transact(new Work<String>() {
			public String run() {

				PlayerEntity p = ofy().load().type(PlayerEntity.class)
						.id(email).get();
				String recordDate = p.gamePeriodData.getDate();
				Date currentDate;

				try {
					currentDate = new SimpleDateFormat("MMMM d, yyyy",
							Locale.ENGLISH).parse(date);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					return null;
				}

				// if in same game period, just add new info and update ranking
				if (p != null) {
					if (recordDate.equals(date)) {
						p.gamePeriodData.addr(r);
						p.gamePeriodData.addRD(RD);
						p.gamePeriodData.adds(s);
						ofy().save().entity(p);
						updateRanking(email, currentDate);

					} else {

						// update ranking, create a new Game Period Data, add
						// the corresponding info

						updateRanking(email, currentDate);

						p.gamePeriodData = new GamePeriodData(
								dateToString(currentDate));
						p.gamePeriodData.addr(r);
						p.gamePeriodData.addRD(RD);
						p.gamePeriodData.adds(s);
						ofy().save().entity(p);

					}
				}else{
					return null;
				}

				return p.rank+" "+p.rd;
			}
		});
		
		return res;
	}

	public static void updateRanking(final String email, final Date currentDate) {
		@SuppressWarnings("unused")
		Boolean res = ofy().transact(new Work<Boolean>() {

			public Boolean run() {

				PlayerEntity p = ofy().load().type(PlayerEntity.class)
						.id(email).get();
				// String recordDate = p.gamePeriodData.getDate();

				if (p != null) {
					GamePeriodData data = p.gamePeriodData;
					if (data != null) {
						ArrayList<Integer> RDs = data.getRDs();
						ArrayList<Double> rs = data.getrs();
						ArrayList<Double> s = data.gets();

						// compute how long passed since last competition
						Date lastCompDate = p.lastCompetition;
						int daysPassed;
						if (lastCompDate == null) {
							daysPassed = 365;
						} else {
							daysPassed = daysBetween(lastCompDate, currentDate);
						}
						Ranking ranking = new Ranking(RDs, rs, s, p.rank, p.rd,
								daysPassed);

						int newRank = (int) ranking.computeNewRanking();
						int newRD = (int) ranking.computeNewRD();

						p.rank = newRank;
						p.rd = newRD;
						p.lastCompetition=currentDate;
						ofy().save().entity(p);
					}

				}

				return true;
			}
		});

	}

	public static int daysBetween(Date earlier, Date later) {
		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		calendar1.setTime(earlier);
		calendar2.setTime(later);

		long milliseconds1 = calendar1.getTimeInMillis();

		long milliseconds2 = calendar2.getTimeInMillis();
		long diff = milliseconds2 - milliseconds1;
		long diffDays = diff / (24 * 60 * 60 * 1000);

		return (int) diffDays;

	}

	public static String dateToString(Date date) {

		DateFormat dtf = DateFormat.getDateInstance();
		String formatedDate = dtf.format(date);
		return formatedDate;
	}
}