package org.gpsmaster.dialogs;

import javax.swing.JDialog;
import javax.swing.JFrame;

import eu.fuegenstein.messagecenter.MessageCenter;

/**
 * 
 * derzeit etwas ungl�cklich. entweder alle dialoge darauf aufbauen
 * oder diese klasse in GenericDownloadDialog einarbeiten
 *
 */
/**
 * Generic function class for launching from the app
 */
public abstract class GenericDialog extends JDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2979483355856594216L;
	/** Reference to message center */
	protected MessageCenter msg = null;
	/** Reference to parent parentFrame */
	protected JFrame parentFrame = null;


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public GenericDialog(JFrame frame, MessageCenter msg)	
	{
		super(frame);
		this.msg = msg;
		parentFrame = frame;
	}

	/**
	 * Begin the function
	 */
	public abstract void begin();

	/**
	 * @return the key for the function name
	 */
	public abstract String getTitle();
	
}
