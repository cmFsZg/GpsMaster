package org.gpsmaster;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.gpsmaster.UnitConverter.UNIT;
import org.gpsmaster.gpxpanel.GPXFile;
import org.gpsmaster.gpxpanel.GPXObject;
import org.gpsmaster.gpxpanel.Route;
import org.gpsmaster.gpxpanel.Track;
import org.gpsmaster.gpxpanel.Waypoint;
import org.gpsmaster.gpxpanel.WaypointGroup;

import com.topografix.gpx._1._1.LinkType;

import eu.fuegenstein.util.XTime;

/**
 * Table model containing properties of specified {@link GPXObject}
 * @author rfu
 *
 */
public class PropsTableModel extends DefaultTableModel {

	/*
	 * This TableModel holds three values for each row:
	 * 	1	Property Name as displayed in table (string)
	 * 	2	Property Value (object)
	 * 	3	is editable (boolean)
	 * 3rd column isn't displayed, for future (internal) use only
	 */

	/**
	 *
	 */
	private static final long serialVersionUID = -2702982954383747924L;
	private Timer timer;
	private long lastPropDisplay = 0;
	private int displayTime = 4; // default time on display for Trackpoints in seconds
	private GPXObject activeGPXObject = null;
	protected List<Integer> extensionIdx = new ArrayList<Integer>();
	private DateFormat sdf = null;
	private UnitConverter uc = new UnitConverter();
	private JTable myTable = null; // JTable using this model

    /**
     * custom cell renderer. renders extension properties in BLUE.
     */
    class propTableCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {

		/**
		 *
		 */
		private static final long serialVersionUID = 6876231432766928405L;

		@Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(null);
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setText(String.valueOf(value));
            if (extensionIdx.contains(row)) {
            	setForeground(Color.BLUE);
            } else {
            	setForeground(Color.BLACK);
            }
            return this;
        }
    }

    /* TIMER ACTION LISTENER
     * -------------------------------------------------------------------------------------------------------- */
    ActionListener actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent actionEvent) {
        	if (actionEvent.getSource() == timer) {
        		// un-display waypoint properties after a few seconds
        		if (lastPropDisplay != 0) {
        			if ((System.currentTimeMillis() - lastPropDisplay) > (displayTime * 1000)) {
        				updatePropsTable();
        				lastPropDisplay = 0;
        				timer.stop();
        			}
        		}
        	}
        }
    };


     /* Single click on the table when waypoint properties are displayed
      * stops the timer until another waypoint or GPXObject is set.
      * TODO show some kind of icon (pin) when propsdisplay is locked
      */
    MouseAdapter mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
    		if (timer.isRunning()) {
    			timer.stop();
    		}
        }
    };

	/**
	 * Default Constructor
	 */
	public PropsTableModel(UnitConverter converter) {
		super(new Object[]{"Name", "Value"},0);
		setColumnCount(2);
		uc = converter;
		// sdf = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

	    timer = new Timer(1000, actionListener);
	    timer.setInitialDelay(1000);

	}

	/**
	 * Set the {@link JTable} using this model
	 * @param table {@link JTable} using this model
	 */
	public void setTable(JTable table) {
		myTable = table;

        Enumeration<TableColumn> enumeration = myTable.getColumnModel().getColumns();
        while (enumeration.hasMoreElements()) {
        	TableColumn column = enumeration.nextElement();
        	column.setCellRenderer(new propTableCellRenderer());
        }

        myTable.addMouseListener(mouseListener);
	}

	/**
	 * sets for how long the Trackpoint properties are displayed
	 * @param seconds time on display in seconds
	 */
	public void setDisplayDuration(int seconds) {
		displayTime = seconds;
	}

	public int getDisplayDuration() {
		return displayTime;
	}

	/**
	 *
	 * @param gpx
	 */
	public void setGPXObject(GPXObject gpx) {
		if (timer.isRunning()) {
			timer.stop();
		}
		activeGPXObject = gpx;
		updatePropsTable();
	}

	/**
	 *
	 * @param trackpoint
	 */
	public void setTrackpoint(Waypoint trackpoint, int indexOf) {
		clear();
		propsDisplayTrackpoint(trackpoint, indexOf);
		lastPropDisplay = System.currentTimeMillis();
		timer.start();
	}

	/**
	 *
	 * @param unit
	 */
	public void setUnit(UnitConverter unit) {
		uc = unit;
	}

	// TODO edit properties: stop timer on editing waypoint properties


    /**
     *
     * @param links
     */
    private void propsDisplayLink(List<LinkType> links) {
    	for (LinkType link : links) {
    		// URL url = null;
    		String text = "link";
			if (link.getText() != null) {
				text = link.getText();
			}
			addRow(new Object[]{text, link.getHref()});
			/*
			try {
				url = new URL(link.getHref());
				addRow(new Object[]{text, url});
			} catch (MalformedURLException e) {
				addRow(new Object[]{text, "<malformed URL>"});
			}
			*/
    	}
    }

    /**
     *
     * @param extensions
     */
    private void propsDisplayExtensions(Hashtable<String, String> extensions) {

    	if (extensions.size() > 0) {
    		Iterator<String> i = extensions.keySet().iterator();
    		while (i.hasNext()) {
    			String key = i.next();
    			addRow(new Object[]{key, extensions.get(key), true});
        		extensionIdx.add(getRowCount()-1);
    		}
    	}
    }

    /**
     * Display properties which are common to all GPX objects
     * @param o
     */
    private void propsDisplayEssentials(GPXObject o) {

    	Date startTime = o.getStartTime();
    	Date endTime = o.getEndTime();

        if (startTime != null && endTime != null) {
            String startTimeString = "";
            String endTimeString = "";
            startTimeString = sdf.format(startTime);
            endTimeString = sdf.format(endTime);
            addRow(new Object[]{"start time", startTimeString, false});
            addRow(new Object[]{"end time", endTimeString, false});
        }

        if (o.getDuration() != 0) {
        	addRow(new Object[]{"duration", XTime.getDurationText(o.getDuration()), false});
        }
        /* don't display while still buggy
        if (o.getDurationExStop() != 0) {
        	addRow(new Object[]{"duration ex stop", getTimeString(o.getDurationExStop())});
        }
        */
        if (o.getLengthMeters() > 0) {
        	String distFormat = "%.2f ".concat(uc.getUnit(UNIT.KM));
        	double dist = uc.dist(o.getLengthMeters(), UNIT.KM);
            addRow(new Object[]{"distance", String.format(distFormat, dist), false});

            String speedFormat = "%.1f ".concat(uc.getUnit(UNIT.KMPH));
            double speed = uc.speed(o.getMaxSpeedKmph(), UNIT.KMPH);
            addRow(new Object[]{"max speed", String.format(speedFormat, speed), false});

            if (o.getDuration() > 0) {
            	double avgSpeed = uc.speed((dist / o.getDuration() * 3600000), UNIT.KMPH);
            	addRow(new Object[]{"avg speed", String.format(speedFormat, avgSpeed), false});
            }
            /* don't display while still buggy
            if (o.getDurationExStop() > 0) {
            	double avgSpeedEx = (dist / o.getDurationExStop() * 3600000);
            	addRow(new Object[]{"avg speed ex stop", String.format(speedFormat, avgSpeedEx)});
            }
			*/
        }
    }

    /**
     *
     * @param o
     */
    private void propsDisplayRiseFall(GPXObject o) {

    	String formatDist = "%.0f "+uc.getUnit(UNIT.M);
        double grossRise = uc.dist(o.getGrossRiseMeters(), UNIT.M);
        addRow(new Object[]{"gross rise", String.format(formatDist, grossRise), false});
        double grossFall = uc.dist(o.getGrossFallMeters(), UNIT.M);
        addRow(new Object[]{"gross fall", String.format(formatDist, grossFall), false});

		long riseTime = o.getRiseTime();
		if (riseTime > 0) {
			addRow(new Object[]{"rise time", XTime.getDurationText(riseTime), false});
		}
		long fallTime = o.getFallTime();
		if (fallTime > 0) {
			addRow(new Object[]{"fall time", XTime.getDurationText(fallTime), false});
		}

		String formatSpeed = "%.0f "+uc.getUnit(UNIT.MHR);
        double avgRiseSpeed = uc.speed((grossRise / riseTime) * 3600000, UNIT.MHR);
        if (Double.isNaN(avgRiseSpeed) || Double.isInfinite(avgRiseSpeed)) {
            avgRiseSpeed = 0;
        }
        if (avgRiseSpeed != 0) {
            addRow(new Object[]{"avg rise speed", String.format(formatSpeed, avgRiseSpeed), false});
        }
        double avgFallSpeed = uc.speed((grossFall / fallTime) * 3600000, UNIT.MHR);
        if (Double.isNaN(avgFallSpeed) || Double.isInfinite(avgFallSpeed)) {
            avgFallSpeed = 0;
        }
        if (avgFallSpeed != 0) {
            addRow(new Object[]{"avg fall speed", String.format(formatSpeed, avgFallSpeed), false});
        }
    }


    /**
     *
     * @param o
     */
    private void propsDisplayElevation(GPXObject o) {

    	double eleStart = uc.dist(o.getEleStartMeters(), UNIT.M);
    	String format = "%.0f "+uc.getUnit(UNIT.M);
    	if (eleStart > 0) {
    		addRow(new Object[]{"elevation (start)", String.format(format, eleStart), false});
    	}
    	double eleEnd = uc.dist(o.getEleEndMeters(), UNIT.M);
    	if (eleEnd > 0) {
    		addRow(new Object[]{"elevation (end)", String.format(format, eleEnd), false});
    	}
    	double eleMin = uc.dist(o.getEleMinMeters(), UNIT.M);
    	addRow(new Object[]{"min elevation", String.format(format, eleMin), false});
    	double eleMax = uc.dist(o.getEleMaxMeters(), UNIT.M);
    	addRow(new Object[]{"max elevation", String.format(format, eleMax), false});
    }

    /**
	 * displays the properties of a trackpoint
	 *
	 * @param wpt
	 */
	private void propsDisplayTrackpoint(Waypoint wpt, int indexOf) {
		clear();

		// mandatory
		if (indexOf > -1) {
			addRow(new Object[]{"trackpoint #", indexOf, false});
		}
		addRow(new Object[]{"latitude", wpt.getLat(), false});
		addRow(new Object[]{"longitude", wpt.getLon(), false});
		addRow(new Object[]{"elevation", wpt.getEle(), false}); // TODO: meters, unit conversion
		Date time = wpt.getTime();
		if (time != null) {
			addRow(new Object[]{"time", sdf.format(time), false});
		}
		// optional
		if (wpt.getSat() > 0) { addRow(new Object[]{"sat", wpt.getSat(), false}); }
		if (wpt.getHdop() > 0) { addRow(new Object[]{"hdop", wpt.getHdop(), false}); }
		if (wpt.getVdop() > 0) { addRow(new Object[]{"vdop", wpt.getVdop(), false}); }
		if (wpt.getPdop() > 0) { addRow(new Object[]{"pdop", wpt.getPdop(), false}); }
		// TODO: also support the remaining ones
		if (wpt.getName().isEmpty() == false) {
			addRow(new Object[]{"name", wpt.getName(), true});
		}
		if (wpt.getDesc().isEmpty() == false) {
			addRow(new Object[]{"desc", wpt.getDesc(), true});
		}
		if (wpt.getCmt().isEmpty() == false) {
			addRow(new Object[]{"comment", wpt.getCmt(), true});
		}
		propsDisplayLink(wpt.getLink());
		propsDisplayExtensions(wpt.getExtensions());
		lastPropDisplay = System.currentTimeMillis();
	}

	/**
     *
     * @param o
     */
    private void propsDisplayWaypointGrp(GPXObject o) {
    	WaypointGroup wptGrp = (WaypointGroup) o;
    	addRow(new Object[]{"name", wptGrp.getName(), true});
        addRow(new Object[]{"# of pts", wptGrp.getWaypoints().size(), false});
    }

    /**
     *
     * @param o
     */
    private void propsDisplayRoute(GPXObject o) {
    	Route route = (Route) o;
    	addRow(new Object[]{"name", route.getName(), true});
        addRow(new Object[]{"# of pts", route.getNumPts(), false});
    }
    /**
     *
     * @param o
     */
    private void propsDisplayTrack(GPXObject o) {

    	Track track = (Track) o;
    	if (track.getName() != null) {
    		addRow(new Object[]{"track name", track.getName(), true});
    	}
    	if (track.getDesc() != null) {
    		addRow(new Object[]{"desc", track.getDesc(), true});
    	}
    	if (track.getType() != null) {
    		addRow(new Object[]{"type", track.getType(), true});
    	}

    	if (track.getTracksegs().size() > 0) {
    		addRow(new Object[]{"segments", track.getTracksegs().size(), false});
    	}
    	if (track.getNumPts() > 0) {
    		addRow(new Object[]{"# of pts", track.getNumPts(), false});
    	}
        if (track.getNumber() != 0) {
            addRow(new Object[]{"track number", track.getNumber(), true}); // editable?
        }
        propsDisplayLink(track.getLink());
    }

    /**
     *
     * @param o
     */
    private void propsDisplayGpxFile(GPXObject o) {

    	GPXFile gpxFile = (GPXFile) o;
        addRow(new Object[]{"GPX name", gpxFile.getMetadata().getName(), true});
        if (gpxFile.getMetadata().getDesc() != null) {
            addRow(new Object[]{"GPX desc", gpxFile.getMetadata().getDesc(), true});
        }
        if (!gpxFile.getCreator().isEmpty()) {
        	addRow(new Object[]{"creator", gpxFile.getCreator()});
        }

        // if (!gpxFile.getMetadata().getLink().isEmpty()) {
        // addRow(new Object[]{"link", gpxFile.getLink()});
        // }
        String timeString = "";
        if (gpxFile.getMetadata().getTime() != null) {
            Date time = gpxFile.getMetadata().getTime();
            timeString = sdf.format(time);
        }
        addRow(new Object[]{"GPX time", timeString, false}); // show even if empty
        if (gpxFile.getRoutes().size() > 0) {
        	addRow(new Object[]{"# of routes", gpxFile.getRoutes().size(), false});
        }
        if (gpxFile.getTracks().size() > 0) {
        	addRow(new Object[]{"# of tracks", gpxFile.getTracks().size(), false});
        }
        if (gpxFile.getNumWayPts() > 0) {
        	addRow(new Object[]{"# of waypoints", gpxFile.getNumWayPts(), false});
        }
        if (gpxFile.getNumTrackPts() > 0) {
        	addRow(new Object[]{"# of trackpoints", gpxFile.getNumTrackPts(), false});
        }

    }

    /**
     * show properties of current GPX object in properties table
     */
    private void updatePropsTable() {
    	clear();
    	if (activeGPXObject != null) {

    		if (activeGPXObject.isGPXFile()) {
	    		propsDisplayGpxFile(activeGPXObject);
	            propsDisplayEssentials(activeGPXObject);
	            propsDisplayElevation(activeGPXObject);
	            propsDisplayRiseFall(activeGPXObject);
	            propsDisplayExtensions(activeGPXObject.getExtensions());
	    	} else if (activeGPXObject.isTrack()) {
	    		propsDisplayTrack(activeGPXObject);
	            propsDisplayEssentials(activeGPXObject);
	            propsDisplayElevation(activeGPXObject);
	            propsDisplayRiseFall(activeGPXObject);
	            propsDisplayExtensions(activeGPXObject.getExtensions());
	    	} else if (activeGPXObject.isRoute()) {
	    		propsDisplayRoute(activeGPXObject);
	    		// ...
	    		propsDisplayElevation(activeGPXObject);
	    	} else if (activeGPXObject.isTrackseg()) {
	    		propsDisplayWaypointGrp(activeGPXObject);
	    		propsDisplayEssentials(activeGPXObject);
	    		propsDisplayElevation(activeGPXObject);
	    		propsDisplayRiseFall(activeGPXObject);
	    		propsDisplayExtensions(activeGPXObject.getExtensions());
	    	} else if (activeGPXObject.isWaypointGroup()) {
	    		propsDisplayWaypointGrp(activeGPXObject);
	    		propsDisplayElevation(activeGPXObject);
	    		propsDisplayExtensions(activeGPXObject.getExtensions());
	    	}
    	}
    }

    /**
     *
     */
    public void clear() {
    	setRowCount(0);
    	extensionIdx.clear();
    }
}
