package KinectedSpace;

import java.applet.Applet;
import java.awt.Dimension;


/**
 * Top level module for KinectedSpace - applet version
 *     get parameters
 *     instantiate the connected space
 *     create an update thread
 */
public class KinectedSpaceApplet extends Applet implements Runnable {
	
	private UserSensor sense;	// reference to KinectSensor Instance
	private KinectedSpace room;	// reference to KinectedSpace instance
	private boolean running;	// to control the updater thread
	private int testPasses;		// is this just a test run?
	private int debugLevel;		// how much debug output we want

	private static final String REGIONFILE = "Regions.xml";
	private static final String RULEFILE = "Rules.xml";
	private static final int MS_PER_STEP = 10;		// test execution speed
	private static final int HEIGHT = 768;
	private static final int WIDTH = 1024;
	
	private static final long serialVersionUID = 1L;	// LAME
	// TODO - can I use the applet window for my image displays?
	
	/**
	 * initialization method ... called at startup
	 * 	process parameters
	 */
	public void init() {

		// process our parameters
		String regionFile = getParameter("regions");
		if (regionFile == null)
			regionFile = REGIONFILE;
		String ruleFile = getParameter("rules");
		if (ruleFile == null) 
			ruleFile = RULEFILE;
		String prefix = getParameter("base");
		String s = getParameter("height");	// TODO - collides w/applet height
		int height = (s == null) ? HEIGHT : Integer.parseInt(s);
		s = getParameter("width");			// TODO - collides w/applet width
		int width = (s == null) ? WIDTH : Integer.parseInt(s);
		s = getParameter("test");
		testPasses = (s == null) ? 0 : Integer.parseInt(s);
		s = getParameter("debug");
		debugLevel = (s == null) ? 0 : Integer.parseInt(s);
		
		// instantiate the space
		Dimension d = new Dimension(width,height);
		room = new KinectedSpace(d);
		room.debug(debugLevel);
		room.prefix(prefix);
		room.readRegions(regionFile);
		room.readRules(ruleFile);
	}
	
	/**
	 * execution startup
	 */
	public void start() {
		if (testPasses > 0) {
			while( room.test(testPasses) ) {
				try {
					Thread.sleep(MS_PER_STEP);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}
		} else {
			sense = new UserSensor();
			sense.debug(debugLevel);
			
			room.start();
			
			Thread updater = new Thread(this);
			running = true;
			updater.start();
		}
	}
	
	/**
	 * main loop
	 * 	 update the sensor
	 * 	 check for new and dropped actors
	 * 	 for each user
	 * 		get his position, and pass it to the room
	 */
	public void run() {
		int minActor = -1;
		int maxActor = -1;
		int actors = 0;
		
		while(running && !room.finished) {
			sense.update();
			int n = sense.numUsers();
			int lowest = 99999;
			
			// look for added or lost actors
			if (n != actors) {
				for(int i = 0; i < n; i++) {
					// see if any new actors have been added
					int a = sense.actor(i);
					if (a > maxActor) {
						room.addActor(a);
						maxActor = a;
					} else if (a < lowest)
						lowest = a;
				}
				
				// see if we lost any actors
				if (lowest != 99999) {
					if (minActor == -1)
						minActor = lowest;
					while(lowest > minActor) {
						room.dropActor(minActor++);
					}
				}
		
				actors = n;
			}
			
			// now update all of their positions
			for(int i = 0; i < n; i++)
				room.update(sense.actor(i), sense.getCoM(i));
		}
	}
	
	/**
	 * shut down the updater thread
	 */
	public void destroy() {
		running = false;
	}
}
