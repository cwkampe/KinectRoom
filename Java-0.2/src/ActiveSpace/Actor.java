package ActiveSpace;

/**
 * an Actor is a body with a position in the space
 *
 * @author cwkampe
 */
public class Actor {
	private String name;		//* given name of this actor
	private Coord lastPosition;	//* last known position of this actor
	private Region lastRegion;	//*	last region entered by this actor
	
	/**
	 * instantiate a new Actor in some position
	 * 
	 * @param name of this actor
	 * @param location of this actor
	 */
	public Actor( String name, Coord location ) {
		this.name = name;
		this.lastPosition = location;
	}
	
	/**
	 * move actor to a new position
	 * 
	 * @param location
	 */
	public void lastPosition( Coord location ) {
		this.lastPosition = location;
	}
	
	/**
	 * @return last known position
	 */
	public Coord lastPosition() {
		return(this.lastPosition);
	}
	
	/**
	 * @return last region entered
	 */
	public Region lastEntered() {
		return(this.lastRegion);
	}
	
	/**
	 * set the last region entered
	 */
	public void lastEntered( Region r ) {
		this.lastRegion = r;
	}
	
	public String toString() {
		return( name );
	}
}
