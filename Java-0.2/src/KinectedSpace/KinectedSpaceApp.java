package KinectedSpace;

import java.awt.Dimension;

/**
 * main class
 * 		process command line arguments
 * 		instantiate a KinectedSpace
 * 		loop on its update method
 * 
 * @author markk
 */
public class KinectedSpaceApp {
	
	private UserSensor sense;	// reference to KinectSensor Instance
	private KinectedSpace room;	// reference to KinectedSpace instance

	private static String usage[] = {
		"kinectedSpace.java [switches] [regions-file] [rules-file]",
		"    switches:",
		"        --regions=regions-file",
		"        --rules=rules-file",
		"        --base=prefix",
		"        --height=#",
		"        --width=#",
		"        --test=#",
		"        --debug=#"
	};
	
	private static final String REGIONFILE = "bin/Regions.xml";
	private static final String RULEFILE = "bin/Rules.xml";
	private static final String PREFIX = "bin";
	private static final int HEIGHT = 768;
	private static final int WIDTH = 1024;
	
	private static final int MS_PER_STEP = 10;		// test movement rate
	
	public static void main(String args[]) {
	
		String regionFile = null;
		String ruleFile = null;
		String prefix = null;
		int height = HEIGHT;
		int width = WIDTH;
		int testPasses = 0;
		int debug = 0;
		
		// process the command line arguments (why isn't there a standard for this?)
		for( int i = 0; i < args.length; i++ ) {
			if (args[i].contains("regions=")) {
				regionFile = args[i].substring(args[i].indexOf('=') + 1);
			} else if (args[i].contains("rules=")) {
				ruleFile = args[i].substring(args[i].indexOf('=') + 1);
			} else if (args[i].contains("base=")) {
				prefix = args[i].substring(args[i].indexOf('=') + 1);
			} else if (args[i].contains("height=")) {
				height = Integer.parseInt(args[i].substring(args[i].indexOf('=') + 1));
			} else if (args[i].contains("width=")) {
				width = Integer.parseInt(args[i].substring(args[i].indexOf('=') + 1));
			} else if (args[i].contains("debug=")) {
				debug = Integer.parseInt(args[i].substring(args[i].indexOf('=') + 1));
			} else if (args[i].contains("test=")) {
				testPasses = Integer.parseInt(args[i].substring(args[i].indexOf('=') + 1));
			} else if (args[i].contains("help") || args[i].contains("?")) {
				usage();
				return;
			} else if (regionFile == null) {
				regionFile = args[i];
			} else if (ruleFile == null) {
				ruleFile = args[i];
			} else {
				usage();
				return;
			}
		}
		
		// use default file locations if none are specified
		if (ruleFile == null)
			ruleFile = RULEFILE;
		if (regionFile == null)
			regionFile = REGIONFILE;
		if (prefix == null)
			prefix = PREFIX;
		
		// instantiate a KinectedSpace
		Dimension d = new Dimension(width, height);
		KinectedSpaceApp app = new KinectedSpaceApp(regionFile, ruleFile, prefix, d, debug);
		if (testPasses > 0) {
			while( app.room.test(testPasses) ) {
				try {
					Thread.sleep(MS_PER_STEP);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}
			System.exit(0);
		} else {
			app.sense = new UserSensor();
			app.sense.debug(debug);
			app.run();
		}
	}
	
	
	/**
	 * initialize an app instance
	 */
	public KinectedSpaceApp( String regionFile, String ruleFile, String prefix, Dimension d, int debugLevel ) {		
		// create the basic space
		room = new KinectedSpace(d);
		room.debug(debugLevel);
		room.readRegions(regionFile);
		room.prefix(prefix);
		room.readRules(ruleFile);
		sense = null;
	}
	
	/**
	 * main loop
	 * 		update the sensor
	 * 		for each user
	 * 			get his position, and pass it to the room
	 */
	public void run() {
		room.start();
		
		int minActor = -1;
		int maxActor = -1;
		int actors = 0;
		
		while( !room.finished) {
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
			
			for(int i = 0; i < n; i++)
				room.update(sense.actor(i), sense.getCoM(i));
		}
		System.exit(0);
	}

	/**
	 * print out a usage message
	 */
	private static void usage() {
		for( int i = 0; i < usage.length; i++ )
			System.out.println(usage[i]);
	}
}
