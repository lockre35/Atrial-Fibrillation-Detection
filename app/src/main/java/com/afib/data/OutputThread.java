package com.afib.data;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Logan on 4/21/2015.
 */
public class OutputThread extends Thread{

    public Stack<Frame> InputStack;
    private InputStream DataInputStream;
    private BufferedReader InputBufferedReader;
    private BlockingQueue<byte[]> DataQueue;

    private String OutputFileName;
    private File OutputFile;
    private Boolean OutputAppend = false;

    //Create a new thread by passing in information about the view, the line to display, and the context
    public OutputThread(String fileName, BlockingQueue<byte[]> dataQueue){
        super();
        this.DataQueue = dataQueue;
        this.OutputFileName = fileName;
        OutputFile = new File(fileName);
        if(OutputFile.exists())
            OutputAppend = true;
    }

    public void run(){
        try
        {
            FileOutputStream outputStream = new FileOutputStream(OutputFile, OutputAppend);
            while(!this.isInterrupted())
            {
                byte[] dataFromQueue;
                if((dataFromQueue = DataQueue.take()) == "Terminate Thread".getBytes())
                {
                    outputStream.close();
                    return;
                }
                else
                {
                    for(Byte singleByte : dataFromQueue)
                    {
                        outputStream.write(singleByte & 0xFF);
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
