package org.zhihanli.hw8;

import java.io.Serializable;
import java.util.ArrayList;

import com.googlecode.objectify.annotation.Embed;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
@Embed
public class GamePeriodData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	String date;

	ArrayList<Integer> RDs = new ArrayList<Integer>();
	ArrayList<Double> rs = new ArrayList<Double>();
	ArrayList<Double> s = new ArrayList<Double>();

	public GamePeriodData(){
		
	}
	
	public GamePeriodData(String date){
		this.date=date;
	}
	
	public ArrayList<Integer> getRDs(){
		return RDs;
	}
	
	public ArrayList<Double> getrs(){
		return rs;
	}

	public ArrayList<Double> gets(){
		return s;
	}

	
	public void addRD(int RD){
		RDs.add(RD);
	}
	
	public void addr(double r){
		rs.add(r);
	}
	
	public void adds(double ss){
		s.add(ss);
	}
	
	public String getDate(){
		return date;
	}
}
