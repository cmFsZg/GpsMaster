package org.gpsmaster.gpxpanel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.gpsmaster.gpxpanel.WaypointGroup.WptGrpType;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.topografix.gpx._1._1.MetadataType;

/**
 * 
 * Top level GPX file element.  Contains all other GPX element types.
 * 
 * @author Matt Hoover
 *
 */
@XmlRootElement
public class GPXFile extends GPXObject {
    
    private String creator;
    private MetadataType metadata;
    private WaypointGroup waypointGroup;
    private List<Route> routes = new ArrayList<Route>();
    private List<Track> tracks = new ArrayList<Track>();
    
    /**
     * Creates an empty {@link GPXFile}.
     */
    public GPXFile() {
        super(true);
        // this.name = "UnnamedFile";
        this.metadata = new MetadataType();
        this.wptsVisible = false;
        this.creator = "GpsMaster";
         
        this.waypointGroup = new WaypointGroup(color, WptGrpType.WAYPOINTS); 
    }
    
    /**
     * Creates an empty {@link GPXFile}.
     * 
     * @param name      The name of the route. 
     */
    public GPXFile(String name) {
        this();
        if (!name.equals("")) {
            metadata.setName(name);
        }
    }
    
    /**
     * Constructs a {@link GPXFile} by cloning the specified object
     * ATTENTION - updateAllProperties() has to be called
     * externally after cloning.
     * @param source {@link GPXFile} to be cloned
     */
    public GPXFile(GPXFile source) {
    	super(source);
    	this.creator = source.creator;
    	this.metadata = source.metadata;
    	this.waypointGroup = new WaypointGroup(source.waypointGroup);
    	for (Track track : source.tracks) {
    		this.tracks.add(new Track(track));    		
    	}
    	for (Route route : source.routes) {
    		this.routes.add(new Route(route));
    	}    	
    }
    
    /**
     * 
     * @return
     */
    public long getNumTrackPts() {
    	long ctr = 0;
    	for (Track track : tracks) {
    		ctr += track.getNumPts();
    	}
    	return ctr;
    }

    /**
     * 
     * @return
     */
    public long getNumRoutePts() {
    	long ctr = 0;
    	for (Route route : routes) {
    		ctr += route.getNumPts();
    	}
    	return ctr;
    }

    /**
     * 
     * @return
     */
    public long getNumWayPts() {

    	return waypointGroup.getNumPts();
    }

        
    public void setColor(Color color) {
        super.setColor(color);
        waypointGroup.setColor(color);
        for (Route route : routes) {
            route.setColor(color);
        }
        for (Track track : tracks) {
            track.setColor(color);
        }
    }

    public String getCreator() {
        return creator;
    }
    
    public void setCreator(String cr) {
        this.creator = cr;
    }

    public MetadataType getMetadata() {
    	return metadata;
    }

    // name & desc now in metadata
    // temp code to find all occurances
    public String getName() {
    	return metadata.getName();
    }
    
    public void setName(String name) {
    	metadata.setName(name);
    }
    
    public String getDesc() {
    	return metadata.getDesc();
    }
    
    public void setDesc(String desc) {
    	metadata.setDesc(desc);
    }
    // temp code end

/*
 * 
    public Date getTime() {
        return time;
    }
    
    public void setTime(Date time) {
        this.time = time;
    }
*/
    public WaypointGroup getWaypointGroup() {
        return waypointGroup;
    }

    public List<Route> getRoutes() {
        return routes;
    }
    
    public Route addRoute() {
        Route route = new Route(color);
        route.setName(metadata.getName());
        routes.add(route);
        return route;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public String toString() {
        return metadata.getName();
    }
    
    /* (non-Javadoc)
     * @see org.gpsmaster.gpxpanel.GPXObject#updateAllProperties()
     */
    @Override
    public void updateAllProperties() {
    	
    	lengthMeters = 0;
    	duration = 0;
    	maxSpeedKmph = 0;
    	riseTime = 0;
    	fallTime = 0;
    	grossRiseMeters = 0;
    	grossFallMeters = 0;
    	
        if (waypointGroup.getWaypoints().size() > 1) {
            waypointGroup.updateAllProperties();
        }
        for (Route route : routes) {
            route.updateAllProperties();
        }
        for (Track track : tracks) {
            track.updateAllProperties();
        }
        
        minLat =  86;
        maxLat = -86;
        minLon =  180;
        maxLon = -180;
        eleMinMeters = Integer.MAX_VALUE;
        eleMaxMeters = Integer.MIN_VALUE;
        for (Route route : routes) {
            minLat = Math.min(minLat, route.getMinLat());
            minLon = Math.min(minLon, route.getMinLon());
            maxLat = Math.max(maxLat, route.getMaxLat());
            maxLon = Math.max(maxLon, route.getMaxLon());
        }
        for (Track track : tracks) {
            minLat = Math.min(minLat, track.getMinLat());
            minLon = Math.min(minLon, track.getMinLon());
            maxLat = Math.max(maxLat, track.getMaxLat());
            maxLon = Math.max(maxLon, track.getMaxLon());
            eleMinMeters = Math.min(eleMinMeters, track.getEleMinMeters());
            eleMaxMeters = Math.max(eleMaxMeters, track.getEleMaxMeters());
            lengthMeters += track.getLengthMeters();
            duration += track.getDuration();
            exStop += track.getDurationExStop();
            
            maxSpeedKmph = Math.max(maxSpeedKmph, track.getMaxSpeedKmph());            
            grossRiseMeters += track.getGrossRiseMeters();
            grossFallMeters += track.getGrossFallMeters();
            riseTime += track.getRiseTime();
            fallTime += track.getFallTime();
        }
        for (Waypoint waypoint : waypointGroup.getWaypoints()) {
            minLat = Math.min(minLat, waypoint.getLat());
            minLon = Math.min(minLon, waypoint.getLon());
            maxLat = Math.max(maxLat, waypoint.getLat());
            maxLon = Math.max(maxLon, waypoint.getLon());
        }
        
        if (tracks.size() > 0) {
        	startTime = tracks.get(0).getStartTime();
        	eleStartMeters = tracks.get(0).getEleStartMeters();
        }
        if (tracks.size() > 1) {
        	endTime = tracks.get(tracks.size()-1).getEndTime();
        	eleEndMeters = tracks.get(tracks.size()-1).getEleEndMeters();
        } 
                
        // if time in GPX file is not specified, use time of first waypoint
        if ((metadata.getTime() == null) && (tracks.size() > 0)) { 
        	Track track = tracks.get(0);
        	if (track.getTracksegs().size() > 0) {
        		Waypoint wpt = track.getTracksegs().get(0).getStart();
        		if (wpt != null) {
        			metadata.setTime(wpt.getTime());
        		}
        	}
        }
        
    }
}
