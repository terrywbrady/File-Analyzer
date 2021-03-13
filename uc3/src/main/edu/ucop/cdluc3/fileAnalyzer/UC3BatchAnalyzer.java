package edu.ucop.cdluc3.fileAnalyzer;

import edu.ucop.cdluc3.fileAnalyzer.filetest.UC3ActionRegistry;
import edu.ucop.cdluc3.fileAnalyzer.importer.UC3ImporterRegistry;

import gov.nara.nwts.ftapp.BatchAnalyzer;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filetest.ActionRegistry;
import gov.nara.nwts.ftapp.importer.ImporterRegistry;
/**
 * Driver for the File Analyzer GUI loading image-specific rules but not NARA specific rules.
 * @author TBrady
 *
 */
public class UC3BatchAnalyzer extends BatchAnalyzer {

	public UC3BatchAnalyzer() {
		super();
	}
	
	public ActionRegistry getActionRegistry(FTDriver ft) {
		return new UC3ActionRegistry(ft, true);
	}

	protected ImporterRegistry getImporterRegistry(FTDriver ft) {
		return new UC3ImporterRegistry(ft);
	}
	public static void main(String[] args) {
		UC3BatchAnalyzer ba = new UC3BatchAnalyzer();
		ba.run(args);
	}

}
