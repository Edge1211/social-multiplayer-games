package org.zhihanli.hw6.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ChessServiceAsync {

	void sendMove(String move, String userid,AsyncCallback<Boolean> callback);

	void login(String userid, AsyncCallback<String> callback);
	
	void askForGoogleUserid(String userid, AsyncCallback<String> callback);
	
	void autoMatch(AsyncCallback<Boolean> callback);

}
