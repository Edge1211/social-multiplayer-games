package org.zhihanli.hw6.client;



public class Player {
	private String userid;
	private String name;
	private String token;
	private Status status;
	private String email;

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
	
	public void setEmail(String email){
		this.email=email;
	}
	
	public String getEmail(){
		return email;
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
}
