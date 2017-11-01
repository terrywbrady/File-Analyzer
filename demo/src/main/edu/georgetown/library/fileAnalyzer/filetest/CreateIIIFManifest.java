package edu.georgetown.library.fileAnalyzer.filetest;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filetest.DefaultFileTest;
import gov.nara.nwts.ftapp.filter.JpegFileTestFilter;
import gov.nara.nwts.ftapp.filter.TiffFileTestFilter;
import gov.nara.nwts.ftapp.filter.TiffJpegFileTestFilter;
import gov.nara.nwts.ftapp.ftprop.FTPropString;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;

/**
 * @author TBrady
 *
 */
class CreateIIIFManifest extends DefaultFileTest {
        private static enum Type {Folder, Image;}
        private static enum IIIFStatsItems implements StatsItemEnum {
                Key(StatsItem.makeStringStatsItem("Path", 400)),
                Type(StatsItem.makeEnumStatsItem(Type.class, "Type")),
                Name(StatsItem.makeStringStatsItem("Name", 400)), 
                InfoPath(StatsItem.makeStringStatsItem("InfoPath", 400));

                StatsItem si;

                IIIFStatsItems(StatsItem si) {
                        this.si = si;
                }

                public StatsItem si() {
                        return si;
                }
        }
        
        public static enum Generator implements StatsGenerator {
                INSTANCE;

                public Stats create(String key) {
                        return new Stats(details, key);
                }
        }

        public static StatsItemConfig details = StatsItemConfig.create(IIIFStatsItems.class);

        long counter = 1000000;
        public static final String IIIFROOT = "iiifroot";

        public CreateIIIFManifest(FTDriver dt) {
                super(dt);
                ftprops.add(
                  new FTPropString(dt, this.getClass().getSimpleName(), IIIFROOT, IIIFROOT, "IIIF Root Path", "")
                );
        }

        private IIIFManifest manifest;

        public InitializationStatus init() {
                manifest = new IIIFManifest(dt.getRoot(), this.getProperty(IIIFROOT).toString());
                return super.init();
        }
        public void refineResults() {
                try {
                        manifest.write();
                } catch (JSONException e) {
                         e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }
        
        public String toString() {
                return "Create IIIF Manifest";
        }

        public String getKey(File f) {
                return getRelPath(f);
        }

        public String getShortName() {
                return "IIIF";
        }

        public Object fileTest(File f) {
                Stats s = getStats(f);
                File parent = f.getParentFile();
                manifest.makeRange(parent, parent.getName(), parent.getName(), false);
                
                s.setVal(IIIFStatsItems.Name, f.getName());
                s.setVal(IIIFStatsItems.Type, Type.Image);
                s.setVal(IIIFStatsItems.InfoPath, manifest.addFile(s.key, f));                        
                return s;
        }

        public Stats createStats(String key) {
                return Generator.INSTANCE.create(key);
        }

        public StatsItemConfig getStatsDetails() {
                return details;
        }

        public void initFilters() {
                filters.add(new TiffJpegFileTestFilter());
                filters.add(new TiffFileTestFilter());
                filters.add(new JpegFileTestFilter());
        }

        public String getDescription() {
                return "Create IIIF Manifest for files";
        }

}
