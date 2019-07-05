package org.gpsmaster.gpxpanel;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.ImageIcon;

import org.gpsmaster.GpsMaster;
import org.gpsmaster.UnitConverter;
import org.gpsmaster.markers.Marker;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;

import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import eu.fuegenstein.messagecenter.MessageCenter;


// import org.gpsmaster.ClickableMarker;

/**
 *
 * An extension of {@link JMapViewer} to include the display of GPX elements and related functionality.
 *
 * @author Matt Hoover
 *
 */
@SuppressWarnings("serial")
public class GPXPanel extends JMapViewer {

    private List<GPXFile> gpxFiles;

    private Image imgPathStart;
    private Image imgMarkerPt;
    private Image imgPathEnd;
    private Image imgCrosshair;
    private double crosshairLat;
    private double crosshairLon;
    private float trackLineWidth = 3;
    private boolean showCrosshair;
    private boolean paintBorder = true;
    private Point shownPoint;
    private Color activeColor;

    private MouseAdapter mouseAdapter = null;
    private MessageCenter msg = null;
    private LabelPainter labelPainter = null;
    private ReentrantLock gpxFilesLock = new ReentrantLock(); // lock for central List<GPXFile>
    private List<Marker> markerPoints;

    private final long lockTimeout = 5;

    /**
     * Constructs a new {@link GPXPanel} instance.
     */
    public GPXPanel(UnitConverter converter, MessageCenter msg) {
        super(new MemoryTileCache(), 8);
        this.setTileSource(new OsmTileSource.Mapnik());
        DefaultMapController mapController = new DefaultMapController(this);
        mapController.setDoubleClickZoomEnabled(false);
        mapController.setMovementEnabled(true);
        mapController.setWheelZoomEnabled(true);
        mapController.setMovementMouseButton(MouseEvent.BUTTON1);
        this.setScrollWrapEnabled(false); // TODO make everything work with wrapping?
        this.setZoomButtonStyle(ZOOM_BUTTON_STYLE.VERTICAL);
        this.msg = msg;
        gpxFiles = new ArrayList<GPXFile>();
        labelPainter = new LabelPainter(this, converter);

        imgPathStart = new ImageIcon(GpsMaster.class.getResource("/org/gpsmaster/icons/markers/path-start.png")).getImage();
        imgPathEnd = new ImageIcon(GpsMaster.class.getResource("/org/gpsmaster/icons/markers/path-end.png")).getImage();
        imgCrosshair = new ImageIcon(GpsMaster.class.getResource("/org/gpsmaster/icons/crosshair-map.png")).getImage();

        // markers = new Hashtable<Waypoint, MeasureMarker>();
        markerPoints = new ArrayList<Marker>();

        mouseAdapter = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				checkMarkerClick(e);
			}
		};
		addMouseListener(mouseAdapter);

		setLayout(new BorderLayout());
    }

    public List<GPXFile> getGPXFiles() {
        return gpxFiles;
    }

    public void setCrosshairLat(double crosshairLat) {
        this.crosshairLat = crosshairLat;
    }

    public void setCrosshairLon(double crosshairLon) {
        this.crosshairLon = crosshairLon;
    }

    public void setShowCrosshair(boolean showCrosshair) {
        this.showCrosshair = showCrosshair;
    }

    public Point getShownPoint() {
        return shownPoint;
    }

    public void setShownPoint(Point shownPoint) {
        this.shownPoint = shownPoint;
    }


    public float getTrackLineWidth() {
		return trackLineWidth;
	}

	public void setLineWidth(float trackLineWidth) {
		this.trackLineWidth = trackLineWidth;
	}

	public Color getActiveColor() {
        return activeColor;
    }

    public void setActiveColor(Color activeColor) {
        this.activeColor = activeColor;
    }

    // pass-through properties for label painter

    public void setProgressType(ProgressType show) {
        labelPainter.setProgressType(show);
    }

    public ProgressType getProgressType() {
    	return labelPainter.getProgressType();
    }

    public void setArrowType(ArrowType type) {
    	labelPainter.setArrowType(type);
    }

    public ArrowType getArrowType() {
    	return labelPainter.getArrowType();
    }

    /**
     * Get the list of arbitrary markers which are displayed on the
     * map at their respective locations, but are not intended to
     * be kept in a {@link GPXFile}
     *
     * @return List of current Markers
     */
    public List<Marker> getMarkers() {
    	return markerPoints;
    }

	public ReentrantLock getGpxFilesLock() {
		return gpxFilesLock;
	}

	public void setGpxFilesLock(ReentrantLock lock) {
		this.gpxFilesLock = lock;
	}



    /**
     * Adds the chosen {@link GPXFile} to the panel.
     * (thread safe)
     */
    public void addGPXFile(GPXFile gpxFile) {
    	try {
			if (gpxFilesLock.tryLock(lockTimeout, TimeUnit.SECONDS)) {
				gpxFiles.add(gpxFile);
			}
		} catch (InterruptedException e) {
			msg.error("addGPXFile: Unable to acquire lock:", e);
		} finally {
			gpxFilesLock.unlock();
			repaint();
		}
    }

    /**
     * Removes the chosen {@link GPXFile} to the panel.
     * (thread safe)
     */
    public void removeGPXFile(GPXFile gpxFile) {
    	try {
			if (gpxFilesLock.tryLock(lockTimeout, TimeUnit.SECONDS)) {
				gpxFiles.remove(gpxFile);
			}
		} catch (InterruptedException e) {
			msg.error("removeGPXFile: Unable to acquire lock:", e);
		} finally {
			gpxFilesLock.unlock();
			repaint();
		}
    }


    @Override
    protected synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        try {
			if (gpxFilesLock.tryLock(lockTimeout, TimeUnit.SECONDS)) {
		        paintFiles(g2d, gpxFiles);
			}
		} catch (InterruptedException e) {
			msg.volatileError("Paint", e);
		} finally {
			gpxFilesLock.unlock();
		}

        if (markerPoints.size() > 0) {
        	paintMarkers(g2d);
        }
        if (showCrosshair) {
            Point p = null;
            if (crosshairLon > -180) { // hack fix for bug in JMapViewer.getMapPosition
                p = this.getMapPosition(new Coordinate(crosshairLat, crosshairLon), false);
            } else {
                p = this.getMapPosition(new Coordinate(crosshairLat, -180), false);
            }
            int offset = imgCrosshair.getWidth(null) / 2;
            g2d.drawImage(imgCrosshair, p.x - offset, p.y - offset, null);
        }
        if (shownPoint != null) {
            Stroke saveStroke = g2d.getStroke();
            Color saveColor = g2d.getColor();

            // square mark (with transparency)
            g2d.setColor(Color.black);
            g2d.drawRect(shownPoint.x - 9, shownPoint.y - 9, 17, 17);
            g2d.setColor(Color.white);
            g2d.drawRect(shownPoint.x - 8, shownPoint.y - 8, 15, 15);
            g2d.setColor(Color.black);
            g2d.drawRect(shownPoint.x - 7, shownPoint.y - 7, 13, 13);
            int red = activeColor.getRed();
            int green = activeColor.getGreen();
            int blue = activeColor.getBlue();
            AlphaComposite ac = AlphaComposite.SrcOver;
            g2d.setComposite(ac);
            g2d.setColor(new Color(255 - red, 255 - green, 255 - blue, 160));
            g2d.fill(new Rectangle(shownPoint.x - 6, shownPoint.y - 6, 11, 11));

            g2d.setStroke(saveStroke);
            g2d.setColor(saveColor);
        }
    }

    /**
     * Paints each file.
     */
    private void paintFiles(Graphics2D g2d, List<GPXFile> files) {
        // TODO implement lock
    	for (GPXFile file: files) {
            if (file.isVisible()) {
                for (Route route : file.getRoutes()) {
                    if (route.isVisible()) {
                        paintPath(g2d, route.getPath());
                    }
                }
                for (Track track : file.getTracks()) {
                    if (track.isVisible()) {
                        for (WaypointGroup path : track.getTracksegs()) {
                            if (path.isVisible()) {
                                paintPath(g2d, path);
                                // paintColoredPath(g2d, path); // RFU
                                labelPainter.paint(g2d, path);
                            }
                        }
                    }
                }

            	if (file.getWaypointGroup().isVisible())
            	{
            		paintWaypointGroup(g2d, file.getWaypointGroup());
            	}
                if (file.isWptsVisible()) {
                    for (Route route : file.getRoutes()) {
                        if (route.isWptsVisible() && route.isVisible()) {
                            paintPathpointGroup(g2d, route.getPath());
                        }
                    }
                    for (Track track : file.getTracks()) {
                        if (track.isWptsVisible() && track.isVisible()) {
                            for (WaypointGroup wptGrp : track.getTracksegs()) {
                                paintPathpointGroup(g2d, wptGrp);
                            }
                        }
                    }
                }
                for (Route route : file.getRoutes()) {
                    if (route.isVisible()) {
                        paintStartAndEnd(g2d, route.getPath());
                    }
                }
                for (Track track : file.getTracks()) {
                    if (track.isVisible()) {
                        for (WaypointGroup path : track.getTracksegs()) {
                            if (path.isVisible()) {
                                paintStartAndEnd(g2d, path);
                            }
                        }
                    }
                }
            }
        }
    }




    /**
     * paints a path with colored segments
     * @param g2d
     * @param waypointPath
     */
    private void paintColoredPath(Graphics2D g2d, WaypointGroup waypointPath) {
        Point maxXY = getMapPosition(waypointPath.getMinLat(), waypointPath.getMaxLon(), false);
        Point minXY = getMapPosition(waypointPath.getMaxLat(), waypointPath.getMinLon(), false);
        if (maxXY.x < 0 || maxXY.y < 0 || minXY.x > getWidth() || minXY.y > getHeight()) {
            return; // don't paint paths that are completely off screen
        }

        if (waypointPath.getNumPts() >= 2) {
            g2d.setColor(waypointPath.getColor());
            List<Waypoint> waypoints = waypointPath.getWaypoints();
            g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            GeneralPath path = new GeneralPath();
            Waypoint wpt = waypointPath.getStart();

            Point point = getMapPosition(wpt.getLat(), wpt.getLon(), false);
            path.moveTo(point.x, point.y);
            Point prev = point;
            for (int i = 1; i < waypoints.size(); i++) {
                wpt = waypoints.get(i);
                if (wpt.getSegmentColor() != null) {
                	g2d.draw(path);
                	path.reset();
                	path.moveTo(prev.x, prev.y);
                	g2d.setColor(wpt.getSegmentColor());
                }

                point = getMapPosition(wpt.getLat(), wpt.getLon(), false);
                path.lineTo(point.x, point.y);
                prev = point;
            }
            g2d.draw(path);
        }
    }

    /**
     * Paints a single path contained in a {@link WaypointGroup}.
     */
    private  void paintPath(Graphics2D g2d, WaypointGroup waypointPath) {
        Point maxXY = getMapPosition(waypointPath.getMinLat(), waypointPath.getMaxLon(), false);
        Point minXY = getMapPosition(waypointPath.getMaxLat(), waypointPath.getMinLon(), false);
        if (maxXY.x < 0 || maxXY.y < 0 || minXY.x > getWidth() || minXY.y > getHeight()) {
            return; // don't paint paths that are completely off screen
        }

        g2d.setColor(waypointPath.getColor());
        if (waypointPath.getNumPts() >= 2) {
            List<Waypoint> waypoints = waypointPath.getWaypoints();
            GeneralPath path;
            Waypoint rtept;
            Point point;

            Stroke saveStroke = g2d.getStroke();
            Color saveColor = g2d.getColor();

            path = new GeneralPath();
            rtept = waypointPath.getStart();
            point = getMapPosition(rtept.getLat(), rtept.getLon(), false);
            path.moveTo(point.x, point.y);
            Point prev = point;
            for (int i = 1; i < waypoints.size(); i++) {
                rtept = waypoints.get(i);
                point = getMapPosition(rtept.getLat(), rtept.getLon(), false);
                if (point.equals(prev) == false) { // performance improvement?
                	path.lineTo(point.x, point.y);
                }
                prev = point;
            }

            // don't paint track background = border when segment color is transparent
            if ((paintBorder) && waypointPath.getColor().getAlpha() == 255) {
                // draw black border
                g2d.setStroke(new BasicStroke(trackLineWidth + 2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.setColor(Color.BLACK);
            	g2d.draw(path);
            }

            // draw colored route
            g2d.setStroke(new BasicStroke(trackLineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.setColor(waypointPath.getColor());
            g2d.draw(path);
            g2d.setStroke(saveStroke);
        }
    }

    /**
     * Paints the waypoints for a path in {@link WaypointGroup}.
     */
    private  void paintPathpointGroup(Graphics2D g2d, WaypointGroup wptGrp) {
        if (wptGrp.isVisible() && wptGrp.isWptsVisible()) {
        	g2d.setColor(Color.BLACK);
            List<Waypoint> wpts = wptGrp.getWaypoints();
            for (Waypoint wpt : wpts) {
                Point point = getMapPosition(wpt.getLat(), wpt.getLon(), false);
                if (getBounds().contains(point)) {
                	g2d.drawOval(point.x-2, point.y-2, 4, 4);
                }
            }
        }
        // System.out.println(String.format("%d %d %d %d", getBounds().x, getBounds().y, getBounds().width, getBounds().y));
    }

    /**
     * paint a single {@link Marker} on the map
     * @param g2d
     * @param marker
     */
    private void paintMarker(Graphics2D g2d, Marker marker) {
        Point point = getMapPosition(marker.getLat(), marker.getLon(), false);
        g2d.drawOval(point.x - 2, point.y - 2, 4, 4);
        marker.paint(g2d, point);
    }

    /**
     * Paints the waypoints in {@link WaypointGroup} as markers.
     */
     private void paintWaypointGroup(Graphics2D g2d, WaypointGroup wptGrp) {
    	 if (wptGrp.isVisible() && wptGrp.isWptsVisible()) {
            for (Waypoint wpt : wptGrp.getWaypoints()) {
                paintMarker(g2d, (Marker) wpt);
            }
        }
    }

    /**
     * Paints the start/end markers of a {@link Route} or {@link Track}.
     */
    private  void paintStartAndEnd(Graphics2D g2d, WaypointGroup waypointPath) {
        if (waypointPath.getNumPts() >= 2) {
            Waypoint rteptEnd = waypointPath.getEnd();
            Point end = getMapPosition(rteptEnd.getLat(), rteptEnd.getLon(), false);
            g2d.setColor(Color.BLACK);
            g2d.drawImage(imgPathEnd, end.x - 9, end.y - 28, null);
        }

        if (waypointPath.getNumPts() >= 1) {
            Waypoint rteptStart = waypointPath.getStart();
            Point start = getMapPosition(rteptStart.getLat(), rteptStart.getLon(), false);
            g2d.setColor(Color.BLACK);
            g2d.drawImage(imgPathStart, start.x - 9, start.y - 28, null);
        }
    }

    /**
     * paints all {@link Waypoint} in
     *
     *  @author rfuegen
     */
    private void paintMarkers(Graphics2D g2d) {

     	for (Marker marker : markerPoints) {
     		paintMarker(g2d, marker);
    	}
    }

    /**
     * checks if a marker was clicked
     * and fires PropertyChangeEvent if applicable.
     * only the first matching marker will be considered.
     *
     */
    private void checkMarkerClick(MouseEvent e) {
    	for (Marker marker : markerPoints) {
			if (marker.contains(e.getPoint())) { // redundant code, consolidate
				firePropertyChange(e.getClickCount() + "click", null, marker);
				return;
			}
    	}
    	for (GPXFile gpx : gpxFiles) {
    		for (Waypoint wpt : gpx.getWaypointGroup().getWaypoints()) {
    			Marker marker = (Marker) wpt;
    			if (marker.contains(e.getPoint())) {  // redundant code, consolidate
    				firePropertyChange(e.getClickCount() + "click", null, marker);
    				return;
    			}
    		}
    	}
    }

    /**
     * Centers the {@link GPXObject} and sets zoom for best fit to panel.
     */
    public void fitGPXObjectToPanel(GPXObject gpxObject) {
        int maxZoom = tileController.getTileSource().getMaxZoom();
        int xMin = OsmMercator.LonToX(gpxObject.getMinLon(), maxZoom);
        int xMax = OsmMercator.LonToX(gpxObject.getMaxLon(), maxZoom);
        int yMin = OsmMercator.LatToY(gpxObject.getMaxLat(), maxZoom); // screen y-axis positive is down
        int yMax = OsmMercator.LatToY(gpxObject.getMinLat(), maxZoom); // screen y-axis positive is down

        if (xMin > xMax || yMin > yMax) {
            //setDisplayPositionByLatLon(36, -98, 4); // U! S! A!
        } else {
            int width = Math.max(0, getWidth());
            int height = Math.max(0, getHeight());
            int zoom = maxZoom;
            int x = xMax - xMin;
            int y = yMax - yMin;
            while (x > width || y > height) {
                zoom--;
                x >>= 1;
                y >>= 1;
            }
            x = xMin + (xMax - xMin) / 2;
            y = yMin + (yMax - yMin) / 2;
            int z = 1 << (maxZoom - zoom);
            x /= z;
            y /= z;
            setDisplayPosition(x, y, zoom);
        }

    }

    /**
     *
     * @return
     */
    public GeoBounds getVisibleBounds() {

    	GeoBounds bounds = new GeoBounds();
    	Point center = getCenter();

		bounds.setW(OsmMercator.XToLon(center.x - getWidth() / 2, getZoom()));
		bounds.setN(OsmMercator.YToLat(center.y - getHeight() / 2, getZoom()));
		bounds.setE(OsmMercator.XToLon(center.x + getWidth() / 2, getZoom()));
		bounds.setS(OsmMercator.YToLat(center.y + getHeight() / 2, getZoom()));

    	return bounds;
    }

    /*
	@Override public void paint(Graphics g, MapView mv) {
		boolean clickedFound = false;
		for (ImageEntry e : data) {
			if (e.pos != null) {
				Point p = mv.getPoint(e.pos);
				Rectangle r = new Rectangle(p.x-e.icon.getIconWidth()/2, p.y-e.icon.getIconHeight()/2, e.icon.getIconWidth(), e.icon.getIconHeight());
				e.icon.paintIcon(mv, g, r.x, r.y);
				Border b = null;
				Point mousePosition = mv.getMousePosition();
				if (mousePosition == null)
					continue; // mouse outside the whole window
				if (!clickedFound && mousePressed && r.contains(mousePosition)) {
					b = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
					clickedFound = true;
				} else
					b = BorderFactory.createBevelBorder(BevelBorder.RAISED);
				Insets inset = b.getBorderInsets(mv);
				r.grow((inset.top+inset.bottom)/2, (inset.left+inset.right)/2);
				b.paintBorder(mv, g, r.x, r.y, r.width, r.height);
			}
		}
	}
	*/
}
