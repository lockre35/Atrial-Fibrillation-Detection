package com.afib.graph;

import java.util.Random;

public class TestData {

	// x is the data number (not auto generated)
	public static Point getDataFromReciever(int x)
	{
		return new Point(x, generateRandomData(x));
	}
	
	// generate y values based on the sine wave
	private static int generateRandomData(double x)
	{
		/*
		Random random = new Random();
		return random.nextInt(5);
		*/
		
		//Need to modify values since the graph expects int's, not double's
		//(Will likely need to modify the graph to support doubles)
		double y = Math.sin(x/100);
		y = y*100;
		return (int) Math.round(y);
	}
}
