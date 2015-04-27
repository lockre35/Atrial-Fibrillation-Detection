package com.afib.graph;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;

import com.afib.communication.Constants;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;


public class AnalyzeGraph {

	private GraphicalView view;

	//Data to display on line
	private TimeSeries dataset = new TimeSeries("ECG");
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    private int SamplesInASecond = 1000 / Constants.ACTION.DELAY_BETWEEN_INPUT;

	//Values needed to create a graph
	private XYSeriesRenderer renderer = new XYSeriesRenderer();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

	public AnalyzeGraph(double minX)
	{		
		//Add single dataset to multiple dataset
		mDataset.addSeries(dataset);
		
		//Customization for line
		renderer.setColor(Color.RED);
		renderer.setFillPoints(false);
		renderer.setLineWidth(6);
		
		//Set attributes for the entire graph
		mRenderer.addSeriesRenderer(renderer);
		mRenderer.setYAxisMax(300);
		mRenderer.setYAxisMin(0);
        mRenderer.setXAxisMin(minX*SamplesInASecond);
		double[] initialRange = { 
			    minX, minX + 10, 0, 255
			};
        double[] panLimits = {
                0, Integer.MAX_VALUE, 0, 300
        };
        //mRenderer.setPanLimits(panLimits);
        //mRenderer.setZoomLimits(panLimits);
		//mRenderer.setInitialRange(initialRange);
		mRenderer.setPanEnabled(true, false);
        mRenderer.setZoomEnabled(true, false);
		mRenderer.setAxisTitleTextSize(6);
		mRenderer.setAxesColor(Color.BLACK);
		mRenderer.setGridColor(Color.GRAY);
		mRenderer.setYLabelsColor(0,Color.BLACK);
		mRenderer.setXLabelsColor(Color.BLACK);
		//mRenderer.setYAxisAlign(Align.LEFT, 0);
        mRenderer.setYLabelsAlign(Align.RIGHT);
        mRenderer.setYLabelsPadding(5);
		mRenderer.setShowGrid(true);
		//mRenderer.setBackgroundColor(Color.LTGRAY);
        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setBackgroundColor(Color.TRANSPARENT);
		mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
        mRenderer.setShowLegend(false);
		//mRenderer.setMarginsColor(Color.WHITE);
	}
	
	public GraphicalView getView(Context context)
	{
		//Generate the view
		view = ChartFactory.getLineChartView(context, mDataset, mRenderer);
		return view;
	}
	
	public void addNewPoints(Point p)
	{
		//Add a point
		dataset.add(p.getX(), p.getY());
	}
	
	public void removeAllPoints()
	{
		//Remove all points
		dataset.clear();
	}
}
