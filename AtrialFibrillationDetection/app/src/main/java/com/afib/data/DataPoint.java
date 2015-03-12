package com.afib.data;

public class DataPoint {
	
	private int x;
	private int y;
	
	//Just a simple implementation of a point object
	public DataPoint(int x, int y)
	{
		this.x = x;
		this.y = y;	
	}
	
	//Obtain x value of the point
	public int getX(){
		return x;
	}
	
	//Obtain y value of the point
	public int getY(){
		return y;
	}
}
