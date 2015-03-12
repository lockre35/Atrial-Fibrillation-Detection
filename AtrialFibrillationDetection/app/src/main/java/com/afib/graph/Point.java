package com.afib.graph;

public class Point {
	
	private int x;
	private int y;
	
	//Just a simple implementation of a point object
	public Point(int x, int y)
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
