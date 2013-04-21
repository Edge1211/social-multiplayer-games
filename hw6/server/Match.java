package org.zhihanli.hw6.server;

import java.io.Serializable;
import java.util.Date;

import com.googlecode.objectify.annotation.Embed;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
@Embed
public class Match implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	Long matchID;
	String playerOneEmail;
	String playerTwoEmail;
	String state;
	String turn;
	String result;
	Date startDate;

	public Match() {

	}

	public Match(Long matchId, String p1Email, String p2Email, String state,
			String turn, String result,Date date) {
		this.matchID = matchId;
		this.playerOneEmail = p1Email;
		this.playerTwoEmail = p2Email;
		this.state = state;
		this.turn = turn;
		this.result = result;
		this.startDate=date;
	}

	@Override
	public String toString() {
		return matchID + "#" + playerOneEmail + "#" + playerTwoEmail + "#"
				+ turn + "#" + (result==null?"Ongoing":result)+"#"+ startDate.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Match))
			return false;

		Match m = (Match) obj;
		return this.matchID.equals(m.matchID);
	}

}
