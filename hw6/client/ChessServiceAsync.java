package org.zhihanli.hw6.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ChessServiceAsync {

	void sendMove(String move, String userid, AsyncCallback<Boolean> callback);

	void login(AsyncCallback<String> callback);

	void autoMatch(String userid,AsyncCallback<Boolean> callback);

	void requestMatchList(String userid, AsyncCallback<List<String>> callback);
	
	void sendNewMatch(String p1Email,String p2Email,AsyncCallback<Void> callback);

	void getStateAndTurnAndPlayerInfoWithMatchId(Long matchId, AsyncCallback<String> callback);

	void deleteMatchFromPlayer(String email, Long matchId,
			AsyncCallback<Void> callback);
	
	void getWaitingList(AsyncCallback<String> callback);
}
