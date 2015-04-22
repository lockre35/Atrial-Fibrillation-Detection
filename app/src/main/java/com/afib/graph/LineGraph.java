package com.afib.graph;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;


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
		mRenderer.setXAxisMax(1000);
		mRenderer.setXAxisMin(0);
		mRenderer.setYAxisMax(300);
		mRenderer.setYAxisMin(0);
		double[] initialRange = { 
			    0, 100, 0, 255
			};
		mRenderer.setInitialRange(initialRange);
		mRenderer.setPanEnabled(false, false);
        mRenderer.setZoomEnabled(false, false);
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
