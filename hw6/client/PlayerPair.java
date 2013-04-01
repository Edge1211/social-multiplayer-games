package org.zhihanli.hw6.client;

public class PlayerPair {
	private Player PlayerOne;
	private Player PlayerTwo;

	public PlayerPair(Player PlayerOne, Player PlayerTwo) {
		this.PlayerOne = PlayerOne;
		this.PlayerTwo = PlayerTwo;
	}

	public Player getPlayerOne() {
		return PlayerOne;
	}

	public Player getPlayerTwo() {
		return PlayerTwo;
	}

	public boolean hasPlayerWithUserid(String userid) {
		return PlayerOne.getUserid().equals(userid)
				|| PlayerTwo.getUserid().equals(userid);
	}

	public Player getAnotherPlayer(String userid) {
		if (!hasPlayerWithUserid(userid))
			return null;
		
		if(PlayerOne.getUserid().equals(userid))
			return PlayerTwo;
		
		if(PlayerTwo.getUserid().equals(userid))
			return PlayerOne;
		
		return null;
	}
}
