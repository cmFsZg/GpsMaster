package org.gpsmaster.gpxpanel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.gpsmaster.gpxpanel.WaypointGroup.WptGrpType;

/**
 *
 * The GPX "trk" element.
 *
 * @author Matt Hoover
 *
 */
public class Track extends GPXObjectCommon implements Comparable<Track> {

    private List<WaypointGroup> tracksegs = new ArrayList<WaypointGroup>();

    /**
     * Constructs a {@link Track} with the chosen color.
     *
     * @param color     The color.
     */
    public Track(Color color) {
        super(color);
    }

    /**
     * Constructs a {@link Track} by cloning the specified object
     * ATTENTION - updateAllProperties() has to be called
     * externally after cloning.
     * @param source {@link Track} to be cloned
     */
    public Track(Track source) {
    	super(source);
    	for (WaypointGroup wptGrp : source.tracksegs) {
    		this.tracksegs.add(new WaypointGroup(wptGrp));
    	}
    }

    public String toString() {
        String str = "Track";
        if (this.name != null && !this.name.equals("")) {
            str = str.concat(" - " + this.name);
        }
        return str;
    }

    public void setColor(Color color) {
        super.setColor(color);
        for (WaypointGroup trackseg : tracksegs) {
            trackseg.setColor(color);
        }
    }



    @XmlElement(name = "trkseg")
    public List<WaypointGroup> getTracksegs() {
        return tracksegs;
    }

    public WaypointGroup addTrackseg() {
        WaypointGroup trackseg = new WaypointGroup(this.color, WptGrpType.TRACKSEG);
        tracksegs.add(trackseg);
        return trackseg;
    }

    /**
     *
     * @return
     */
    public long getNumPts() {
        long ctr = 0;
        for (WaypointGroup wptGrp : tracksegs) {
        	ctr += wptGrp.getNumPts();
        }
        return ctr;
    }

    /* (non-Javadoc)
     * @see org.gpsmaster.gpxpanel.GPXObject#updateAllProperties()
     */
    @Override
    public void updateAllProperties() {
    	lengthMeters = 0;
        maxSpeedKmph = 0;
        duration = 0;
        eleMinMeters = Integer.MAX_VALUE;
        eleMaxMeters = Integer.MIN_VALUE;
        minLat =  86;
        maxLat = -86;
        minLon =  180;
        maxLon = -180;

        for (WaypointGroup trackseg : tracksegs) {
            trackseg.updateAllProperties();

            duration += trackseg.getDuration();
            exStop += trackseg.getDurationExStop();
            maxSpeedKmph = Math.max(maxSpeedKmph, trackseg.getMaxSpeedKmph());
            lengthMeters += trackseg.getLengthMeters();
            eleMinMeters = Math.min(eleMinMeters, trackseg.getEleMinMeters());
            eleMaxMeters = Math.max(eleMaxMeters, trackseg.getEleMaxMeters());
            grossRiseMeters += trackseg.getGrossRiseMeters();
            grossFallMeters += trackseg.getGrossFallMeters();
            riseTime += trackseg.getRiseTime();
            fallTime += trackseg.getFallTime();

            minLat = Math.min(minLat, trackseg.getMinLat());
            minLon = Math.min(minLon, trackseg.getMinLon());
            maxLat = Math.max(maxLat, trackseg.getMaxLat());
            maxLon = Math.max(maxLon, trackseg.getMaxLon());
        }

        if (tracksegs.size() > 0) {
            eleStartMeters = tracksegs.get(0).getEleStartMeters();
            eleEndMeters = tracksegs.get(tracksegs.size() - 1).getEleEndMeters();
            startTime = tracksegs.get(0).getStartTime();
            endTime = tracksegs.get(tracksegs.size() - 1).getEndTime();
        } else {
            eleStartMeters = 0;
            eleEndMeters = 0;
        }
        extToColor();
    }

	@Override
	public int compareTo(Track o) {
		if ((getStartTime() == null) || (o.getStartTime() == null)) {
			return 0;
		}
		return getStartTime().compareTo(o.getStartTime());
	}

}
