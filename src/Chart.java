
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
  
public class Chart {
	/**
	 * create JFreeChart Line Chart
	 */

	public static JFreeChart createChart(CategoryDataset categoryDataset) {
		JFreeChart jfreechart = ChartFactory.createLineChart("a", "b", "c",
				categoryDataset, // dataset
				PlotOrientation.VERTICAL, true, // legend
				false, // tooltips
				false); // URLs
		CategoryPlot plot = (CategoryPlot) jfreechart.getPlot();
		plot.setBackgroundAlpha(0.5f);
		plot.setForegroundAlpha(0.5f);
		LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
				.getRenderer();
		renderer.setBaseShapesVisible(true); 
		renderer.setBaseLinesVisible(true); 
		renderer.setUseSeriesOffset(true); 
		renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
		renderer.setBaseItemLabelsVisible(true);
		return jfreechart;
	}

	/**
	 * create CategoryDataset object
	 * 
	 */
	public static CategoryDataset createDataset() {
		String[] rowKeys = { "pixel" };
		String[] colKeys = new String[30];
		for (int i = 0; i < 30; i += 1)
			colKeys[i] = Integer.toString(30 * i);
		double[][] data = { { 4, 3, 1, 1, 1, 1, 2, 2, 2, 1, 8, 2, 1, 1, 2, 3,
				4, 5, 6, 7, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, };
		return DatasetUtilities.createCategoryDataset(rowKeys, colKeys, data);
	}
}
