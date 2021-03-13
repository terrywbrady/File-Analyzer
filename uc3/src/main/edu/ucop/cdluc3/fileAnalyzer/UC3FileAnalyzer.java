package edu.ucop.cdluc3.fileAnalyzer;

import java.io.File;

import edu.ucop.cdluc3.fileAnalyzer.filetest.UC3ActionRegistry;
import edu.ucop.cdluc3.fileAnalyzer.importer.UC3ImporterRegistry;

import gov.nara.nwts.ftapp.filetest.ActionRegistry;
import gov.nara.nwts.ftapp.gui.DirectoryTable;
import gov.nara.nwts.ftapp.importer.ImporterRegistry;
/**
 * Driver for the File Analyzer GUI loading image-specific rules but not NARA specific rules.
 * @author TBrady
 *
 */
public class UC3FileAnalyzer extends DirectoryTable {

	public UC3FileAnalyzer(File f, boolean modifyAllowed) {
		super(f, modifyAllowed);
		this.title = "CDL UC3 File Analyzer";
		this.message = "Illustrates extensions to the file analzyer.";
		this.refreshTitle();
		
	}
	
	protected ActionRegistry getActionRegistry() {
		return new UC3ActionRegistry(this, modifyAllowed);
	}

	protected ImporterRegistry getImporterRegistry() {
		return new UC3ImporterRegistry(this);
	}
	public static void main(String[] args) {
		if (args.length > 0)
			new UC3FileAnalyzer(new File(args[0]), false);		
		else
			new UC3FileAnalyzer(null, false);		
	}

}
