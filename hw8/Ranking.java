package org.zhihanli.hw8;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Ranking {

	private ArrayList<Integer> RDs;
	private ArrayList<Double> rs;
	private ArrayList<Double> s;
	private double r;
	private double RD0;
	private int t;
	private double q;

	private double RD;
	private double dSquare;

	public static void main(String args[]) {
		ArrayList<Integer> RDs = new ArrayList<Integer>();
		ArrayList<Double> rs = new ArrayList<Double>();
		ArrayList<Double> s = new ArrayList<Double>();

		RDs.add(30);
		RDs.add(100);
		RDs.add(300);

		rs.add(1400.0);
		rs.add(1550.0);
		rs.add(1700.0);

		s.add(1.0);
		s.add(0.0);
		s.add(0.0);
		Ranking ranking = new Ranking(RDs, rs, s, 1500, 200, 1);
		System.out.println(ranking.computeNewRanking());
		System.out.println(ranking.computeNewRD());

		Date currentDate = new Date();
		DateFormat dtf = DateFormat.getDateInstance();
		String currentDateString = dtf.format(currentDate);
		System.out.println(currentDateString);

		Date lastCompDate = new Date();
		
		String lastCompDateString = dtf.format(lastCompDate);
		System.out.println(lastCompDateString);

		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		lastCompDate.setDate(1);
		calendar1.setTime(lastCompDate);
		calendar2.setTime(currentDate);

		long milliseconds1 = calendar1.getTimeInMillis();

		long milliseconds2 = calendar2.getTimeInMillis();
		long diff = milliseconds2 - milliseconds1;
		long diffDays = diff / (24 * 60 * 60 * 1000);

		int daysPassed = (int) (diff / (24 * 60 * 60 * 1000));
		System.out.println(milliseconds1 + " " + milliseconds2 + " "
				+ (daysPassed) + " " + diffDays);
	}

	public Ranking(ArrayList<Integer> RDs, ArrayList<Double> rs,
			ArrayList<Double> s, double r, double RD0, int t) {
		this.RDs = RDs;
		this.rs = rs;
		this.s = s;
		this.r = r;
		this.RD0 = RD0;
		this.t = t;

		q = Math.log(10) / 400;
		dSquare = dSquare();
		RD = computeRD();
		// FOR test
		// RD=200;
	}

	public double computeRD() {
		double c = Math.sqrt((350 * 350 - 50 * 50) / 365);
		return Math.min(Math.sqrt(RD0 * RD0 + c * c * t), 350);
	}

	public double computeNewRanking() {

		double rightSum = 0;
		for (int i = 0; i < RDs.size(); i++) {
			double RDi = RDs.get(i);
			rightSum += g(RDi) * (s.get(i) - E(g(RDi), r, rs.get(i)));
		}

		double rPrime = r + q / (1 / (RD * RD) + 1 / dSquare) * rightSum;

		return rPrime;

	}

	private double g(double RDi) {
		return 1 / (Math.sqrt(1 + 3 * q * q * RDi * RDi / (Math.PI * Math.PI)));
	}

	private double E(double gRDi, double r, double ri) {
		return 1 / (1 + Math.pow(10, (gRDi * (r - ri) / -400)));
	}

	private double dSquare() {
		double result = 0;

		for (int i = 0; i < RDs.size(); i++) {
			double RDi = RDs.get(i);
			double ri = rs.get(i);
			double gRDi = g(RDi);
			double E = E(gRDi, r, ri);
			result += q * q * gRDi * gRDi * E * (1 - E);
		}
		return 1 / result;
	}

	public double computeNewRD() {

		return Math.sqrt(1 / (1 / (RD * RD) + 1 / dSquare));
	}
}
