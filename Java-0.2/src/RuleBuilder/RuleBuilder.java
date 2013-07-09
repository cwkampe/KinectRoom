package RuleBuilder;

/**
 * main class
 * 		process command line arguments
 * 		instantiate the rulebuilder GUI
 * 
 * @author markk
 */
public class RuleBuilder {

	private static String usage[] = {
		"RuleBuilder [switches] [regions-file]",
		"    switches:",
		"        --regions=regions-file",
		"        --rules=rules-file",
		"        --debug=#"
	};
	
	public static void main(String args[]) {
		String regionFile = null;
		String rulesFile = null;
		int debugLevel = 1;		// major file events only
		
		// process the command line arguments (why isn't there a standard for this?)
		for( int i = 0; i < args.length; i++ ) {
			if (args[i].contains("rules=")) {
				rulesFile = args[i].substring(args[i].indexOf('=') + 1);
			} else if (args[i].contains("regions=")) {
				regionFile = args[i].substring(args[i].indexOf('=') + 1);
			} else if (args[i].contains("debug=")) {
				debugLevel = Integer.parseInt(args[i].substring(args[i].indexOf('=') + 1));
			} else if (args[i].contains("help") || args[i].contains("?")) {
				usage();
				return;
			} else if (regionFile == null) {
				regionFile = args[i];
			} else {
				usage();
				return;
			}
		}
		
		// kick off the rule-builder GUI
		new MainScreen( regionFile, rulesFile, debugLevel );
	}

	/**
	 * print out a usage message
	 */
	private static void usage() {
		for( int i = 0; i < usage.length; i++ )
			System.out.println(usage[i]);
	}
}
