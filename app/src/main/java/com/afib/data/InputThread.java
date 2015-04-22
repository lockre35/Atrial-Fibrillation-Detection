package com.afib.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;

import android.content.Context;
import android.util.Log;

public class InputThread extends Thread{

	public Stack<Frame> InputStack;
	public Context Context;
    private InputStream DataInputStream;
    private BufferedReader InputBufferedReader;
    private BlockingQueue<byte[]> DataQueue;
	
	//Create a new thread by passing in information about the view, the line to display, and the context
	public InputThread(Context context, BlockingQueue<byte[]> dataQueue){
		super();
		this.Context = context;
        this.DataQueue = dataQueue;
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

    public void openInputFile(String fileName)
    {
        try {
            DataInputStream = Context.getAssets().open(fileName);
            InputBufferedReader = new BufferedReader(new InputStreamReader(DataInputStream));
        }
        catch (Exception e) {
            Log.e("Exception", "Failed to open input file");
        }
    }
}
