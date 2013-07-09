package ActiveSpace;
/**
 * Coordinates in a two- or three-dimensional space
 * 
 * 	these are a simplified version of the Processing PVector,
 * 	implemented independently (with only the few required functions)
 * 	to enable this code to be independent of Processing.
 *
 * @author cwkampe
 */
public class Coord {
	
	public float x;		/** horizontal distance from center	*/
	public float y;		/**	vertical distance from center	*/
	public float z;		/**	distance away from kinect		*/
	
	/**
	 * constructor for 2D coordinates
	 */
	public Coord( float x, float z ) {
		this.x = x;
		this.y = 0;
		this.z = z;
	}
	
	/**
	 * constructor for 3D coordinates
	 */
	public Coord( float x, float y, float z ) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * @param from point relative to which distance is measured	
	 * @return	distance between this and that point
	 */
	public float dist( Coord from ) {
		return( dist( from.x, from.y, from.z) );
	}
	
	/**
	 * @return	distance (always positive) between this and specified coordinates
	 */
	public float dist( float x, float y, float z ) {
		float dx = this.dx(x);
		float dy = this.dy(y);
		float dz = this.dz(z);
		float d = (dx * dx) + (dy * dy) + (dz * dz);
		return (float) Math.sqrt(d);
	}
	
	public float dx(float x) {
		return(x - this.x);
	}
	
	public float dy(float y) {
		return(y - this.y);
	}
	
	public float dz(float z) {
		return(z - this.z);
	}
	
	public String toString() {
		return( "<" + x + "," + y + "," + z + ">");
	}
	
	/**
	 * @return XML description for this region
	 */
	public String toXML() {
		String out = "        <position ";
		out += "x=\"" + x + "\" ";
		out += "y=\"" + y + "\" ";
		out += "z=\"" + z + "\" ";
		out += "/>\n";

		return( out );
	}
}
