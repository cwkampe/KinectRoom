Programs and Parameters:

	RegionBuilder.jar ... use Kinect and GUI to define regions
		--regions=file ... starting point for region defintions
		
	RuleBuilder.jar ... use region file and GUI to define rules
		--regions=file ... region definitions
		--rules=file ... starting point for rule definitions
		
	KinectedSpace.jar ... use Kinect, rules and regions for a performance
		--regions=file ... region definitions
		--rules=file ... rule definitions
		--base=prefix ... prefix to be pre-pended to sound/image file names
		--height=pixels ... image display window height
		--width=pixels ... image display window width
		--test=# ... number of test users to run through the regions
		--delay=# ... number of ms to wait between steps (in test mode)
		
	KinectedSpaceApplet.jar ... applet version of the performance program
		takes the same parameters as the application
		(I'm not sure whether or not having an applet is worth anything)
	
    common parameters
    	--help ... get a usage message
    	--debug=# ... set debug output level
    		1: configuration and trigger events
    		2: all interesting events
    		3: painfully verbose (for debugging)
    	
    	note that the introduction characters (e.g. --, -, /)
    		are unimportant, as the program is really just
    		looking for [stuff][name]=[value]
    		
Running applications (from command line prompt or script)

	java -jar <application.jar> [arguments] ...

	examples:
		java -jar RuleBuilder.xml --regions=/Users/Mark/Regions.xml
		java -jar KinectedSpace.jar --regions=Regions.xml --rules=Rules.xml 
				--base=http://googlepages.com/markkampe/test --test=2
				
	NOTE: that local filenames must be specified in unix (v.s. DOS) format
		no leading "C:"
		using forward (/) rather than back (\) slashes as separators
	
Configuration Files:
    Regions.xml ... defines the regions to be monitored
    Rules.xml ... defines operations to perform when regions are entered/exited
    
KNOWN ISSUES:
	
    APPLICATION
	It looks like the class path that comes with the app jar knows where
	the OpenNI library has been installed.  We either have to always install
	it in that place or figure out how to override the classpath for a 
	distributed jar

    APPLET
	I haven't noticed any benefits that compensate for the limitations
	(in the ability to use files and control your primary window), so
	I have lost interest in the applet.  But If you want it, I can get
	it working again pretty quickly.
