package org.zhihanli.hw8;

import com.google.gwt.i18n.client.Messages;

public interface GameMessages extends Messages {
	// String setPlayerInfo(String p1, String p2);

	@DefaultMessage("Current Player:  {1} <{0}>")
	String setCurrentPlayerInfo(String email, String name);

	// String setGameResult(String result);

	@DefaultMessage("Auto match")
	String setAutoMatchLabel();

	@DefaultMessage("Sorry,no other players available now, you will be matched once there is an available player")
	String setAutoMatchFailReply();

	@DefaultMessage("Delete selected match")
	String setDeleteMatchLabel();

	@DefaultMessage("Create a game match")
	String setStartGameLabel();

	@DefaultMessage("Waiting to play")
	String setWaitingStatus();

	@DefaultMessage("White")
	String colorWhite();

	@DefaultMessage("Black")
	String colorBlack();

	@DefaultMessage(" to play")
	String toPlay();

	@DefaultMessage("Game end.")
	String gameEnd();

	@DefaultMessage("wins")
	String win();

	@DefaultMessage("Reason: Checkmate.")
	String checkMate();

	@DefaultMessage("Reason: Fifty-move rule")
	String fiftyMoveRule();

	@DefaultMessage("Reason: Stalemate")
	String stalemate();

	@DefaultMessage("Reason: Threefold Repetition Rule")
	String threeFoldRepetitionRule();
	
	@DefaultMessage("Start to play")
	String startToPlay();
	
	@DefaultMessage("Players: ")
	String versus();

	@DefaultMessage("You cannot play agains yourself.")
	String cannotPlayWithSelf();
	
	@DefaultMessage("Unable to connect to server.")
	String connectFailure();
	
	@DefaultMessage("Channel error.")
	String channelError();
	
	@DefaultMessage("Ranking:")
	String ranking();
	
	@DefaultMessage("Move is saved on local storage")
	String saveMoveOnLocalStorage();
}
