package edu.ucop.cdluc3.fileAnalyzer.importer;

import gov.nara.nwts.ftapp.importer.ImporterRegistry;
import edu.georgetown.library.fileAnalyzer.filetest.MarcItemInventory;
import edu.georgetown.library.fileAnalyzer.importer.EncodingCheck;
import edu.georgetown.library.fileAnalyzer.importer.MarcInventory;
import edu.georgetown.library.fileAnalyzer.importer.MarcRecValidator;
import edu.georgetown.library.fileAnalyzer.importer.MarcSerializer;
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
		removeImporter(CounterValidation.class);
		add(new MarcRecValidator(dt));
		add(new EncodingCheck(dt));
		add(new MarcInventory(dt));
        add(new MarcItemInventory(dt));
		add(new MarcSerializer(dt));
		add(new ZipAnalyzer(dt));
	}	

}
