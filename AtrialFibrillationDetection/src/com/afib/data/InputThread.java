package com.afib.data;

import java.util.Stack;

public class InputThread extends Thread{

	
	public Stack<Frame> InputStack;
	
	//Create a new thread by passing in information about the view, the line to display, and the context
	public InputThread(){
		super();
	}
	
	public void run(){
		CSVParser parser = new CSVParser();
		
		while(!this.isInterrupted())
		{
			Frame frame = parser.getFrame();
			if(frame!=null)
				InputStack.push(frame);
			else
				return;
		}
	}
}
