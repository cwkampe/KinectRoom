package KinectedSpace;

import org.openni.IObservable;
import org.openni.IObserver;
import org.openni.License;
import org.openni.Point3D;
import org.openni.Context;
import org.openni.DepthGenerator;
import org.openni.DepthMetaData;
import org.openni.GeneralException;
import org.openni.StatusException;
import org.openni.UserEventArgs;
import org.openni.UserGenerator;

import ActiveSpace.Coord;

/**
 * A UserSensor keeps track of user positions.
 */
public class UserSensor {

    private Context context;				// session
    private DepthGenerator depthGen;		// depth generator (used by user generator)
    @SuppressWarnings("unused")
	private int width, height;				// sensor resolution
    private UserGenerator userGen;			// user generator
    
    private static final int maxUsers = 10;	// max concurrently trackable users
    private int[] users;					// users currently being tracked
    private int userMap[];					// map kinect users to actor numbers
    private int lastUser;					// monotonic user counter
    
    private int debugLevel;					// level of debug output

    private static final String NITElicense = "0KOIk2JeIBYClPWVnMoRKn5cdY4=";
    private static final int MIN_Z = 50;	// anything less is bogus
    
    public UserSensor() {
        try {
        	// manually create the context (eliminate dependency on cfg file)
        	context = new Context();
        	License license = new License("PrimeSense", NITElicense);
        	context.addLicense(license);
        	
        	// enable depth reporting (needed by user tracking)
            depthGen = DepthGenerator.create(context);
            DepthMetaData depthMD = depthGen.getMetaData();
            width = depthMD.getFullXRes();
            height = depthMD.getFullYRes();
            
            // enable user tracking as well
            userGen = UserGenerator.create(context);          
            userGen.getNewUserEvent().addObserver(new NewUserObserver());
            userGen.getLostUserEvent().addObserver(new LostUserObserver());	
            
			context.startGeneratingAll();
        } catch (GeneralException e) {
        	System.out.println("Kinect initialization failure");
            e.printStackTrace();
        }

        debugLevel = 0;
        users = null;
        userMap = new int[maxUsers];
        lastUser = 0;
    }
    
    /**
     * a new user has been detected
     */
	class NewUserObserver implements IObserver<UserEventArgs> {
		public void update(IObservable<UserEventArgs> observable,
				UserEventArgs args) {
			int uid = args.getId();
			if (uid < maxUsers) {
				userMap[uid] = ++lastUser;
				if (debugLevel > 0)
					System.out.println("Detected new user[" + uid + "] = " + lastUser);
			}
		}
	}
	
	/**
	 * a user has fallen out of sensor memory
	 */
	class LostUserObserver implements IObserver<UserEventArgs> {
		public void update(IObservable<UserEventArgs> observable,
				UserEventArgs args) {
			int uid = args.getId();
			userMap[uid] = 0;
			if (debugLevel > 0)
				System.out.println("Lost user[" + uid + "]");
		}
	}
	

    /**
     * @return number of users currently known to the sensor
     */
	public int numUsers() {
		return (users == null) ? 0 : users.length;
	}
	
	/**
	 * return the actor ID of the n'th user
	 * 		OpenNI user numbers are current and dense (0-n)
	 * 		whereas actor ID's are monotonically increasing
	 * 
	 * @param	n	relative current user number
	 * @return	monotonically increasing actor ID #
	 */
	public int actor(int n) {
		if (n >= users.length)
			return( -1 );
		int u = users[n];
		if (u >= maxUsers)
			return( -1 );
		return( userMap[u]);
	}

	/**
	 * get center of mass coordinates for specified user
	 * 
	 * @param	i	relative current user number
	 * @return	center-of-mass coordinates (or null)
	 */
	public Coord getCoM(int i) {
		Point3D com;
		try {
			com = depthGen.convertRealWorldToProjective(userGen.getUserCoM(users[i]));
		} catch (StatusException e) {
			System.out.println("Error reading user " + i + "CoM");
			e.printStackTrace();
			return null;
		}
		
		// make sure we don't report non-locations
		if (com == null || com.getZ() < MIN_Z)
			return null;
		
		return( new Coord(com.getX(), com.getY(), com.getZ()));
	}

    /**
     * read a new depth and user frame
     */
	public void update() {
		try {
			@SuppressWarnings("unused")
			DepthMetaData depthMD = depthGen.getMetaData();
			context.waitAnyUpdateAll();
			users = userGen.getUsers();
        } catch (GeneralException e) {
        	System.out.println("Error reading depth/users");
            e.printStackTrace();
        }
	}

	 public void debug(int debug) {
		 debugLevel = debug;
	 }
}