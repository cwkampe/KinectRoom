package ActiveSpace;

/**
 * a RegionEvent is a call-back object to be invoked
 * when an Actor enters or leaves a Region.  
 * 
 * This is a generic implementation that supports combinations
 * of images, sounds and text ... but much more sophisticated
 * callbacks could be created.
 *
 * @author cwkampe
 */
public class RegionEvent {
	
	private String imageFile;	// name of file containing image to display
	private String textFile;	// name of file containing text to display
	private String soundFile;	// name of file containing sound to play
	
	/**
	 * Constructor for new callback
	 * 
	 * Note:
	 * 		I thought of having the constructor take an XML element parm
	 * 		(e.g. for the <rule></rule>) but decided that was a "cute"
	 * 		idea that was probably much more trouble than it was worth.
	 */
	public RegionEvent() {
		imageFile = null;
		soundFile = null;
		textFile = null;
	}
	
	/**
	 * associate an image display action with a rule
	 * 
	 * @param name of file containing image to display
	 * 			"none" means clear the image
	 */
	public void setImage( String filename ) {
		imageFile = filename;
	}
	
	/**
	 * associate an sound playing action with a rule
	 * 
	 * @param name of file containing sound to play
	 * 			"none" means stop the sound
	 */
	public void setSound( String filename ) {
		soundFile = filename;
	}
	
	/**
	 * associate an text display action with a rule
	 * 
	 * @param name of file containing text to display
	 * 			"none" means clear the text
	 */
	public void setText( String filename ) {
		textFile = filename;
	}
	
	/**
	 * @return a pretty string form of our actions
	 */
	public String toString() {
		String out = "";
		if (imageFile != null)
			out += "image=" + imageFile + " ";
		if (soundFile != null)
			out += "sound=" + soundFile + " ";
		if (textFile != null)
			out += "text=" + textFile;
		
		return out;
	}
	
	/**
	 * @return XML sub-elements for this set of actions
	 */
	public String toXML() {
		String out = "";
		if (imageFile != null)
			out += "        <image file=\"" + imageFile + "\" />\n";
		if (soundFile != null)
			out += "        <sound file=\"" + soundFile + "\" />\n";
		if (textFile != null)
			out += "        <text file=\"" + textFile + "\" />\n";
		return out;
	}
	
	/**
	 * generic action callback when this event is triggered
	 * 
	 * 	execute the actions associated with this instance
	 * 
	 * @param r	Region in which this event happened
	 * @param a	Actor that triggered this event
	 * @param t	Type of event (e.g. entry/exit)
	 * 
	 * NOTE:
	 * 		the generic implementation does not use any of its
	 * 		parameters, taking fixed canned actions ... but they
	 * 		are provided in case a smaller callback handler wants
	 * 		to take actions based on the state of the region/actor.
	 */
	public void callback( Region r, Actor a, Rule.EventType t) {
		
		if (imageFile != null) {	// FIXME: add support for image display
			if (imageFile.equals("cancel")) {
				System.out.println("UNIMPLEMENTED ... clear displayed image");
			} else {
				System.out.println("UNIMPLEMENTED ... display image " + imageFile);
			}
		}
		
		if (soundFile != null) {	// FIXME: add support for sound playing
			if (soundFile.equals("cancel")) {
				System.out.println("UNIMPLEMENTED ... silence sound");
			} else {
				System.out.println("UNIMPLEMENTED ... play sound " + soundFile);
			}
		}
		
		if (textFile != null) {	// FIXME: add support for displayed text
			if (textFile.equals("cancel")) {
				System.out.println("UNIMPLEMENTED ... clear text");
			} else {
				System.out.println("UNIMPLEMENTED ... display text " + textFile);
			}
		}
	}
}
