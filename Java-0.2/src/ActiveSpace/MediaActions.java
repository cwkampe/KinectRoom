package ActiveSpace;

/**
 * This is the media playing interface exposed to Active Spaces
 */
public interface MediaActions {
	/**
	 * display the image from the specified file
	 * @param filename
	 */
	public void displayImage( String filename );
	
	/**
	 * blank the displaned image
	 */
	public void blankImage();
	
	/**
	 * play the sound from the specified file
	 * @param filename
	 */
	public void playSound( String filename );
	
	/**
	 * silence the sound player
	 */
	public void silence();
	
	/**
	 * display the text from the specified file
	 * 
	 * @param filename
	 */
	public void displayText( String filename );
	
	/**
	 * clear the displayed text
	 */
	public void clearText();
}
