package KinectedSpace;

/**
 * A KinectedSpace is:
 * 	  a space, monitored by a single kinect
 *    with a set of defined regions
 *    a set of rules defining region entry/exit events
 *    a multi-media player capable of rendering sounds and images
 */

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JWindow;

import ActiveSpace.Actor;
import ActiveSpace.Coord;
import ActiveSpace.MediaActions;
import ActiveSpace.Space;

public class KinectedSpace extends JWindow implements MediaActions {
	public boolean finished;	// we have been told to shut down
	
	private Space s;			// space in which we are running
	private Actor actors[];		// the known actors
	private int updates[];		// and their last update generations
	private int maxActors;		// upper limit on concurrent actors
	private int generation;		// monotonically increasing move generation
	
	private Dimension size;		// specified window size
	private Image image;		// active display image
	private Clip clip;			// active audio clip
	
	private String prefix;		// base prefix for files
	private	int debugLevel;		// how noisy we want to be
	private int testsRun;		// how many tests have we run
	private boolean ignoreY;	// ignore Y values

	// pseudo-tunable constants
	private static final int MAX_ACTORS = 10;	// maximum concurrent actors
	
	public KinectedSpace( Dimension d ) {
		size = d;				// note our window size
		finished = false;		// we're running
		ignoreY = true;			// treat space as two dimensional
		maxActors = MAX_ACTORS;	// limited number of concurrent actors
		testsRun = 0;			// we haven't run any tests yet
		prefix = null;			// we have no base prefix
		clip = null;			// we are not playing any sounds
		image = null;			// we are not displaying any images
		
		s = new Space();
		
		// create a display window and register us as the multi-media player
		this.setPreferredSize( size );
		this.pack();
		this.setVisible(true);
		s.media(this);
		
		actors = new Actor[maxActors];
		updates = new int[maxActors];
		for( int i = 0; i < maxActors; i++) {
			actors[i] = null;
			updates[i] = 0;
		}
	}
	
	/**
	 * find/allocate a particular Actor object
	 * 
	 * @param actorNumber	(monotonically increasing) actor number
	 * @return				Actor object
	 */
	private Actor findActor( int actorNumber ) {
		String name = "Actor-" + actorNumber;
		
		// see if we can find a record for this actor
		int free = 0;
		for( int i = 0; i < maxActors; i++ ) {
			if (actors[i] == null)
				free++;
			else if (name.equals(actors[i].toString())) {
				updates[i] = ++generation;
				return( actors[i] );
			}
		}
		
		/*
		 * Garbage collection
		 * 		To avoid creating a watcher-style path for 
		 * 		"Elvis has left the building" events between
		 * 		the sensor and the space, I chose to do a 
		 * 		simple LRU for tracked Actors
		 */
		if (free == 0) {
			int oldest = 0;
			for( int i = 1; i < maxActors; i++ ) {
				if (updates[i] < updates[oldest])
					oldest = i;
			}
			actors[oldest] = null;
			updates[oldest] = 0;
		}
		
		// allocate a new actor tracker
		for( int i = 0; i < maxActors; i++ )
			if (actors[i] == null) {
				actors[i] = new Actor(name, null);
				updates[i] = 0;
				return( actors[i] );
			}
		
		return( null );		// can't reach
	}

	/**
	 * read a set of region definitions into the space
	 * 
	 * @param filename (not relative to prefix)
	 */
	public void readRegions( String filename ) {
		try {
			s.readRegions(filename, ignoreY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * read a set of rule definitions into the space
	 * 
	 * @param filename (not relative to prefix)
	 */
	public void readRules( String filename ) {
		try {
			s.readRules(filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * set the base prefix for all files we access
	 * 
	 * @param base
	 */
	public void prefix( String base ) {
		this.prefix = base;
		s.prefix( base );
	}
	
	public String name() {
		return( s.name());
	}
	
	/**
	 * set the debug level for this space
	 * @param level
	 */
	public void debug( int level ) {
		debugLevel = level;
		s.debug(debugLevel);
	}
	
	/**
	 * auto-test
	 * In test mode we progressively move a sequence of actors
	 * through all of the regions in the space, trying to hit
	 * all of the (stateless) rules.   We work our way through
	 * the test Actors, calling the space.test method on each 
	 * until it reports that all have done the whole walk.
	 * 
	 * @param passes	desired number of test passes
	 * @return	true if there is still more testing to do
	 */
	public boolean test( int passes ) {		
		// stop when we have run the requested number of tests
		if (testsRun >= passes) {
			finished = true;
			return false;
		}
		
		// figure out which actor we should be moving
		Actor a = findActor(testsRun);
		
		// move us along current pass or advance us to next
		if (s.test(a) == false) {
			testsRun++;
			a.lastPosition( null );
		}
		return true;
	}

	/**
	 * regularly called update method
	 * 
	 * @param	actor number (monotonically increasing)
	 * @param	current position
	 */
	public void update(int actorNum, Coord pos) {
		// ignore non-reports
		if (pos == null)
			return;
		
		// perhaps ignore all Y coordinates
		if (ignoreY)
			pos.y = 0;
		
		// ignore reports about non-actors
		Actor a = findActor(actorNum);
		if (a == null)
			return;
		
		if (debugLevel > 1) {
			if (a.lastPosition() == null)
				System.out.println("Actor " + a + " entered at " + pos);
			else
				System.out.println("    Actor " + a + " at " + pos);
		}

		s.processPosition(a, pos);		// process the new position
		a.lastPosition(pos);			// update the known position
	}

	public void displayImage(String filename) {
		if (debugLevel > 1)
			System.out.println("   ... display image file: " + filename);
		File file = new File(filename);
		try {
			image = ImageIO.read(file);
		} catch (Exception e) {
			System.out.println("Error reading image file: " + filename);
			e.printStackTrace();
			image = null;
		}
		
		repaint();
	}

	public void blankImage() {
		if (debugLevel > 1)
			System.out.println("   ... clear displayed image");
		image = new BufferedImage((int) size.width, (int) size.height, BufferedImage.TYPE_BYTE_BINARY);
		repaint();
	}
	
	/**
	 * repaint our window with thye selected image
	 */
	public void paint( Graphics g ) {
		g.drawImage(image, 0, 0, (int) size.getWidth(), (int) size.getHeight(), this);
	}

	/**
	 * play the sound in the specified file
	 * @param filename
	 */
	public void playSound(String filename) {
		if (debugLevel > 1)
			System.out.println("   ... play audio file: " + filename);
		try {
			File file = new File(filename);
			AudioInputStream in = AudioSystem.getAudioInputStream(file);
			clip = AudioSystem.getClip();
			clip.open(in);
			clip.start();
		} catch (Exception e) {
			
		}
	}

	/**
	 * silence any playing sound
	 */
	public void silence() {
		if (debugLevel > 1)
			System.out.println("   ... silence");
		if (clip != null && clip.isRunning()) {
			clip.stop();
			clip = null;
		}
	}

	/**
	 * display the text in the specified file
	 * @param filename
	 */
	public void displayText(String filename) {
		System.out.println("UNIMPLEMENTED: display text from file " + filename);
		// TODO implement text display
	}

	/**
	 * clear the displayed text
	 */
	public void clearText() {
		System.out.println("UNIMPLEMENTED: clear displayed text");
		// TODO implement text clearing
	}
}
