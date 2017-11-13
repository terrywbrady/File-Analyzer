package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;

/**
 * @author TBrady
 *
 */
public class CreateIIIFManifestAIP extends CreateIIIFManifest {
        public static StatsItemConfig details = StatsItemConfig.create(IIIFStatsItems.class);

        public CreateIIIFManifestAIP(FTDriver dt) {
                super(dt);
        }

        @Override protected IIIFManifest createIIIFManifest(File manFile) {
                return new IIIFManifestAIP(dt.getRoot(), this.getProperty(IIIFROOT).toString(), manFile); 
        }
        @Override public String toString() {
                return "Create IIIF Manifest AIP";
        }

        @Override public String getShortName() {
                return "IIIF AIP";
        }

        @Override public StatsItemConfig getStatsDetails() {
                return details;
        }

        @Override public String getDescription() {
                return "Create IIIF Manifest for AIP folders";
        }

        @Override public String getKey(File f) {
                return getRelPath(f);
        }

}
