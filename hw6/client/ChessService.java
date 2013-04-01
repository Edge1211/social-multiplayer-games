package org.zhihanli.hw6.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("zhihanli")
public interface ChessService extends RemoteService {

	boolean sendMove(String move, String userid);

	String login(String userid);
	
	String askForGoogleUserid(String userid);
	
	boolean autoMatch();
}
