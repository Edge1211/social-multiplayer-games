package org.zhihanli.hw6.server;

import java.io.Serializable;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class WaitingPlayers implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	String emailid;
//	String email;

	public WaitingPlayers() {

	}

	public WaitingPlayers(String emailid) {
		this.emailid=emailid;
//		this.email = email;
	}

}
