package edu.georgetown.library.fileAnalyzer.importer;

import edu.georgetown.library.fileAnalyzer.filetest.CounterValidationXls;
import edu.georgetown.library.fileAnalyzer.filetest.MarcItemInventory;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filetest.CounterValidation;


/**
 * Activates the Importers that will be presented on the Import tab.
 * @author TBrady
 *
 */
public class DemoImporterRegistry extends DSpaceImporterRegistry {
	
	private static final long serialVersionUID = 1L;

	public DemoImporterRegistry(FTDriver dt) {
		super(dt);
		// add(new MarcValidator(dt));
		
		removeImporter(CounterValidation.class);
		add(new CounterValidationXls(dt));
        add(new DemoImporter(dt));
		add(new MarcRecValidator(dt));
		add(new OutsourcedMarcRecValidator(dt));
		add(new EncodingCheck(dt));
		add(new MarcInventory(dt));
        add(new MarcItemInventory(dt));
		add(new MarcSerializer(dt));
	}
	

}
