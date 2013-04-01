package org.zhihanli.hw7;

import com.googlecode.objectify.annotation.Embed;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
@Embed
public class Match {
	@Id String matchID;
	String playerOneEmail;
	String playerTwoEmail;
	String state;
	String turn;
	String result;
	
	
	public Match(){
		
	}
	
	public Match(String matchId,String p1Email,String p2Email,String state, String turn,String result){
		this.matchID=matchId;
		this.playerOneEmail=p1Email;
		this.playerTwoEmail=p2Email;
		this.state=state;
		this.turn=turn;
		this.result=result;
	}

}
