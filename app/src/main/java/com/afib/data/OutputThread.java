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

import flanagan.analysis.CurveSmooth;


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
            double[] input = new double[1000];
            int i = 0;
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
                        int temp = singleByte & 0xFF;
                        input[i] = temp;
                        i++;
                        //outputStream.write(singleByte & 0xFF);
                        if(i > 999)
                        {
                            CurveSmooth csm = new CurveSmooth(input);
                            double[] smoothedData = csm.savitzkyGolay(40);
                            for(double singleDouble : smoothedData)
                            {
                                byte storedByte = (byte) singleDouble;
                                outputStream.write(storedByte & 0xFF);
                            }
                            i = 0;
                            input = new double[1000];
                        }
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
