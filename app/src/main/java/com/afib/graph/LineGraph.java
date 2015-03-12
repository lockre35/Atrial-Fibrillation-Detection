package com.afib.graph;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;


public class LineGraph {
	
	private GraphicalView view;

	//Data to display on line
	private TimeSeries dataset = new TimeSeries("ECG");
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	
	//Values needed to create a graph
	private XYSeriesRenderer renderer = new XYSeriesRenderer();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	
	public LineGraph()
	{		
		//Add single dataset to multiple dataset
		mDataset.addSeries(dataset);
		
		//Customization for line
		renderer.setColor(Color.RED);
		renderer.setFillPoints(false);
		renderer.setLineWidth(6);
		
		//Set attributes for the entire graph
		mRenderer.addSeriesRenderer(renderer);
		mRenderer.setXAxisMax(3000);
		mRenderer.setXAxisMin(0);
		mRenderer.setYAxisMax(400);
		mRenderer.setYAxisMin(-400);
		double[] initialRange = { 
			    0, 3000, -400, 400
			};
		mRenderer.setInitialRange(initialRange);
		mRenderer.setPanEnabled(false, false);
		mRenderer.setAxisTitleTextSize(6);
		mRenderer.setAxesColor(Color.BLACK);
		mRenderer.setGridColor(Color.GRAY);
		mRenderer.setYLabelsColor(0,Color.BLACK);
		mRenderer.setXLabelsColor(Color.BLACK);
		mRenderer.setYAxisAlign(Align.LEFT, 0);
		mRenderer.setShowGrid(true);
		mRenderer.setBackgroundColor(Color.LTGRAY);
		//mRenderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));
		mRenderer.setMarginsColor(Color.LTGRAY);
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
