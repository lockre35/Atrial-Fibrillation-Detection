package com.afib.graph;

import android.util.Log;

import com.afib.communication.Constants;

import org.achartengine.GraphicalView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Logan on 4/25/2015.
 */
public class GraphAnalyzer {
    //Text view for raw ouput
    private AnalyzeGraph AnalyzeGraph;
    private GraphicalView View;
    private Point CurrentPoint;
    private int StartSample;
    private int EndSample;
    private static int SamplesInASecond = 1000 / Constants.ACTION.DELAY_BETWEEN_INPUT;
    private String FileName;

    //Create a new thread by passing in information about the view, the line to display, and the context
    public GraphAnalyzer(AnalyzeGraph line, GraphicalView view, String fileName, int startTime, int endTime){
        super();
        this.AnalyzeGraph = line;
        this.View = view;
        this.StartSample = startTime * SamplesInASecond;
        this.EndSample = endTime * SamplesInASecond;
        this.FileName = fileName;
    }

    /**
     * Method used to build the graph view
     */
    public void BuildGraph()
    {
        try {
            File inputFile = new File(this.FileName);
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(inputFile));

            //Clear any existing data
            AnalyzeGraph.removeAllPoints();
            PointBuilder pointBuilder = new PointBuilder();

            int startX = this.StartSample;

            buf.skip(startX);
            while (startX < this.EndSample) {
                //Read 100 bytes
                byte[] bytes = new byte[100];
                buf.read(bytes, 0, 100);
                Log.i("GraphAnalyzer", "100 Bytes Read");

                //Add all points within those 100 bytes
                for(byte currentByte : bytes)
                {
                    //Update the graph
                    //Obtain a new point
                    Point p = pointBuilder.getDataFromReciever(startX, currentByte);

                    //Add the new point to the line
                    AnalyzeGraph.addNewPoints(p);

                    startX += 1;
                }
                //Update the graph view
                View.repaint();
            }
            buf.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
