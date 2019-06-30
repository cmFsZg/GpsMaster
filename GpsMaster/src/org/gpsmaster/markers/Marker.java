package org.gpsmaster.markers;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.gpsmaster.GpsMaster;
import org.gpsmaster.gpxpanel.Waypoint;

/**
 * A Marker basically is a {@link Waypoint} with an icon.
 *  
 * @author rfu
 *
 */
public class Marker extends Waypoint {

	protected ImageIcon icon = null;
	protected Color iconColor = Color.BLUE;
	protected Color foregroundColor = Color.BLACK;
	protected Color backgroundColor = new Color(255, 255, 255, 192); // transparent white
	protected Font font = null;

	protected JLabel label = new JLabel();
	
	// label/marker positions
	public static final int ABOVE = 1;
	public static final int BELOW = 2;
	public static final int LEFT = 3;
	public static final int RIGHT = 4;
	public static final int CENTER = 5;

	protected final String resourcePath = "/org/gpsmaster/icons/markers/";
	
	protected int labelPosition = BELOW;
	protected int markerPosition = ABOVE;
	
	/**
	 * 
	 * @param lat Latitude
	 * @param lon Longitude
	 */
	public Marker(double lat, double lon) {
		super(lat, lon);
		setup();
	}
	
	/**
	 * 
	 * @param waypoint
	 */
	public Marker(Waypoint waypoint) {
		super(waypoint);
		setup();
	}
	
	/*
	 * PUBLIC PROPERTIES
	 */

	public ImageIcon getIcon() {
		return icon;
	}
	
	public void setIcon(ImageIcon icon) {
		this.icon = icon;
	}

	/**
	 * 
	 * @param filename
	 */
	public void setIcon(String filename) {
		icon = new ImageIcon(GpsMaster.class.getResource(resourcePath.concat(filename)));
	}

	/**
	 * 
	 * @return
	 */
	public Color getIconColor() {
		return iconColor;
	}
	
	/**
	 * Set the color of the icon, if possible.
	 * This is done by re-coloring the bitmap
	 * representing the icon. 
	 * @param color
	 */
	public void setIconColor(Color color) {
		iconColor = color;
	}

	
	/**
	 * 
	 * @return
	 */
	public Color getForeground() {
		return foregroundColor;
	}
	
	/**
	 * Set label text color
	 * @param color
	 */
	public void setForeground(Color color) {
		foregroundColor = color;
	}

	/**
	 * 
	 * @return
	 */
	public Color getBackground() {
		return backgroundColor;
	}
	
	/**
	 * Set label background color
	 * @param color
	 */
	public void setBackground(Color color) {
		backgroundColor = color;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getMarkerPosition() {
		return markerPosition;
	}
	
	/**
	 * Set the location of the marker in relation to its coordinates
	 */
	public void setMarkerPosition(int position) {
		// check range of param
		markerPosition = position;
	}

	/**
	 * 
	 * @return
	 */
	public int getLabelPosition() {
		return labelPosition;
	}

	/**
	 * Set the location of the label in relation to the marker
	 * 
	 * @param position
	 */
	public void setLabelPosition(int position) {
		// check range of param
		labelPosition = position;
	}
	/*
	 * PUBLIC METHODS
	 */
	
	/**
	 * 
	 * @param g2d
	 */
	public void paint(Graphics2D g2d, Point point) {
		Point iconPoint = new Point();
		Point labelPoint = new Point(); 

		if (icon == null) { // set default marker icon
			setIcon("default.png");
		}
		
		switch(markerPosition) {
		
		default:  // default = ABOVE
			iconPoint.x = point.x - (icon.getIconWidth() / 2);
			iconPoint.y = point.y - icon.getIconHeight() - 2;
			break;	
		}
		
		g2d.drawImage(icon.getImage(), iconPoint.x, iconPoint.y, null);

		if (name.isEmpty() == false) {
			
			FontMetrics metrics = g2d.getFontMetrics();
			Rectangle2D box = null;
			box = metrics.getStringBounds(name, g2d);
			
			switch(labelPosition) {
			default: // default = below
				labelPoint.x = point.x - (int) (box.getWidth() / 2);
				labelPoint.y = point.y + 2;
				break;
			}
			
			g2d.setColor(backgroundColor);
			g2d.fillRoundRect(
				labelPoint.x - 2, 
				labelPoint.y - 2, 
				(int) (box.getWidth() + 4), 
				(int) (box.getHeight() + 4), 
				2, 2);
					
			g2d.setColor(Color.BLACK);
			g2d.drawString(name, labelPoint.x, (int) (labelPoint.y + box.getHeight() - 1));
		}

	}
	
	protected void setup() {
		// load default icon
	}
}
