package edu.georgetown.library.fileAnalyzer.importer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Vector;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.stats.Stats;

/**
 * @author TBrady
 *
 */
public class ItemUpdateFolderCreateAddBitstream extends ItemUpdateFolderCreate {
        public ItemUpdateFolderCreateAddBitstream(FTDriver dt) {
                super(dt);
        }

        @Override
        public String getSubtask() {
                return "Add Bitstreams";
        }

        @Override
        public String getCmdOpt() {
                return "-A";
        }

        @Override
        public String getColDesc() {
                return "\t2) Item file name - required, a file with that name must exist relative to the imported spreadsheet\n"+
                        "\t3) Bundle Name\n" +
                        "\t4) Bitstream description - Description that will be assigned to a bitstream on ingest\n";
        }

        @Override
        public int getColCount(){
                return FIXED.values().length;
        };

        public static enum FIXED {
                HANDLE(0), FILE(1), BUNDLE(2), BITDESC(3);
                int index;
                FIXED(int i) {index = i;}
        }
        
        public void createItem(Stats stats, File selectedFile, Vector<String> cols) {
                String handle = cols.get(FIXED.HANDLE.index);
                String bitfile = cols.get(FIXED.FILE.index);
                String bundle = cols.get(FIXED.BUNDLE.index);
                String bitdesc = cols.get(FIXED.BITDESC.index);
        
                File dir = makeItemDir(stats, handle);
                File sourceFile = new File(selectedFile.getParentFile(), bitfile);
                if (!sourceFile.exists()) {
                        stats.setVal(ItemUpdateStatsItems.Status, status.FAIL);
                        stats.setVal(ItemUpdateStatsItems.Message, String.format("File %s not found", bitfile));
                        return;                    
                }
                File copyFile = new File(dir, bitfile);
                try {
                        Files.copy(sourceFile.toPath(), copyFile.toPath());
                } catch (IOException e) {
                        stats.setVal(ItemUpdateStatsItems.Status, status.FAIL);
                        stats.setVal(ItemUpdateStatsItems.Message, e.getMessage());
                        return;
                }
                createMetadataFile(stats, dir, handle);

                try {
                        writeContents(dir, bitfile, bundle, bitdesc);
                } catch (IOException e1) {
                        stats.setVal(ItemUpdateStatsItems.Status, status.FAIL);
                        stats.setVal(ItemUpdateStatsItems.Message, e1.getMessage());
                        return;
                }
                stats.setVal(ItemUpdateStatsItems.Status, status.PASS);
                
        }
}
