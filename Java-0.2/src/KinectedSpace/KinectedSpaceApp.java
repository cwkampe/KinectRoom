package KinectedSpace;

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
		"        --test=#",
		"        --debug=#"
	};
	
	private static final String REGIONFILE = "bin/Regions.xml";
	private static final String RULEFILE = "bin/Rules.xml";
	private static final String PREFIX = "bin";
	private static final int MS_PER_STEP = 10;		// test movement rate
	
	public static void main(String args[]) {
	
		String regionFile = null;
		String ruleFile = null;
		String prefix = null;
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
		KinectedSpaceApp app = new KinectedSpaceApp(regionFile, ruleFile, prefix, debug);
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
	public KinectedSpaceApp( String regionFile, String ruleFile, String prefix, int debugLevel ) {
		room = new KinectedSpace();
		room.debug(debugLevel);
		room.prefix(prefix);
		room.readRegions(regionFile);
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
		while( !room.finished) {
			sense.update();
			int n = sense.numUsers();
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
