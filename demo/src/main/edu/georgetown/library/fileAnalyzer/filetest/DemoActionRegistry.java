package edu.georgetown.library.fileAnalyzer.filetest;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftappImg.filetest.ImageActionRegistry;

/** 
 * Initialize the File Analzyer with generic image processing rules (but not NARA specific business rules)
 * @author TBrady
 *
 */
public class DemoActionRegistry extends ImageActionRegistry {
	
	private static final long serialVersionUID = 1L;

	public DemoActionRegistry(FTDriver dt, boolean modifyAllowed) {
		super(dt, modifyAllowed);
		add(new IngestInventory(dt));
		add(new IngestValidate(dt));
	}
	
}
