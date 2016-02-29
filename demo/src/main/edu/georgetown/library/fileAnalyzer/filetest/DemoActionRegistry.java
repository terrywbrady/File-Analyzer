package edu.georgetown.library.fileAnalyzer.filetest;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filetest.CounterValidation;
/** 
 * Initialize the File Analzyer with generic image processing rules (but not NARA specific business rules)
 * @author TBrady
 *
 */
public class DemoActionRegistry extends DSpaceActionRegistry {
	
	private static final long serialVersionUID = 1L;

	public DemoActionRegistry(FTDriver dt, boolean modifyAllowed) {
		super(dt, modifyAllowed);
		add(new PageCount(dt));
		add(new ImageProperties(dt));
		add(new YearbookNameValidationTest(dt)); 
		add(new CreateBag(dt)); 
        add(new CreateAPTrustBag(dt)); 
        add(new AIPZipToAPT(dt)); 
        add(new AIPDirToAPT(dt)); 
		add(new VerifyBag(dt)); 
		add(new VerifyBagZip(dt)); 
		add(new VerifyBagTar(dt)); 
        add(new VerifyAPTrustBagTar(dt)); 
		removeFT(CounterValidation.class);
		add(new CounterValidationXls(dt)); 
        add(new DemoFileTest(dt)); 
	    add(new MarcItemInventory(dt));
	}
	
}
