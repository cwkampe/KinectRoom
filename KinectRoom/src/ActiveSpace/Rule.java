package ActiveSpace;

/**
 * a Rule is a set of trigger conditions and an associated set of actions
 * 
 * @author markk
 */
public class Rule {
	
	//* the known types of triggering events
	public enum EventType {
		NONE, ENTRY, EXIT
	};
	
	//* convert string representations into EventType enum values
	public static EventType eventType( String s ) {
		if (s.equals("ENTRY") || s.equals("entry"))
			return EventType.ENTRY;
		if (s.equals("EXIT") || s.equals("exit"))
			return EventType.EXIT;
		return EventType.NONE;
	}
	
	private String name;
	private Region region;
	private EventType eventType;
	private int initState;
	private int nextState;
	private RegionEvent action;
	
	/**
	 * Create a new rule
	 * 
	 * @param name		name of this rule
	 * @param region	region to which it apples
	 * @param event		triggering event type 
	 * @param initState	triggering region state
	 * @param nextState	next state to move to
	 * @param callback	action handler
	 */
	public Rule( 
			String name, Region region, EventType event, int initState, int nextState, 
			RegionEvent callback )	
	{	// initialize the rule
		this.name = name;
		this.region = region;
		this.eventType = event;
		this.initState = initState;
		this.nextState = nextState;
		this.action = callback;
		
		// and associate it with the region
		region.addRule(this);
	}
	
	private static final String ruleFormat = "%-20s\t%-10s\t%-10s\t%2d->%2d\t%s\n";
	/**
	 * @return	String containing one line description of this rule
	 */
	public String toString() {
		String out = String.format(ruleFormat,
				name, region.getName(), eventType, initState, nextState, action );
		return out;
	}
	
	/**
	 * generate the XML representation for this rule
	 * 
	 * @return	String for XML representation
	 */
	public String toXML() {
		String out = "    <rule";
		out += " name=\"" + this.name + "\"";
		out += " region=\"" + this.region.getName() + "\"";
		out += " event=\"" + this.eventType + "\"";
		if (initState >= 0)
			out += " state=\"" + initState + "\"";
		if (nextState >= 0)
			out += " next=\"" + nextState + "\"";
		out += ">\n";
		out += this.action.toXML();
		out += "    </rule>\n";
		
		return out;
	}
	
	/**
	 * check whether or not this rule has been triggered, and if
	 * so perform the appropriate actions
	 *
	 * @param actor	actor who triggered eent
	 * @param event	type of event
	 * @return		whether or not the event was triggered
	 */
	public boolean checkTriggered( Actor actor, EventType event ) {
		// see if the triggering conditions have been met
		if (event != eventType)
			return false;
		if (initState >= 0 && initState != region.getState())
			return false;
		
		// call the event callback handler
		System.out.println("Actor " + actor + " triggered rule '" + name + "'");
		action.callback(region, actor, event);
		if (nextState >= 0)
			region.setState(nextState);
		
		return true;
	}
}
