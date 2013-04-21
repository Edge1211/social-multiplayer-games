package org.zhihanli.hw6.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("zhihanli")
public interface ChessService extends RemoteService {

	boolean sendMove(String move, String userid);

	String login();
	
	boolean autoMatch(String userid);
	
	void sendNewMatch(String p1Email,String p2Email);
	
	List<String> requestMatchList(String userid);
	
	String getStateAndTurnAndPlayerInfoWithMatchId(Long matchId);
	
	void deleteMatchFromPlayer(String email, Long matchId);
	
	String getWaitingList();
}
