package org.gpsmaster.online;

/**
 * Generic Class to hold a single track from online services
 * 
 * @author rfu
 * @author tim.prune
 * Code taken from GpsPrune
 * http://activityworkshop.net/
 * 
 */
 public class OnlineTrack
{
	 private long id = 0;
	/** Track name or title */
	private String trackName = null;
	/** Description */
	private String description = null;
	/** Web page for more details */
	private String webUrl = null;
	/** Track length in metres */
	private double trackLength = 0.0;
	/** Download link */
	private String downloadLink = null;


	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @param inName name of track
	 */
	public void setTrackName(String inName)
	{
		trackName = inName;
	}

	/**
	 * @return track name
	 */
	public String getTrackName()
	{
		return trackName;
	}

	/**
	 * @param inDesc description
	 */
	public void setDescription(String inDesc)
	{
		description = inDesc;
	}

	/**
	 * @return track description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @param inUrl web page url
	 */
	public void setWebUrl(String inUrl)
	{
		webUrl = inUrl;
	}

	/**
	 * @return web url
	 */
	public String getWebUrl()
	{
		return webUrl;
	}

	/**
	 * @param inLength length of track
	 */
	public void setLength(double inLength)
	{
		trackLength = inLength;
	}

	/**
	 * @return track length
	 */
	public double getLength()
	{
		return trackLength;
	}

	/**
	 * @param inLink link to download track
	 */
	public void setDownloadLink(String inLink)
	{
		downloadLink = inLink;
	}

	/**
	 * @return download link
	 */
	public String getDownloadLink()
	{
		return downloadLink;
	}
}
