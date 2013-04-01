package org.zhihanli.hw6.server;

import java.util.LinkedList;

import org.zhihanli.hw6.client.ChessService;
import org.zhihanli.hw6.client.Player;
import org.zhihanli.hw6.client.PlayerPair;
import org.zhihanli.hw6.client.Status;
import org.zhihanli.hw7.DataOperation;
import org.zhihanli.hw7.Match;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.googlecode.objectify.ObjectifyService;

public class ChessServiceImpl extends RemoteServiceServlet implements
		ChessService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static LinkedList<Player> players = new LinkedList<Player>();
	private static LinkedList<Player> waitingPlayers = new LinkedList<Player>();
	private static LinkedList<PlayerPair> PlayerPairs = new LinkedList<PlayerPair>();
	private static ChannelService channelService = ChannelServiceFactory
			.getChannelService();

	static {
		// register entity
		ObjectifyService.register(Match.class);
		ObjectifyService.register(Player.class);
	}

	@Override
	public String login(String userid) {
		if (userid == null || userid.length() == 0)
			return null;
		String token = channelService.createChannel(userid);
		Player newPlayer = new Player(userid, token);

		players.add(newPlayer);
		return newPlayer.getToken();
	}
	
	private void createNewPlayer(String userid,String token){
		DataOperation.savePlayer()
		
	}
	
	
	

	@Override
	public String askForGoogleUserid(String userid) {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();

		if (user == null)
			return null;
		String name = user.getNickname();
		String email = user.getEmail();

		for (Player p : players) {
			if (p.getUserid().equals(userid)) {
				p.setName(name);
				p.setEmail(email);
			}
		}
		return name + " " + email;
	}

	@Override
	public boolean autoMatch() {
		return match();
	}

	private boolean match() {
		if (waitingPlayers.size() < 2)
			return false;

		Player playerOne = waitingPlayers.pollFirst();
		playerOne.setStatus(Status.PLAYING);

		Player playerTwo = waitingPlayers.pollFirst();
		playerTwo.setStatus(Status.PLAYING);

		PlayerPairs.add(new PlayerPair(playerOne, playerTwo));
		channelService.sendMessage(new ChannelMessage(playerOne.getUserid(),
				"RB" + playerTwo.getName()));
		channelService.sendMessage(new ChannelMessage(playerTwo.getUserid(),
				"RW" + playerOne.getName()));

		return true;
	}

	@Override
	public boolean sendMove(String move, String userid) {
		if (move == null)
			return false;

		if (move.equals("C")) {
			for (Player p : players) {
				if (p.getUserid().equals(userid)) {
					waitingPlayers.add(p);
					return true;
				}
			}
			return false;
		} else {
			return dispatchMove(move, userid);
		}
	}

	public boolean dispatchMove(String move, String userid) {
		// find the correct client to send to move through channel
		Player targetPlayer = null;
		for (PlayerPair pair : PlayerPairs) {
			targetPlayer = pair.getAnotherPlayer(userid);
			if (targetPlayer != null) {
				break;
			}
		}

		if (targetPlayer == null)
			return false;

		// found target player, send the move through
		channelService.sendMessage(new ChannelMessage(targetPlayer.getUserid(),
				"M" + move));
		// targetPlayer.sendMessageToClient("M" + move);

		return true;
	}

	private void createMatch() {

	}
}
