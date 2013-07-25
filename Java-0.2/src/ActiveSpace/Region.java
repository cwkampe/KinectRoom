package ActiveSpace;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A Region is a bounded area that can generate entry
 * and exit events as Actors move in and out of them.
 * 
 * Note that the current implementation supports only
 * circular regions.
 */
public class Region {
	
	private String name;	//* name of this region
	private Coord location;	//* location of center
	private float radius;	//* (nominal) radius of this region
	private int state;		//* current state
	private List<Rule> rules;	//* rules for this region
	
	/**
	 * Create a new (circular) region
	 * 
	 * @param name	region name
	 * @param where	center of region
	 * @param radius radius of region
	 */
	public Region(String name, Coord where, float radius) {
		this.name = name;
		this.location = where;
		this.radius = radius;
		this.state = 0;
		this.rules = new LinkedList<Rule>();
	}
	
	/**
	 * @return string form of region name/location
	 */
	public String toString() {
		return( name + "@" + location + ",r=" + radius );
	}
	

	/**
	 * @return XML description for this region
	 */
	public String toXML() {
		String out = "    <region ";
		out += "name=\"" + name + "\" ";
		out += "radius=\"" + radius + "\"";
		out += ">\n";
		out += location.toXML();
		out += "    </region>\n";
		
		return out;
	}
	
	/**
	 * @return XML description for rules in this region
	 */
	public String rulesToXML() {
		String out = "";
		Iterator<Rule> it = rules.iterator();
		while( it.hasNext()) {
			Rule r = (Rule) it.next();
			out += r.toXML();
		}
		
		return( out );
	}
	
	/**
	 * generate a pretty list of rules
	 */
	public String listRules() {
		String out = "";
		Iterator<Rule> it = rules.iterator();
		while( it.hasNext()) {
			Rule r = (Rule) it.next();
			out += r;
		}
		
		return out;
	}
	
	/**
	 * @return name of this region
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * @return position of the center of this region
	 */
	public Coord getCenter() {
		return this.location;
	}
	
	/**
	 * @return radius of this region
	 */
	public float getRadius() {
		return this.radius;
	}
	
	/**
	 * put this region into a specified state
	 * @param newsate
	 */
	public void setState( int newstate ) {
		this.state = newstate;
	}
	
	/**
	 * @return state of this region
	 */
	public int getState() {
		return(this.state);
	}
	
	/**
	 * associate an event rule with this region
	 * 
	 * @param rule to add
	 */
	public void addRule( Rule rule) {
		rules.add(rule);
	}
	
	/**
	 * examine an Actor's position, determine if there have
	 * been any entry/exit events, and if so make the appropriate
	 * call-backs.
	 * 
	 * @param actor	Actor to be examined
	 * @param new posistion of actor
	 * @param debug level
	 * @return whether or not any events were generated
	 */
	public boolean processPosition(Actor actor, Coord newPosn, MediaActions m) {

		Rule.EventType event = Rule.EventType.NONE;
		final float epsilon = 0.05F;	// debounce threshold
		
		float newDistance = this.location.dist(newPosn);
		if (actor.lastPosition() == null) {
			// it is possible for an initial report to trigger an ENTRY event
			if (newDistance > radius * (1-epsilon))
				return false;
			event = Rule.EventType.ENTRY;
		} else {	// all other reports look at relative motion
			/*
			 * To avoid signal noise from small movements near the radius,
			 * I attempt to smooth the signal by requiring entries to
			 * come epsilon inside the radius and exits to go epsilon
			 * outside of the radius.  The right way to know which radius
			 * to test against would be to track whether or not each actor 
			 * is believed to be in each region ... but I didn't want to 
			 * couple those two classes.  So instead I looking at whether
			 * the most recent motion is inwards or outwards to decide 
			 * whether to use the entry or exit radius.
			 * I think this should work :-)
			 */
			// FIX - is there a way to exploit Actor.lastRegion to help
			float oldDistance = this.location.dist(actor.lastPosition());
			boolean approach = (newDistance < oldDistance);
			if (approach) {
				if (newDistance > radius * (1-epsilon))
					return( false );	// haven't entered yet
				if (oldDistance <= radius * (1-epsilon))
					return( false );	// we were already in
				event = Rule.EventType.ENTRY;
			} else {
				if (newDistance < radius * (1+epsilon))
					return( false );	// haven't left yet
				if (oldDistance >= radius * (1+epsilon))
					return( false );	// we were already out
				event = Rule.EventType.EXIT;
			}
		}
		
		// check all of my rules to see if this matches any
		boolean didSomething = false;
		Iterator<Rule> it = rules.iterator();
		while( it.hasNext()) {
			Rule r = (Rule) it.next();
			if (r.checkTriggered(actor, event, m))
				didSomething = true;
		}

		return didSomething;
	}
}
