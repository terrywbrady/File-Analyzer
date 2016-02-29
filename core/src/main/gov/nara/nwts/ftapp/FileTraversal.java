package gov.nara.nwts.ftapp;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.regex.Pattern;


import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filetest.FileTest;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;
import gov.nara.nwts.ftapp.stats.Stats;

/**
 * Base class for handling file traversal logic.  
 * This is used by the command line version of the application.
 * Some behaviors are further enhanced in the GUI application @linkplain gov.nara.nwts.ftapp.gui.GuiFileTraversal.
 * @author TBrady
 *
 */
public class FileTraversal {
	protected FTDriver driver;
	protected FilenameFilter fileFilter;
	protected FilenameFilter dirnameFilter;
	
	protected HashSet<String> alreadyVisited = new HashSet<String>();
	
	public FileTest fileTest;
	protected int max;
	protected int numProcessed = 0;
	protected boolean cancelled = false;
	public InitializationStatus iStat;
	
	public int getNumProcessed() {
		return numProcessed;
	}

	public FileTraversal(FTDriver dt) {
		this.driver = dt;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	public void increment() {
	}
	
	public void setTraversal(FileTest fileTest, int max) {
		this.fileTest = fileTest;
		this.max = max;
		alreadyVisited = new HashSet<String>();
	}
	
	public void reportCancel() {
		System.err.println("Stopping: " +max + " items found.");
	}
	public boolean traverse(File f, FileTest fileTest, int max) {
		if (f==null) return false;
		File[] files = f.listFiles(fileFilter);
		if (files == null) return true;
		if (fileTest.processRoot() && fileTest.isTestDirectory(f)) {
			boolean test = true;
			Pattern p = fileTest.getDirectoryPattern();
			if (p != null){
			    test = p.matcher(f.getAbsolutePath()).matches();
			}
			if (test) {
				if (!Files.isSymbolicLink(f.toPath()) || driver.followLinks()) {
					checkDirFile(f, fileTest);
				}
			}
			
		}
		for(int i=0; i<files.length; i++) {
			if (driver.followLinks()) {
				String path;
				try {
					path = files[i].getCanonicalPath().intern();
					if (alreadyVisited.contains(path)) {
						continue;
					}
					alreadyVisited.add(path);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (Files.isSymbolicLink(files[i].toPath())) {
				continue;
			}
			if (files[i].isDirectory()) {
				if (isCancelled()) return false; 
				if (getNumProcessed() >= max) {
					return false; 
				}
				traverse(files[i], fileTest, max);
				if (fileTest.isTestDirectory(files[i])) {
					boolean test = true;
					Pattern p = fileTest.getDirectoryPattern();
					if (p != null){
					    test = p.matcher(files[i].getAbsolutePath()).matches();
					}
					if (test) {
						checkDirFile(files[i], fileTest);
					}
				}
				increment();
			} else {
				if (isCancelled()) return false; 
				File thefile = files[i];
				
				checkFile(thefile, fileTest);

				fileTest.progress(getNumProcessed());
				numProcessed++;
				if (getNumProcessed() >= max) {
					reportCancel();
					return false; 					
				}
			}
		}
		return true;
	}

	public void checkFile(File thefile, FileTest fileTest) {
		if (fileTest.isTestable(thefile)){
			Stats mystats = fileTest.getStats(thefile);
			if (mystats!=null){
				mystats.compute(thefile, fileTest);
			}
		}
	}
	public void checkDirFile(File thefile, FileTest fileTest) {
		if (fileTest.isTestable(thefile)){
			Stats mystats = fileTest.getStats(thefile);
			mystats.compute(thefile, fileTest);
		}
	}
	
	
	public void countDirectories(File f) {
		if (f==null) return;
		File[] files = f.listFiles(dirnameFilter);
		
		increment();
		if (files == null) return;
		for(int i=0; i<files.length && !isCancelled(); i++) {
			countDirectories(files[i]);
		}
	}
	public void clear() {		
		cancelled = false;
		numProcessed = 0;
	}
	public void completeDirectoryScan() {	
		numProcessed = 0;
	}
	public void completeFileScan() {		
	}
	
	public boolean traverseFile(FileTest fileTest, int max) {
		Timer timer = new Timer();
		fileFilter = driver.getFileFilter(fileTest); 
		dirnameFilter = driver.getDirectoryFilter(fileTest);
		traversalStart();
    	iStat = fileTest.init();
    	if (iStat.hasFailTest()){
            double duration = timer.getDuration();
            String name = fileTest.getShortName()+(++driver.summaryCount);
            traversalEnd(name, false, duration); 
            return false;    	    
    	}
		countDirectories(driver.root);
		completeDirectoryScan();
		boolean completed = traverse(driver.root, fileTest, max);
		fileTest.refineResults();
		completeFileScan();
		double duration = timer.getDuration();
		String name = fileTest.getShortName()+(++driver.summaryCount);
		traversalEnd(name,completed, duration); 
		return completed;
	}

    public boolean traverseFile() {
    	return traverseFile(fileTest, max);
    } 

	public boolean cancel(boolean b) {
		cancelled = true;
		driver.batchItems.clear();
		return cancelled;
	}
	public void traversalStart() {
		clear();
		driver.traversalStart();
		driver.types.clear();		
	}
	
	public void reportDuration(double duration) {
		System.out.println(numProcessed+" items.  "+ FTDriver.ndurf.format(duration) + " seconds");
		System.out.flush();
	}
	public void traversalEnd(String name, boolean completed, double duration) {
		driver.traversalEnd(new ActionResult(driver.root, name, fileTest.toString(), fileTest.getStatsDetails(), driver.types, completed, duration)); 
		reportDuration(duration);
	}

}
