package edu.ucop.cdluc3.fileAnalyzer.importer;

import gov.nara.nwts.ftapp.importer.ImporterRegistry;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filetest.CounterValidation;


/**
 * Activates the Importers that will be presented on the Import tab.
 * @author TBrady
 *
 */
public class UC3ImporterRegistry extends ImporterRegistry {
	
	private static final long serialVersionUID = 1L;

	public UC3ImporterRegistry(FTDriver dt) {
		super(dt);
	}
	

}
