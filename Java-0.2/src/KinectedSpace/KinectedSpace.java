package KinectedSpace;

/**
 * A KinectedSpace is:
 * 	  a space, monitored by a single kinect
 *    with a set of defined regions
 *    a set of rules defining region entry/exit events
 */

import ActiveSpace.Actor;
import ActiveSpace.Coord;
import ActiveSpace.Space;

public class KinectedSpace {
	public boolean finished;	// we have been told to shut down
	
	private Space s;			// space in which we are running
	private Actor actors[];		// the known actors
	private int updates[];		// and their last update generations
	private int maxActors;		// upper limit on concurrent actors
	private int generation;		// monotonically increasing move generation
	
	private String prefix;		// base prefix for files
	private	int debugLevel;		// how noisy we want to be
	private int testsRun;		// how many tests have we run
	private boolean ignoreY;	// ignore Y values

	// pseudo-tunable constants
	private static final int MAX_ACTORS = 10;	// maximum concurrent actors
	
	public KinectedSpace() {
		finished = false;		// we're running
		ignoreY = true;			// treat space as two dimensional
		maxActors = MAX_ACTORS;	// limited number of concurrent actors
		testsRun = 0;			// we haven't run any tests yet
		prefix = null;			// we have no base prefix
		
		s = new Space();
		
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
	 * @param filename
	 */
	public void readRegions( String filename ) {
		if (prefix != null)
			filename = prefix + "/" + filename;
		try {
			s.readRegions(filename, ignoreY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * read a set of rule definitions into the space
	 * 
	 * @param filename
	 */
	public void readRules( String filename ) {
		if (prefix != null)
			filename = prefix + "/" + filename;
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
}
