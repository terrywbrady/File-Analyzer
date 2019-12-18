package edu.georgetown.library.fileAnalyzer.importer;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.stats.Stats;

/**
 * @author TBrady
 *
 */
public class ItemUpdateFolderCreateDeleteBitstream extends ItemUpdateFolderCreate {
        public ItemUpdateFolderCreateDeleteBitstream(FTDriver dt) {
                super(dt);
        }

        @Override
        public String getSubtask() {
                return "Delete Bitstreams";
        }

        @Override
        public String getCmdOpt() {
                return "-D";
        }

        @Override
        public String getColDesc() {
                return "\t2) BitstreamId - required, the integer (or uuid in DSpace6) associated with the bitstream to delete\n";
        }

        @Override
        public int getColCount(){
                return FIXED.values().length;
        };
        public static enum FIXED {
                HANDLE(0), BITID(1);
                int index;
                FIXED(int i) {index = i;}
        }
        
        public void createItem(Stats stats, File selectedFile, Vector<String> cols) {
                String handle = cols.get(FIXED.HANDLE.index);
                String bitid = cols.get(FIXED.BITID.index);
        
                File dir = makeItemDir(stats, handle);
                try {
                        writeDeleteContents(dir, bitid);
                } catch (IOException e1) {
                        stats.setVal(ItemUpdateStatsItems.Status, status.FAIL);
                        stats.setVal(ItemUpdateStatsItems.Message, e1.getMessage());
                        return;
                }
                stats.setVal(ItemUpdateStatsItems.Status, status.PASS);
                
        }
        
}
