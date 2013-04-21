package org.zhihanli.hw6.server;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.zhihanli.hw8.GamePeriodData;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class PlayerEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	String email;
	String name;
	int rank;
	int rd;
	Date lastCompetition=null;
	
	GamePeriodData gamePeriodData=null;
	
	List<String> tokens = new LinkedList<String>();

	List<Match> matches = new LinkedList<Match>();

	public PlayerEntity() {

	}

	public PlayerEntity(String email, String name) {
		this.email = email;
		this.name = name;
		rank=1500;
		rd=350;
	}

	public PlayerEntity(String email, String name, String token) {
		this(email, name);
		tokens.add(token);
	}

	public void addMatch(Match match) {
		matches.add(match);
	}

	public void deleteMatch(Match match) {
		matches.remove(match);
	}

	public void addToken(String token) {
		tokens.add(token);
	}

	public void deleteToken(String token) {
		tokens.remove(token);
	}

	public List<Match> getMatchList() {
		return matches;
	}
}
