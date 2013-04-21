package org.zhihanli.hw6.server;

import java.text.DateFormat;
import java.util.HashMap;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.shared.chess.*;
import org.zhihanli.hw6.client.ChessService;
import org.zhihanli.hw6.client.MoveSerializer;
import org.zhihanli.hw8.GamePeriodData;
import org.zhihanli.hw2.StateChangerImpl;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.googlecode.objectify.ObjectifyService;

public class ChessServiceImpl extends RemoteServiceServlet implements
		ChessService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// private static LinkedList<Player> players = new LinkedList<Player>();
	private static HashMap<String, String> email_token = new HashMap<String, String>();
	private static HashMap<String, String> email_name = new HashMap<String, String>();
	private static LinkedList<String> waitingPlayers = new LinkedList<String>();
	// private static HashSet<String> waitingPlayers=new HashSet<String>();

	// private static LinkedList<PlayerPair> PlayerPairs = new
	// LinkedList<PlayerPair>();
	private static ChannelService channelService = ChannelServiceFactory
			.getChannelService();
	StateChangerImpl stateChanger = new StateChangerImpl();

	// private Date today=new Date();

	static {
		waitingPlayers = new LinkedList<String>();
		// register entity
		ObjectifyService.register(Match.class);
		ObjectifyService.register(PlayerEntity.class);
		ObjectifyService.register(WaitingPlayers.class);
		ObjectifyService.register(GamePeriodData.class);
	}

	@Override
	/**
	 * login method, create player on server, return token for channel communication
	 */
	public String login() {

		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();

		if (user == null)
			return null;

		String name = user.getNickname();
		String email = user.getEmail();
		String token = channelService.createChannel(email);

		// Player newPlayer = new Player(email, token);
		// newPlayer.setName(name);

		// if (!players.contains(newPlayer)) {
		// players.add(newPlayer);
		// }
		email_token.put(email, token);
		email_name.put(email, name);

		updateNewPlayerEntity(email, name);

		String rankAndRD = DataOperation.getRankAndRD(email);
		String rank = "1500";
		String RD = "350";
		if (rankAndRD != null) {
			rank = rankAndRD.split(" ")[0];
			RD = rankAndRD.split(" ")[1];
		}

		return name + " " + email + " " + token + " " + rank + " " + RD;
	}

	@Override
	public List<String> requestMatchList(String userid) {
		return DataOperation.getMatchStringList(userid);
	}

	private void updateNewPlayerEntity(String email, String name) {
		// DataOperation.savePlayer(new Player(userid,))
		PlayerEntity p = DataOperation.getPlayer(email);

		Date date = new Date();
		DateFormat dtf = DateFormat.getDateInstance();
		String formatedDate = dtf.format(date);

		if (p != null) {
			p.name = name;

			if (p.gamePeriodData == null
					|| !p.gamePeriodData.getDate().equals(formatedDate)) {

				p.gamePeriodData = new GamePeriodData(formatedDate);
			}
			DataOperation.savePlayer(p);
		} else {
			PlayerEntity playerEnt = new PlayerEntity(email, name);
			playerEnt.gamePeriodData = new GamePeriodData(formatedDate);

			DataOperation.savePlayer(playerEnt);
		}
	}

	@Override
	public boolean autoMatch(String email) {
		// return true;
		return matchOnDataStore(email);
	}

	private boolean match(String email) {
		String player;
		if (waitingPlayers == null)
			waitingPlayers = new LinkedList<String>();
		if (waitingPlayers.isEmpty()) {
			waitingPlayers.add(email);
			// DataOperation.addWaitingPlayer(System.currentTimeMillis(),
			// email);
			return false;
		} else {

			player = waitingPlayers.pollFirst();
			if (player == null)
				return false;
			if (player.equals(email)) {
				waitingPlayers.add(player);
				if (waitingPlayers.size() == 1) {
					return false;
				} else {
					player = waitingPlayers.pollFirst();
					if (player == null)
						return false;
				}
			}

		}

		if (player.equals(email))
			return false;

		Long matchId = createNewMatch(player, email);

		channelService.sendMessage(new ChannelMessage(player, "RB " + email
				+ " " + matchId));
		channelService.sendMessage(new ChannelMessage(email, "RW " + player
				+ " " + matchId));

		return true;
	}

	private boolean matchOnDataStore(String email) {
		if (email == null)
			return false;
		DataOperation.addWaitingPlayer(email);
		// String opp=null;
		String opp = DataOperation.getOpponent(email);
		if (opp == null)
			return false;
		DataOperation.delWaitingPlayer(email, opp);
		Long matchId = createNewMatch(opp, email);

		channelService.sendMessage(new ChannelMessage(opp, "RB " + email + " "
				+ matchId));
		channelService.sendMessage(new ChannelMessage(email, "RW " + opp + " "
				+ matchId));

		return true;

	}

	@Override
	public boolean sendMove(String move, String userid) {
		if (move == null)
			return false;
		String moveToSend = move.split("#")[0];
		String matchId = move.split("#")[1];

		// get state and turn
		String stateNturn = DataOperation
				.getStateAndTurnAndPlayerInfoWithMatchId(new Long(matchId));
		String state = stateNturn.split("##")[0];
		String turn = stateNturn.split("##")[1];

		// change state
		State s = StateSerializer.deserialize(state);
		Move m = MoveSerializer.stringToMove(move);

		stateChanger.makeMove(s, m);

		// change turn
		String players = DataOperation.getPlayersInfoWithMatchId(new Long(
				matchId));
		String p1Email = players.split("##")[0];
		String p2Email = players.split("##")[1];

		turn = turn.equals(p1Email) ? p2Email : p1Email;
		dispatchMove(moveToSend, turn);
		
		// update info
		DataOperation.updateMatch(new Long(matchId), StateSerializer
				.serialize(s), turn, s.getGameResult() == null ? null : s
				.getGameResult().toString());

		// TODO: update ranking

		// if game ends, add opponent RD, r, and s
		if (s.getGameResult() != null) {
			Date currentDate = new Date();
			String currentDateString = DataOperation.dateToString(currentDate);
			Color winner = s.getGameResult().getWinner();

			String myEmail = turn.equals(p1Email) ? p2Email : p1Email;
			String myRankAndRD = DataOperation.getRankAndRD(myEmail);
			String myRankString = myRankAndRD.split(" ")[0];
			int myRank = Integer.valueOf(myRankString);
			String myRDString = myRankAndRD.split(" ")[1];
			int myRD = Integer.valueOf(myRDString);
			// double myS=winnerEmail.equals(myEmail)?1.0:
			double myS;

			String oppRankAndRD = DataOperation.getRankAndRD(turn);
			String oppRankString = oppRankAndRD.split(" ")[0];
			String oppRDString = oppRankAndRD.split(" ")[1];
			int oppRank = Integer.valueOf(oppRankString);
			int oppRD = Integer.valueOf(oppRDString);
			double oppS;

			if (winner == null) {
				myS = 0.5;
				oppS = 0.5;

			} else {
				String winnerEmail = winner == Color.WHITE ? p2Email : p1Email;
				if (winnerEmail.equals(myEmail)) {
					myS = 1.0;
					oppS = 0;
				} else {
					myS = 0;
					oppS = 1.0;
				}

			}

			String rankAndRD = DataOperation.updateGamePeriodData(turn,
					currentDateString, myRD, (double) myRank, oppS);

			channelService.sendMessage(new ChannelMessage(turn, "rank#"
					+ rankAndRD));

			rankAndRD = DataOperation.updateGamePeriodData(myEmail,
					currentDateString, oppRD, (double) oppRank, myS);
			channelService.sendMessage(new ChannelMessage(myEmail, "rank#"
					+ rankAndRD));

			//
		}
		return true;
	}

	public boolean dispatchMove(String move, String userid) {
		// find the correct client to send to move through channel
		// Player targetPlayer = null;
		// for (PlayerPair pair : PlayerPairs) {
		// targetPlayer = pair.getAnotherPlayer(userid);
		// if (targetPlayer != null) {
		// break;
		// }
		// }

		// if (targetPlayer == null)
		// return false;

		// found target player, send the move through
		channelService.sendMessage(new ChannelMessage(userid, "M" + move));
		return true;
	}

	private Long createNewMatch(String p1Email, String p2Email) {

		// String matchId = Integer.toString(count++);
		Long matchId = System.currentTimeMillis();
		State newState = new State();
		String state = StateSerializer.serialize(newState);
		// String newMatch = matchId + " " + p1Email + " " + p2Email + " "
		// + p2Email + " " + "Ongoing "+(new Date()).toString();

		DataOperation.newMatchTransaction(matchId, p1Email, p2Email, state,
				p2Email, null, new Date());
		channelService.sendMessage(new ChannelMessage(p1Email, "NM"));

		channelService.sendMessage(new ChannelMessage(p2Email, "NM"));

		return matchId;

	}

	@Override
	public void sendNewMatch(String p1Email, String p2Email) {
		createNewMatch(p1Email, p2Email);
	}

	@Override
	public String getStateAndTurnAndPlayerInfoWithMatchId(Long matchId) {
		return DataOperation.getStateAndTurnAndPlayerInfoWithMatchId(matchId);
	}

	@Override
	public void deleteMatchFromPlayer(String email, Long matchId) {
		DataOperation.deleteMatchFromPlayer(email, matchId);

	}

	@Override
	public String getWaitingList() {
		return waitingPlayers.toString();
	}

}
