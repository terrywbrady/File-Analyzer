package edu.georgetown.library.fileAnalyzer.filetest;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.georgetown.library.fileAnalyzer.util.XMLUtil;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;

/**
 * @author TBrady
 *
 */
class CreateIIIFManifestDC extends CreateIIIFManifest {
        public static StatsItemConfig details = StatsItemConfig.create(IIIFStatsItems.class);

        public CreateIIIFManifestDC(FTDriver dt) {
                super(dt);
        }

        @Override protected IIIFManifest createIIIFManifest(File manFile) {
                return new IIIFManifestDC(dt.getRoot(), this.getProperty(IIIFROOT).toString(), manFile); 
        }
        @Override public String toString() {
                return "Create IIIF Manifest - DC Folders";
        }

        @Override public String getShortName() {
                return "IIIF DC";
        }

        @Override public StatsItemConfig getStatsDetails() {
                return details;
        }

        @Override public String getDescription() {
                return "Create IIIF Manifest for folders containing DC xml files";
        }

        @Override public InitializationStatus init() {
                InitializationStatus is = super.init();
                try {
                        File dc = new File(dt.root, "dublin_core.xml");
                        Document d = XMLUtil.db_ns.parse(dc);
                        ((IIIFManifestDC)manifest).setDC(d);
                } catch (SAXException e) {
                        is.addMessage(e);
                } catch (IOException e) {
                        is.addMessage(e);
                }
                return is;
        }
        
        @Override public String getKey(File f) {
                return dt.getRoot().getName() + "/" + getRelPath(f);
        }

}
