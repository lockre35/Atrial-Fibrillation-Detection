package com.afib.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;

public class CSVParser {
	
	File file;
	InputStreamReader reader;
	BufferedReader in;
	public Context Context;
	
	public CSVParser(Context context){
			this.Context = context;
	       try {
	           //file = new File("scope_32.csv");
	           //file = this.Context.getAssets().open("scope_32.csv");
	    	   reader = new InputStreamReader(this.Context.getAssets().open("scope_32.csv"));
	           in = new BufferedReader(reader);
	           in.readLine();
	           in.readLine();
	       } catch (IOException e) {
	           e.printStackTrace();
	       }
	}
	
	
	
	public Frame getFrame()
	{
		Frame frame = new Frame();
		DataPoint p = new DataPoint(0,0);
		int start = 0;
		int end = 0;
		
		try {
           String string = in.readLine();
           if(string != null)
           {
        	   p = getPoint(string);
        	   start = p.getX();
        	   end = p.getX() + 100;
        	   frame.DataPoints.push(p);
           }
           else
           {
        	   return null;
           }
           
           while ((string = in.readLine()) != null && p.getX() < end) {
             p = getPoint(string);
             frame.DataPoints.push(p);
           }

         } catch (IOException e) {
           e.printStackTrace();
         }
		
		return frame;
	}
	
	public DataPoint getPoint(String line)
	{
		DataPoint p;
		int x;
		int y;
		
		String parts[] = line.split(".");
		x = Integer.parseInt(parts[0]);
		
		String parts2[] = parts[1].split(",");
		y = Integer.parseInt(parts2[1]);
		
		if(x == -1)
			x = -1000;
		if(x == 1)
			x = 1000;
		
		p = new DataPoint(x,y);
		
		return p;
	}
}
