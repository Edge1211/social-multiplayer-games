package org.zhihanli.hw6.client;

import org.zhihanli.hw6.server.Match;



public class Player {
	private String userid;
	private String name;
	private String token;
	private Status status;
	
	public Player(String userid, String token) {
		this.token = token;
		status = Status.WAITING;
		this.userid = userid;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	
	public String getUserid() {
		return userid;
	}

	public String getToken() {
		return token;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	@Override
	public boolean equals(Object obj){
		if (this == obj) return true;
	    if (obj == null) return false;
	    if (!(obj instanceof Match)) return false;
		
	    Player p=(Player) obj;
	    return this.getUserid().equals(p.getUserid());
	}
}
