package edu.ucop.cdluc3.fileAnalyzer;

import edu.ucop.cdluc3.fileAnalyzer.filetest.UC3ActionRegistry;
import edu.ucop.cdluc3.fileAnalyzer.importer.UC3ImporterRegistry;

import gov.nara.nwts.ftapp.BatchImporter;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filetest.ActionRegistry;
import gov.nara.nwts.ftapp.importer.ImporterRegistry;
/**
 * Driver for the File Analyzer GUI loading image-specific rules but not NARA specific rules.
 * @author TBrady
 *
 */
public class UC3BatchImporter extends BatchImporter {

	public UC3BatchImporter() {
		super();
	}
	
	public ActionRegistry getActionRegistry(FTDriver ft) {
		return new UC3ActionRegistry(ft, true);
	}

	public ImporterRegistry getImporterRegistry(FTDriver ft) {
		return new UC3ImporterRegistry(ft);
	}
	public static void main(String[] args) {
		UC3BatchImporter ba = new UC3BatchImporter();
		ba.run(args);
	}

}
