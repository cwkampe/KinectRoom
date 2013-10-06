package KinectedSpace;

/**
 * A KinectedSpace is:
 * 	  a space, monitored by a single kinect
 *    with a set of defined regions
 *    a set of rules defining region entry/exit events
 *    a multi-media player capable of rendering sounds and images
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.JWindow;

import ActiveSpace.Actor;
import ActiveSpace.Coord;
import ActiveSpace.MediaActions;
import ActiveSpace.Space;

public class KinectedSpace extends JWindow 
	implements MediaActions, WindowListener, MouseListener {
	public boolean finished;	// we have been told to shut down
	
	private Space s;			// space in which we are running
	private Actor actors[];		// the known actors
	private int updates[];		// and their last update generations
	private int maxActors;		// upper limit on concurrent actors
	private int generation;		// monotonically increasing move generation
	
	private Dimension size;		// specified window size
	private Image image;		// active display image
	private Clip clip;			// active audio clip
	private String overText[];	// text to put on top of image
	private Color textColor;	// color in which to render that text
	
	private	int debugLevel;		// how noisy we want to be
	private int testsRun;		// how many tests have we run
	private boolean ignoreY;	// ignore Y values

	// pseudo-tunable constants
	private static final int MAX_ACTORS = 10;	// maximum concurrent actors
	
	// display text rendering
	private static final int	DISPLAY_FONT_SIZE = 16;
	private static final String	DISPLAY_FONT_STYLE = "BOLD";
	private static final Color	DISPLAY_FONT_COLOR = Color.white;
	
	private static final long serialVersionUID = 1L;	// LAME
	
	public KinectedSpace( Dimension d ) {
		size = d;				// note our window size
		finished = false;		// we're running
		ignoreY = true;			// treat space as two dimensional
		maxActors = MAX_ACTORS;	// limited number of concurrent actors
		testsRun = 0;			// we haven't run any tests yet
		clip = null;			// we are not playing any sounds
		image = null;			// we are not displaying any images
		overText = null;		// we do not have any overlay text
		
		s = new Space();
		
		// create a display window
		this.setPreferredSize( size );
		this.pack();
		this.setVisible(true);
		blankImage();
		
		// choose our text display font, size, color
		setFontSize(DISPLAY_FONT_SIZE);
		setFontStyle(DISPLAY_FONT_STYLE);
		setFontColor(DISPLAY_FONT_COLOR);
		
		// register ourselves as the multi-media player
		s.media(this);
		
		// capture window events
		addWindowListener(this);
		
		// capture mouse events
		addMouseListener(this);
		
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
	 * note the arrival of a new actor
	 * 
	 * @param	number of new actor
	 */
	public void addActor(int actorNumber) {
		Actor a = findActor(actorNumber);
		s.addActor(a);
	}
	
	/**
	 * note the departure of an actor
	 * 
	 * @param	number of departed actor
	 */
	public void dropActor(int actorNumber) {
		String name = "Actor-" + actorNumber;
		
		// see if we can find a record for this actor
		for( int i = 0; i < maxActors; i++ ) {
			if (actors[i] != null && name.equals(actors[i].toString())) {
				s.dropActor(actors[i]);
				actors[i] = null;
				return;
			}
		}
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
		s.prefix( base );
	}
	
	/**
	 * return the name of this space
	 */
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
		if (testsRun >= passes)
			finished = true;
		if (finished)
			return false;
		
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
	 * called before any positions are reported
	 */
	public void start() {
		s.processPosition(null, null);
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

	// TODO I should pull these out into a separate class
	public void displayImage(String filename) {

		// FIX this only works for files ... not URLs
		File file = new File(filename);
		if (!file.exists()) {
			System.out.println("Unable to access input image: " + filename);
			image = null;
			return;
		}
		if (debugLevel > 1)
			System.out.println("   ... display image file: " + filename);
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
	 * repaint our window with the selected image
	 * 	implicit parameters:
	 * 		self.image		image to be rendered
	 * 		self.overText	array of text lines to put on top of it
	 * 		self.getFont	current font, size, color
	 */
	public void paint( Graphics g ) {
		int width = (int) size.getWidth();
		int height = (int) size.getHeight();
		
		// render the image (which may be blank)
		g.drawImage(image, 0, 0, width, (int) height, this);
		
		// see if we have any text to put on to pof the image
		if (overText != null) {
			// figure out text width and height
			int textWidth = 0;
			int textHeight = 0;
			Font font = getFont();
			FontMetrics m = getFontMetrics(font);
			for (int i = 0; i < overText.length; i++) {
				textHeight += m.getHeight();
				int w = m.stringWidth(overText[i]);
				if (w > textWidth)
					textWidth = w;
			}
			
			// render the text
			g.setColor(textColor);	// I couldn't find g to do this sooner
			int row = (textHeight > height) ? 0 : (height - textHeight) / 2;
			int col = (textWidth > width) ? 0 : (width - textWidth) / 2;
			for (int i = 0; i < overText.length; i++) {
				g.drawString(overText[i], col, row);
				row += m.getHeight();
			}
		}
	}

	/**
	 * play the sound in the specified file
	 * @param filename
	 */
	public void playSound(String filename) {
		// FIX this only works for files ... not URLs
		File file = new File(filename);
		if (!file.exists()) {
			System.out.println("Unable to access sound file: " + filename);
			clip = null;
			return;
		}
		if (debugLevel > 1)
			System.out.println("   ... play audio file: " + filename);
		try {
			AudioInputStream in = AudioSystem.getAudioInputStream(file);
			AudioFormat format = in.getFormat();
			DataLine.Info info = new DataLine.Info(Clip.class, format);
			clip = (Clip) AudioSystem.getLine(info);
			clip.open(in);
			clip.start();
		} catch (Exception e) {
			System.out.println("Error playing sound file: " + filename);
			e.printStackTrace();
			clip = null;
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
		overText = null;
		
		// FIX this only works for files ... not URLs
		File file = new File(filename);
		if (!file.exists()) {
			System.out.println("Unable to access text file: " + filename);
			return;
		}
		if (debugLevel > 1)
			System.out.println("   ... display text file: " + filename);
		List<String> l = new ArrayList<String>();
		try {
			BufferedReader r = new BufferedReader(new FileReader(filename));
			String s;
			while ((s = r.readLine()) != null)
				l.add(s);
			r.close();
			overText = l.toArray(new String[0]);
		} catch (Exception e) {
			System.out.println("Error processing text file: " + filename);
			e.printStackTrace();
		}
		
		// either way, we now need to refresh the display
		repaint();
	}

	/**
	 * clear the displayed text
	 */
	public void clearText() {
		overText = null;
		repaint();
	}
	
	/**
	 * set font size for displayed text
	 * @param size	font size
	 */
	public void setFontSize(int newSize) {
		// figure out what we've got
		Font current = getFont();
		String name = current.getName();
		int style = current.getStyle();
		Font newFont = new Font(name, style, newSize);
		setFont(newFont);
	}
	
	/**
	 * set font name for displayed text
	 * @param newName	name of desired font
	 */
	public void setFontName(String newName) {
		// figure out what we've got
		Font current = getFont();
		int style = current.getStyle();
		int size = current.getSize();
		Font newFont = new Font(newName, style, size);
		setFont(newFont);
	}
	
	public void setFontColor(Color color) {
		textColor = color;
		// apparently I can't follow the JWindow to its graphics context
		// in order to directly do a setColor here ... so I just moved that
		// call into the paint method (which does have the graphics context)
	}
	
	/**
	 * set font name for displayed text
	 * @param newStile:	"plain", "bold", "italic"
	 */
	public void setFontStyle(String newStyle) {
		// figure out what we've got
		Font current = getFont();
		String name = current.getName();
		int size = current.getSize();
		int style = Font.PLAIN;
		if (newStyle.equals("BOLD") || newStyle.equals("bold"))
			style = Font.BOLD;
		else if (newStyle.equals("ITALIC") || newStyle.equals("italic"))
			style = Font.ITALIC;
		Font newFont = new Font(name, style, size);
		setFont(newFont);
	}
	
	
	// repaint the window when ever it reappears
	public void windowOpened(WindowEvent arg0) {
		if (debugLevel > 1)
			System.out.println("Image window opened");
		repaint();
	}
	
	public void windowActivated(WindowEvent arg0) {
		if (debugLevel > 1)
			System.out.println("Image window activated");
		repaint();
	}
	
	public void windowDeiconified(WindowEvent arg0) {
		if (debugLevel > 1)
			System.out.println("Image window de-iconified");
		repaint();
	}

	public void windowClosed(WindowEvent arg0) {}
	public void windowClosing(WindowEvent arg0) {}
	public void windowDeactivated(WindowEvent arg0) {}
	public void windowIconified(WindowEvent arg0) {}


	// mouse click in the window means shutdown
	public void mouseClicked(MouseEvent arg0) {
		if (debugLevel > 0)
			System.out.println("Mouse Click in display window ... shutting down");
		finished = true;
	}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
}
