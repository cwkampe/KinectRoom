package RuleBuilder;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import ActiveSpace.Region;
import ActiveSpace.RegionEvent;
import ActiveSpace.Rule;
import ActiveSpace.Space;

/**
 * This class implements all of the GUI widgetry
 */
public class MainScreen extends JFrame 
						implements	ActionListener, 
									WindowListener {
	
	private Space space;		// space for regions and rules
	private int debugLevel;		// desired level of diagnostic output
	private boolean changes;	// have we made any changes
	
	private static final long serialVersionUID = 0xdeadbeef;	// this is stupid

	// main panel GUI widgets
	private Container mainPane;
	private JComboBox<String> regionSelector;
	private JComboBox<String> eventSelector;
	private JComboBox<String> stateSelector;
	private JComboBox<String> nextSelector;
	private JTextField eventName;
	private JTextField imageFile;
	private JTextField soundFile;
	private JTextField textFile;
	private JMenuItem fileSaveAs;
	private JMenuItem fileExit;
	private JMenuItem fileLoadRules;
	private JMenuItem fileLoadRegions;
	private JButton imageButton;
	private JButton noImageButton;
	private JButton soundButton;
	private JButton noSoundButton;
	private JButton textButton;
	private JButton noTextButton;
	private JButton createButton;
	private JTextArea ruleList;
	
	private JFileChooser chooser;
	private FileNameExtensionFilter imageFilter;
	private FileNameExtensionFilter soundFilter;
	private FileNameExtensionFilter textFilter;
	private FileNameExtensionFilter xmlFilter;
	
	// pseudo-tunables
	private static final int B = 10;			//	default widget border
	private static final int TEXT_WIDTH = 25;	// default text field
	private static final int LIST_HEIGHT = 20;	// default rule list height
	private static final int LIST_WIDTH = 80;	// default rule list width
	


	/**
	 * create all the main-screen widgetry
	 * 
	 * @param s		Space containing the regions and rules
	 * @param path	to directory containing media files
	 */
	public MainScreen( String regionFile, String rulesFile, int debug )  {
		
		this.space = new Space();		// create an ActiveSpace
		this.debugLevel = debug;		// figure out how verbose to be
		space.debug(debugLevel);		// only the most basic debug info
		
		// get a handle on our primary window
		mainPane = getContentPane();
		addWindowListener( this );		// so I can handle window close events
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		// create the main panel widgetry
		createMenus();			// create the menu hierarchy
		createList();			// create a list of defined rules
		createEditor();			// create the rule editor panel
		setVisible( true );		// make this all visible
		pack();					// validate widgets, compute display size
		
		// set up a single file chooser (so we can remember directories)
		chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		imageFilter = new FileNameExtensionFilter("Images", "jpg", "jpeg", "JPG");
		chooser.addChoosableFileFilter(imageFilter);
		soundFilter = new FileNameExtensionFilter("Sounds", "mp3", "au", "MP3");
		chooser.addChoosableFileFilter(soundFilter);
		textFilter = new FileNameExtensionFilter("Text", "txt", "text", "TXT");
		chooser.addChoosableFileFilter(textFilter);
		xmlFilter = new FileNameExtensionFilter("XML", "xml", "XML");
		chooser.addChoosableFileFilter(xmlFilter);
		
		// initialize the space we'll be working on
		if (regionFile != null)
			loadRegions(regionFile);
		if (rulesFile != null)
			loadRules(rulesFile);
		
		changes = false;			// no changes have yet been made
	}
	
	
	/**
	 * create the hierarchy of menus that will drive most 
	 * of our actions
	 */
	private void createMenus() {
		
		// create File menu
		fileLoadRegions = new JMenuItem("Load Regions");
		fileLoadRegions.addActionListener(this);
		fileLoadRules = new JMenuItem("Load Rules");
		fileLoadRules.addActionListener(this);
		fileSaveAs = new JMenuItem("Save as");
		fileSaveAs.addActionListener(this);
		fileExit = new JMenuItem("Exit");
		fileExit.addActionListener(this);
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(fileLoadRegions);
		fileMenu.add(fileLoadRules);
		fileMenu.add(fileSaveAs);
		fileMenu.add( new JSeparator() );
		fileMenu.add(fileExit);
		
		// assemble the menu bar
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		setJMenuBar( menuBar );	
	}
	
	/**
	 * create a scrollable list of existing rules
	 */
	private void createList() {
		ruleList = new JTextArea(LIST_HEIGHT, LIST_WIDTH);
		ruleList.setEditable(false);
		JScrollPane p = new JScrollPane(ruleList);
		mainPane.add(p, BorderLayout.CENTER);
		ruleList.setText((String) space.listRules());
	}
	
	/**
	 * create the controls for entering new rules
	 */
	private void createEditor() {
		JPanel e;	// the main panel for the editor
		JPanel h;	// 2nd level horizontal panels
		JPanel p;	// 3rd level vertical panels
		JPanel b2;	// 4th level dual button panels
		JLabel l;	// 4th level widget labels within vertical stacks
		
		// create the editor panel
		e = new JPanel();
		e.setLayout(new BoxLayout(e, BoxLayout.Y_AXIS));
		e.setBorder(BorderFactory.createEmptyBorder(B,B,B,B));
		
		// create the top row of the rule creation widget
		h = new JPanel();
		h.setLayout(new BoxLayout(h, BoxLayout.X_AXIS));
		
		// region selection widget
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createEmptyBorder(B,B,B,B));
		l = new JLabel("Region");
		p.add(l);
		regionSelector = new JComboBox<String>();
		regionSelector.addActionListener(this);
		p.add(regionSelector);
		h.add(p);
		
		// event type selector widget
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createEmptyBorder(B,B,B,B));
		l = new JLabel("Event");
		p.add(l);
		String eventChoices[] = { "ENTRY", "EXIT" };
		eventSelector = new JComboBox<String>(eventChoices);
		eventSelector.setSelectedIndex(0);
		eventSelector.addActionListener(this);
		p.add(eventSelector);
		h.add(p);
		
		// event name widget text field
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createEmptyBorder(B,B,B,B));
		l = new JLabel("Rule Name");
		p.add(l);
		String s = regionSelector.getSelectedItem() + " " + eventSelector.getSelectedItem();
		eventName = new JTextField(s, TEXT_WIDTH);
		p.add(eventName);
		h.add(p);
		
		// state selector widgets
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createEmptyBorder(B,B,B,B));
		l = new JLabel("state");
		p.add(l);
		String stateChoices[] = { "-", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
		stateSelector = new JComboBox<String>(stateChoices);
		stateSelector.setSelectedIndex(0);
		p.add(stateSelector);
		h.add(p);
		
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createEmptyBorder(B,B,B,B));
		l = new JLabel("next");
		p.add(l);
		nextSelector = new JComboBox<String>(stateChoices);
		nextSelector.setSelectedIndex(0);
		p.add(nextSelector);
		h.add(p);
		
		e.add(h);	// attach this row to the editor panel
		
		// create the second row of file selection widgets
		h = new JPanel();
		h.setLayout(new BoxLayout(h, BoxLayout.X_AXIS));
		
		// image selection panel
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createEmptyBorder(B,B,B,B));
		l = new JLabel("Image");
		p.add(l);
		imageFile = new JTextField( TEXT_WIDTH );
		p.add(imageFile);
		b2 = new JPanel();
		b2.setLayout(new BoxLayout(b2, BoxLayout.X_AXIS));
		imageButton = new JButton("Choose");
		imageButton.addActionListener(this);
		b2.add(imageButton);
		noImageButton = new JButton("Cancel");
		noImageButton.addActionListener(this);
		b2.add(noImageButton);
		p.add(b2);
		h.add(p);
		
		// sound selection panel
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createEmptyBorder(B,B,B,B));
		l = new JLabel("Sound");
		p.add(l);
		soundFile = new JTextField( TEXT_WIDTH );
		p.add(soundFile);
		b2 = new JPanel();
		b2.setLayout(new BoxLayout(b2, BoxLayout.X_AXIS));
		soundButton = new JButton("Choose");
		soundButton.addActionListener(this);
		b2.add(soundButton);
		noSoundButton = new JButton("Cancel");
		noSoundButton.addActionListener(this);
		b2.add(noSoundButton);
		p.add(b2);
		h.add(p);
		
		// text selection panel
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createEmptyBorder(B,B,B,B));
		l = new JLabel("Text");
		p.add(l);
		textFile = new JTextField( TEXT_WIDTH );
		p.add(textFile);
		b2 = new JPanel();
		b2.setLayout(new BoxLayout(b2, BoxLayout.X_AXIS));
		textButton = new JButton("Choose");
		textButton.addActionListener(this);
		b2.add(textButton);
		noTextButton = new JButton("Cancel");
		noTextButton.addActionListener(this);
		b2.add(noTextButton);
		p.add(b2);
		h.add(p);
		
		e.add(h);	// attach this row to the editor panel
		
		// create rule button
		h = new JPanel();
		createButton = new JButton("Create Rule");
		createButton.addActionListener(this);
		h.add(createButton);
		
		e.add(h);	// attach this row to the editor panel
		
		// and add it all to the main pane
		mainPane.add(e, BorderLayout.SOUTH);
	}
	
	/**
	 * We have a single action listener for all of the interactive widgets,
	 * but all of the non-widget handling is done in per-operation subroutines.
	 */
	public void actionPerformed( ActionEvent e ) {
		Object o = e.getSource();
		
		// menu->File->load regions
		if (o == fileLoadRegions) {
			chooser.setFileFilter(xmlFilter);
			int retval = chooser.showOpenDialog(this);
			if (retval == JFileChooser.APPROVE_OPTION) {
				String filename = chooser.getSelectedFile().getAbsolutePath();
				String correctedName = canonize(filename);
				loadRegions(correctedName);
			} 
			return;
		}
		
		// menu->File->load rules
		if (o == fileLoadRules) {
			chooser.setFileFilter(xmlFilter);
			int retval = chooser.showOpenDialog(this);
			if (retval == JFileChooser.APPROVE_OPTION) {
				String filename = chooser.getSelectedFile().getAbsolutePath();
				String correctedName = canonize(filename);
				loadRules(correctedName);
			} 
			return;
		}
		
		// menu->File->save
		if (o == fileSaveAs) {
			saveRules();
			return;
		}
		
		// menu->File->exit
		if (o == fileExit) {
			shutdown();
			return;
		}
		
		// selected a region or event type in the editor window
		if (o == regionSelector || o == eventSelector) {
			String r = (String) regionSelector.getSelectedItem();
			String t = (String) eventSelector.getSelectedItem();
			eventName.setText(r + " " + t);
			return;
		}
		
		// choose image file button pressed
		if (o == imageButton) {
			chooser.setFileFilter(imageFilter);
			int retval = chooser.showOpenDialog(this);
			if (retval == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				imageFile.setText(file.getName());
			} else 
				imageFile.setText("");
			return;
		} else if (o == noImageButton) {
			imageFile.setText("cancel");
			return;
		}
		
		// choose sound file button pressed
		if (o == soundButton) {
			chooser.setFileFilter(soundFilter);
			int retval = chooser.showOpenDialog(this);
			if (retval == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				soundFile.setText(file.getName());
			} else 
				soundFile.setText("");
			return;
		} else if (o == noSoundButton) {
			soundFile.setText("cancel");
			return;
		}
		
		// choose text file button pressed
		if (o == textButton) {
			chooser.setFileFilter(textFilter);
			int retval = chooser.showOpenDialog(this);
			if (retval == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				textFile.setText(file.getName());
			} else 
				textFile.setText("");
			return;
		} else if (o == noTextButton) {
			textFile.setText("cancel");
			return;
		}
		
		// create rule button pressed
		if (o == createButton) {
			createRule();
			ruleList.setText(space.listRules());
			return;
		}
	}
	

	/* 
	 * We have a full complement of WindowListener entry points, but
	 * the only one we actually care about is the "closing" (he clicked the X),
	 * which we catch so that we can confirm before shutting down.
	 */
	public void windowClosing(WindowEvent e) { shutdown(); }
	public void windowActivated(WindowEvent arg0) {	}
	public void windowClosed(WindowEvent arg0) {}
	public void windowDeactivated(WindowEvent arg0) {}
	public void windowDeiconified(WindowEvent arg0) {}
	public void windowIconified(WindowEvent arg0) {}
	public void windowOpened(WindowEvent arg0) {}
	
	/*
	 * the routines that follow implement the real operations
	 * on the space (loading, saving and creating regions and rules)
	 */
	
	/**
	 * load a set of regions into our space
	 * 
	 * @param filename
	 */
	private void loadRegions( String filename ) {
		try {
			// load in the regions file
			space.readRegions(filename);
			
			// and then update the regions selector widget to know about them
			regionSelector.removeAllItems();
			int n = space.numRegions();
			for( int i = 0; i < n; i++ )
				regionSelector.addItem(space.getRegion(i).getName());
			if (n > 0)
				regionSelector.setSelectedIndex(0);
		} catch (Exception e) {
			JOptionPane.showMessageDialog( mainPane, 
					e.getMessage(),
					"ERROR LOADING REGION DEFINITIONS",
					JOptionPane.ERROR_MESSAGE);
			// e.printStackTrace();
		}
	}
	
	/**
	 * load a set of rules into our space
	 * 
	 * @param filename
	 */
	private void loadRules( String filename ) {
		try {
			// load in the rules file
			space.readRules(filename);
			
			// and then update the rules display to know about them
			ruleList.setText(space.listRules());
		} catch (Exception e) {
			JOptionPane.showMessageDialog( mainPane, 
					e.getMessage(),
					"ERROR LOADING RULES",
					JOptionPane.ERROR_MESSAGE);
			// e.printStackTrace();
		}
	}
	
	/**
	 * process a shut-down request, prompting for save if appropriate
	 */
	private void shutdown() {
		while (changes) {
			int choice = JOptionPane.showConfirmDialog(mainPane, "Save changes?");
			if (choice == JOptionPane.CANCEL_OPTION)
				return;
			if (choice == JOptionPane.YES_OPTION)
				saveRules();
			if (choice == JOptionPane.NO_OPTION)
				break;
		}
		System.exit(0);
	}
	
	/**
	 * select an output file and save to it
	 * 
	 * side effect: save clears the changes boolean
	 */
	private void saveRules() {
		// select an output file
		chooser.setFileFilter(xmlFilter);
		chooser.setSelectedFile( new File( "Rules.xml" ));
		if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			String chosen = chooser.getSelectedFile().getAbsolutePath();
			
			// save the current rule set to it
			if (debugLevel > 0)
				System.out.println("saving rules to " + chosen);
			FileWriter output;
			try {
				output = new FileWriter( chosen );
				output.write(space.rulesToXML());
				output.close();
				changes = false;
			} catch (IOException e) {
				JOptionPane.showMessageDialog( mainPane, 
						e.getMessage(),
						"ERROR UPDATING FILE",
						JOptionPane.ERROR_MESSAGE);

			}
		}
	}
	
	/**
	 * pull the values out of the editor panel to create a new rule
	 */
	private void createRule() {
		// create the RegionEvent callback handler
		RegionEvent r = new RegionEvent();
		String s = imageFile.getText();
		if (s != null && !s.equals(""))
			r.setImage(s);
		s = soundFile.getText();
		if (s != null && !s.equals(""))
			r.setSound(s);
		s = textFile.getText();
		if (s != null && !s.equals(""))
			r.setText(s);
		
		// gather the rule attributes and create the rule
		String name = (String) eventName.getText();
		Region region = space.getRegion((String) regionSelector.getSelectedItem());
		Rule.EventType etype = Rule.eventType((String) eventSelector.getSelectedItem());
		s = (String) stateSelector.getSelectedItem();
		int iState = s.equals("-") ? -1 : Integer.parseInt(s);
		s = (String) nextSelector.getSelectedItem();
		int nState = s.equals("-") ? -1 : Integer.parseInt(s);
		new Rule(name, region, etype, iState, nState, r);	
		
		// and then update the rules display to know about the new rule
		ruleList.setText(space.listRules());
		changes = true;
	}
	
	/**
	 * KLUGE - The file name selection dialogs return local
	 * 		file names, but the region/rule readers need URI's,
	 * 		and Window's file names don't qualify.  A little 
	 * 		research failed to turn up a portable converter,
	 * 		so I am (for now) kluging my own.
	 * 
	 * @param original	local file name
	 * @return	new form, suitable for use as a URI
	 */
	static String canonize(String original) {
		
		// replace any back-slashes with slashes
		String fixed = original.replaceAll("\\\\", "/");
		
		System.out.println("canonize(" + original + ") = " + fixed);
		
		return( fixed );
	}
}
