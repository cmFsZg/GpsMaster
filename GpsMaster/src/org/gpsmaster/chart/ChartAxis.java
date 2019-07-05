package org.gpsmaster.chart;

import javax.swing.ImageIcon;

import org.gpsmaster.GpsMaster;
import org.gpsmaster.UnitConverter;
import org.gpsmaster.gpxpanel.Waypoint;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.ui.RectangleInsets;

/**
 * Base class representing a chart axis, containing
 * GpsMaster- and JFreeChart specific components.
 *
 * @author rfu
 *
 */
public abstract class ChartAxis {

	protected UnitConverter uc = null;
	protected ValueAxis valueAxis = null;
	protected String title = null;

	public abstract double getValue(Waypoint wpt);
	public abstract void reset();

	protected ImageIcon icon = null;
	protected String iconPath = "/org/gpsmaster/icons/chart/";
	protected String iconFile = null;


	/**
	 * Default constructor
	 */
	public ChartAxis(UnitConverter uc) {
		this.uc = uc;
	}



	/**
	 *
	 * @return
	 */
	public UnitConverter getUnit() {
		return uc;
	}

	/**
	 *
	 * @param uc
	 */
	public void setUnit(UnitConverter uc) {
		this.uc = uc;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 *
	 * @return
	 */
	protected String getLabel() {
		return valueAxis.getLabel();
	}

	/**
	 *
	 * @param label
	 */
	protected void setLabel(String label) {
		valueAxis.setLabel(label);
	}

	/**
	 *
	 * @return
	 */
	public ValueAxis getValueAxis() {
		return valueAxis;
	}

	/**
	 * Get the icon that represents this axis
	 * @return
	 */
	public ImageIcon getIcon() {
		if (icon == null) {
			icon = new ImageIcon(GpsMaster.class.getResource(iconPath.concat(iconFile)));
		}
		return icon;
	}

	/**
	 * set defaults specific for all subclasses/axes
	 */
	protected void setDefaults() {
		valueAxis.setLabelInsets(new RectangleInsets(5, 5, 5, 5));

	}
}
