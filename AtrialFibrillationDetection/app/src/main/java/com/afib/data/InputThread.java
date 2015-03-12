package com.afib.data;

import java.util.Stack;

import android.content.Context;

public class InputThread extends Thread{

	
	public Stack<Frame> InputStack;
	public Context Context;
	
	//Create a new thread by passing in information about the view, the line to display, and the context
	public InputThread(Context context){
		super();
		this.Context = context;
	}
	
	public void run(){
		CSVParser parser = new CSVParser(this.Context);
		
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
