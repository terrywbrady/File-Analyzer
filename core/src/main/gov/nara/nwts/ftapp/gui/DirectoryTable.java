package gov.nara.nwts.ftapp.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import gov.nara.nwts.ftapp.filetest.ActionRegistry;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.importer.ImporterRegistry;
import gov.nara.nwts.ftapp.ActionResult;
import gov.nara.nwts.ftapp.MyDirectoryFilter;
import gov.nara.nwts.ftapp.MyFilenameFilter;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.filetest.FileTest;
import gov.nara.nwts.ftapp.filter.FileTestFilter;
import gov.nara.nwts.ftapp.ftprop.FTProp;

/**
 * GUI driver for the File Analyzer which spawns FileTests in a worker thread.
 * User selections are retained from session to session in a Java preferences object.
 * This application was originally created by Terry Brady in NARA's Digitization Services Branch.
 * @author TBrady
 *
 */
public class DirectoryTable extends FTDriver {
	public static final int TAB_CRITERIA = 0;
	public static final int TAB_IMPORT = 1;
	public static final int TAB_PROGRESS = 2;
	public static final int TAB_DETAILS = 3;
	public static final int TAB_MERGE = 4;
	public static final String[] TABS = {
		"Criteria",
		"Import",
		"Progress",
		"Details",
		"Merge"
	};
	

	
	JFrame frame;
	public JFrame getFrame() {return frame;}
	ActionRegistry actionRegistry;
	ImporterRegistry importerRegistry;
	JLabel countLabel;
	JTabbedPane tabs;
	ArrayList<File> recent;
	ArrayList<File> recentImport;
	
	CriteriaPanel criteriaPanel;
	ImportPanel importPanel;
	ProgressPanel progressPanel;
	DetailsPanel detailsPanel;
	MergePanel mergePanel;
	SummaryPanel summaryPanel;
	
	Preferences preferences;
	public String title = "File Analyzer and Metadata Harvester";
	public String message = "This tool is intended to provide a toolkit of features for analyzing files and harvesting metadata.";
	
	public Preferences getPreferences() {
		return preferences;
	}
	
	public boolean getImporterForceKey() {
		return importPanel.forceKey.isEnabled() && importPanel.forceKey.isSelected();
	}
	
	protected ActionRegistry getActionRegistry() {
		return new ActionRegistry(this, modifyAllowed);
	}

	protected ImporterRegistry getImporterRegistry() {
		return new ImporterRegistry(this);
	}

	protected void refreshTitle() {
		frame.setTitle(title);
	}
	
	protected boolean modifyAllowed;
	public DirectoryTable(File root, boolean modifyAllowed) {
		super(root);
		this.modifyAllowed = modifyAllowed;
		frame = new JFrame(title);
		preferences = Preferences.userNodeForPackage(DirectoryTable.class);
		recent = new ArrayList<File>();
		for(int i=20-1; i>=0;i--){
			String s = preferences.get("recent"+i,"");
			if (s!="") recent.add(new File(s));
		}
		recentImport = new ArrayList<File>();
		for(int i=20-1; i>=0;i--){
			String s = preferences.get("recentImport"+i,"");
			if (s!="") recentImport.add(new File(s));
		}
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		actionRegistry = getActionRegistry();
		importerRegistry = getImporterRegistry();
		tabs = new JTabbedPane();
		frame.add(new JScrollPane(tabs), BorderLayout.CENTER);
		criteriaPanel = new CriteriaPanel(this);
		tabs.add(criteriaPanel, TABS[TAB_CRITERIA]);
		importPanel = new ImportPanel(this);
		tabs.add(importPanel, TABS[TAB_IMPORT]);
		progressPanel = new ProgressPanel(this);
		tabs.add(progressPanel, TABS[TAB_PROGRESS]);
		detailsPanel = new DetailsPanel(this);
		tabs.add(detailsPanel, TABS[TAB_DETAILS]);
		mergePanel = new MergePanel(this);
		tabs.add(new JScrollPane(mergePanel), TABS[TAB_MERGE]);
		tabs.setEnabledAt(TAB_DETAILS, false);
		tabs.setEnabledAt(TAB_MERGE, false);
		tabs.setEnabledAt(TAB_PROGRESS, true);
		JMenuBar jmb = new JMenuBar();
		frame.add(jmb, BorderLayout.NORTH);
		JMenu menu = new JMenu("File Analyzer");
		JMenuItem jmi = new JMenuItem("About File Analyzer");
		menu.add(jmi);
		jmi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(DirectoryTable.this.frame,
				message +
				"\n\nSee https://github.com/Georgetown-University-Libraries/File-Analyzer/wiki" +
				"\nfor details about the application and for a user documentation. " +
				"\n--------------------------------- " +
				"\nThis application has been derived from code created by \n" +
				"the National Archives and Records Administration (NARA).\n" +
				"Please see the accompanying README file for more information." +
				"\n\nContact: OpenGov@nara.gov" +
				"\n\nhttps://github.com/usnationalarchives/File-Analyzer", 
				"About File Analyzer", JOptionPane.INFORMATION_MESSAGE);
			}});
		jmi = new JMenuItem("Log errors to a file");
		menu.add(jmi);
		jmi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				String proptmpdir = System.getProperty("java.io.tmpdir");
				File tmpdir = new File(proptmpdir);
				File tmpfile = new File(tmpdir, "FileAnalyzer.log");
				try {
					PrintStream ps = new PrintStream(tmpfile);
					String message = String.format("Redirecting output to %s", tmpfile.getAbsolutePath());
					System.err.println(message);
					JOptionPane.showMessageDialog(frame, message, "Looging to File", JOptionPane.INFORMATION_MESSAGE);
					System.setErr(ps);
					System.setOut(ps);
					System.out.println(String.format("FileAnalyzer Output: %s\n", SimpleDateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime())));
				} catch (FileNotFoundException e) {
				}
			}});
		jmi = new JMenuItem("Enable Batch Processing");
		menu.add(jmi);
		jmi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				progressPanel.batchp.setVisible(true);
			}});
		jmb.add(menu);
		jmb.add(menu);
		
		criteriaPanel.rootLabel.setText(root == null ? preferences.get("root", "") : root.getAbsolutePath());
		setSelectedFile();
		setFilters();
		frame.pack();

		frame.setVisible(true);
		
	}

	public boolean followLinks() {
		return this.criteriaPanel.followLinks.isSelected();
	}
	
	/**
	 * Reset the filter and property tabs based on the selected FileTest.
	 */
	void setFilters() {
		FileTest fileTest = (FileTest)criteriaPanel.actions.getSelectedItem();
		setPreference("action", fileTest.toString());
		criteriaPanel.description.setText(fileTest.getDescription());
		criteriaPanel.description.setCaretPosition(0);
		criteriaPanel.filterTabs.removeAll();
		for(Iterator<FileTestFilter>i=fileTest.getFilters().iterator(); i.hasNext();){
			FileTestFilter filter = i.next();
			new FilterPanel(this, filter);
		}
		criteriaPanel.propPanel.removeAll();
		Box b = new Box(BoxLayout.Y_AXIS);
		b.add(Box.createHorizontalStrut(500));
		criteriaPanel.propPanel.add(b);
		List<FTProp> myprops = fileTest.getPropertyList();
		criteriaPanel.propFilter.setEnabledAt(2, false);
		for(FTProp myprop: myprops) {
			b.add(DirectoryTable.getPropPanel(criteriaPanel, myprop));
			criteriaPanel.propFilter.setEnabledAt(2, true);
		}
		//criteriaPanel.propPanel.repaint();
	}
	
	public void setPreference(String path, String value){
		preferences.put(path, value);
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}		
	}

	public void setSelectedFile() {
		criteriaPanel.analyze.setEnabled(false);
		root = new File(criteriaPanel.rootLabel.getText());
		if (root == null) return;
		if (root.exists()) {
			criteriaPanel.analyze.setEnabled(batchItems.isEmpty());
		}
	}

	
	void checkTabs() {
		tabs.setEnabledAt(TAB_MERGE, (tabs.getTabCount() > TAB_MERGE + 2));
		if (tabs.getTabCount() == TAB_MERGE + 1) {
			tabs.setSelectedIndex(TAB_CRITERIA);
		}
	}
	
	/** 
	 * Modify GUI controls to indicated that a FileTest is in progress
	 */
	public void traversalStart() {
		if (!isBatchProcessing){
			setPreference("root", root.getAbsolutePath());
			for(int i=0; ((i<recent.size())&&(i<20));i++){
				setPreference("recent"+i,recent.get(recent.size()-i-1).getAbsolutePath());
			}			
		}
		recent.remove(root);
		recent.add(root);
		progressPanel.status.setText("");
		progressPanel.cancel.setEnabled(true);
		progressPanel.progress.setValue(0);
		progressPanel.setProgressColor(true);
		tabs.setSelectedIndex(TAB_PROGRESS);
		tabs.setEnabledAt(TAB_CRITERIA, false);
		tabs.setEnabledAt(TAB_IMPORT, false);
		tabs.setEnabledAt(TAB_DETAILS, true);
		frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
	}
	
	public boolean overwrite() {
		return false;
	}
	
	public boolean isSave() {
		return criteriaPanel.autoSave.isSelected();
	}
	
	public void logBatchSize() {
		progressPanel.batchCount.setText(batchItems.size()+" items");		
	}
	public void batchLoaded() {
		logBatchSize();
	}
	
	/**
	 * Display the results of a FileTest to a user
	 * @param res
	 */
	public void logResult(ActionResult res) {
		saveResult(res);
		countLabel.setText(res.types.size()+" items.  "+ ndurf.format(res.duration) + " seconds");
		progressPanel.logTest(res.name,res.action, res.root, res.completed, fileTraversal.getNumProcessed(), res.duration, lastSavedFile);
		logBatchSize();

		if (!isBatchProcessing){
			showSummary(
					res.name,
					res.longname,
					res.details,
					res.types,
					res.completed,
					countLabel.getText()
			);
			
		}
	}
	
	/**
	 * Release the GUI controls to indicate the completion of a FileTest
	 */
	public void traversalEnd(ActionResult res) { 
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			//e.printStackTrace();
		}
		
		progressPanel.cancel.setEnabled(false);
		detailsPanel.jt.repaint();
		progressPanel.setProgressColor(res.completed);
		progressPanel.cancel.setEnabled(false);
		
		logResult(res);
		
		tabs.setEnabledAt(TAB_CRITERIA, true);
		tabs.setEnabledAt(TAB_IMPORT, true);
		frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		processBatch();
		
	}
	
	public String getSaveFileName() {
		return this.criteriaPanel.fctf.getText().trim();
	}
	public File getSaveDir() {
		return new File(this.criteriaPanel.fsc.tf.getText());
	}
	public File save(String fname, StatsItemConfig details, TreeMap<String, Stats> mystats, boolean completed) {
		File f = super.save(fname, details, mystats, completed);
		this.criteriaPanel.fctf.setText("");
		return f;
	}
	
	public void showSummary(String name, String longname, StatsItemConfig details, TreeMap<String,Stats>types, boolean completed, String note){
		summaryPanel = new SummaryPanel(this, longname, note);
		summaryPanel.showStats(details,types); 
		tabs.add(summaryPanel, name);
		if (completed) {
			tabs.setSelectedComponent(summaryPanel);
		}
		checkTabs();		
	}
	
	public FilenameFilter getFileFilter(FileTest fileTest) {
		FilterPanel fp = (FilterPanel)criteriaPanel.filterTabs.getSelectedComponent();
		if (fp == null) {
			return new MyFilenameFilter(
		    	criteriaPanel.ignorePeriods.isSelected(),
		    	"",
		    	false,
		    	"",
		    	false,
		    	"",
		    	false,
		    	"",
		    	false,
		    	fileTest.isTestFiles()
			);
		}
	    return new MyFilenameFilter(
	    	criteriaPanel.ignorePeriods.isSelected(),
	    	fp.prefix.getText(),
	    	fp.rePrefix.isSelected(),
	    	fp.contains.getText(),
	    	fp.reContains.isSelected(),
	    	fp.suffix.getText(),
	    	fp.reSuffix.isSelected(),
	    	fp.exclusion.getText(),
	    	fp.reExclusion.isSelected(),
	    	fileTest.isTestFiles() 
		); 
	}
	
	public FilenameFilter getDirectoryFilter(){
		return new MyDirectoryFilter(
			criteriaPanel.ignorePeriods.isSelected()
		);    	
	}
	
	public void report(String s) {
		progressPanel.status.append(s);
		progressPanel.status.append("\n");
		progressPanel.status.setCaretPosition(progressPanel.status.getText().length());
	}

	public void importFileStart() {
		frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));		
	}
	public void importFileEnd(ActionResult res) {
		logResult(res);
		frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));	
	}
	
	/**
	 * Prepare the FileAnalyzer to process an input directory.
	 * This method may be invoked on a single directory or it may be repeatedly invoked on a batch of directories.
	 */
	public void initiateFileTest(File input, File output) {
		root = input;
		criteriaPanel.fctf.setText(output.getName());
		criteriaPanel.fsc.tf.setText(output.getParentFile().getAbsolutePath());
		initiateFileTest();
	}
	/**
	 * Spawn a worker thread to perform a file test
	 */
	public void initiateFileTest() {
		traversalStart();
		GuiFileTraversalSW gftSW = new GuiFileTraversalSW(this, detailsPanel.tm);
		fileTraversal = gftSW.traversal;
		fileTraversal.setTraversal(
				(FileTest)criteriaPanel.actions.getSelectedItem(),
				(Integer)criteriaPanel.limit.getSelectedItem()
			);
		gftSW.execute();
	}

	/**
	 * Begin processing a batch; the batch will continue to run until all items have been processed, until processing has been cancelled or until batch processing has been paused.
	 */
	public void batchStart(){
		criteriaPanel.autoSave.setSelected(true);
		super.batchStart();
		progressPanel.clearBatch.setEnabled(false);
		progressPanel.doBatch.setEnabled(false);
		progressPanel.pause.setEnabled(true);
		progressPanel.pause.setSelected(false);
		criteriaPanel.analyze.setEnabled(false);
	}
	public void batchComplete(){
		super.batchComplete();
		progressPanel.clearBatch.setEnabled(true);
		progressPanel.doBatch.setEnabled(true);
		progressPanel.pause.setEnabled(false);
		setSelectedFile();
		resetTest();
	}
	public void resetTest() {
		FileTest next = ((FileTest)criteriaPanel.actions.getSelectedItem()).resetOption();
		if (next != null) {
			criteriaPanel.actions.setSelectedItem(next);
			setFilters();
		}		
	}
	
	public void pauseBatch(){
		super.pauseBatch();
		progressPanel.clearBatch.setEnabled(true);
	}
	public void reportSave(File f) {	
	}
	
	static JPanel getPropPanel(JPanel parent, FTProp myprop) {
		JPanel pp = new JPanel(new BorderLayout());
		
		String desc = myprop.describe();
		if (desc == null) desc = "";
		if (!desc.isEmpty()) {
			JTextArea description = new JTextArea(myprop.describe());
			description.setMargin(new Insets(1,1,1,1));
			description.setEditable(false);
			description.setLineWrap(true);
			description.setWrapStyleWord(true);
			description.setBackground(parent.getBackground());
			description.setFont(description.getFont().deriveFont(Font.ITALIC));
			pp.add(description, BorderLayout.NORTH);			
		}
		JComponent c = myprop.getEditor();
		pp.add(c, BorderLayout.CENTER);
		pp.setBorder(BorderFactory.createTitledBorder(myprop.getName()));
		return pp;
	}

}
