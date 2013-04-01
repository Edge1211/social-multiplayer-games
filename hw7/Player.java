package org.zhihanli.hw7;

import java.util.LinkedList;
import java.util.List;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class Player {
	@Id
	String email;
	String name;
	List<String> tokens;
	List<Match> matches;

	public Player() {

	}

	public Player(String email, String name) {
		this.email = email;
		this.name = name;
		tokens = new LinkedList<String>();
		matches = new LinkedList<Match>();

	}

	public Player(String email, String name, String token) {
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
