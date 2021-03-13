package edu.ucop.cdluc3.fileAnalyzer.filetest;

import gov.nara.nwts.ftapp.filetest.ActionRegistry;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filetest.CounterValidation;
import edu.georgetown.library.fileAnalyzer.filetest.MarcItemInventory;

/**
 * Initialize the File Analzyer with code relying on external libraries
 *
 */
public class UC3ActionRegistry extends ActionRegistry {

        private static final long serialVersionUID = 1L;

        public UC3ActionRegistry(FTDriver dt, boolean modifyAllowed) {
                super(dt, modifyAllowed);
                removeFT(CounterValidation.class);
                add(new DataoneZip(dt));
                add(new ZipInventory(dt));
                add(new MarcItemInventory(dt));
                add(new MarcAsciiCheck(dt));
        }

}
