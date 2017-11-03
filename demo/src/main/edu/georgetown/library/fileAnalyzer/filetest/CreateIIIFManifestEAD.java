package edu.georgetown.library.fileAnalyzer.filetest;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.ftprop.FTPropString;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;
import gov.nara.nwts.ftapp.ftprop.InvalidInputException;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.georgetown.library.fileAnalyzer.util.XMLUtil;

/**
 * @author TBrady
 *
 */
class CreateIIIFManifestEAD extends CreateIIIFManifest {
        public static StatsItemConfig details = StatsItemConfig.create(IIIFStatsItems.class);
        public static final String EAD = "ead";

        class EADFile extends FTPropString {
                Document d;
                EADFile(FTDriver dt) {
                    super(dt,CreateIIIFManifestEAD.this.getClass().getName(), EAD, EAD,
                            "EAD File containing key informaiton", "AIDS_papers_ead_updated.xml");
                }
                @Override public InitializationStatus initValidation(File refFile) {
                    InitializationStatus iStat = new InitializationStatus();
                    try {
                        readEADFile(new File(dt.root, this.getValue().toString()));
                    } catch (IOException e) {
                        iStat.addFailMessage(e.getMessage());
                    } catch (InvalidInputException e) {
                        iStat.addFailMessage(e.getMessage());
                    } catch (SAXException e) {
                        iStat.addFailMessage(e.getMessage());
                    } catch (ParserConfigurationException e) {
                        iStat.addFailMessage(e.getMessage());
                    }
                    return iStat;
                }
                public void readEADFile(File selectedFile) throws IOException, InvalidInputException, SAXException, ParserConfigurationException {
                        d = XMLUtil.db_ns.parse(selectedFile);
                        ((IIIFManifestEAD)manifest).setEAD(d);
                }
                Document getDocument() {
                        return d;
                }
        }
        private EADFile eadFile;
        
        public CreateIIIFManifestEAD(FTDriver dt) {
                super(dt);
                eadFile = new EADFile(dt);
                ftprops.add(eadFile);
        }

        @Override protected IIIFManifest createIIIFManifest(File manFile) {
                return new IIIFManifestEAD(dt.getRoot(), this.getProperty(IIIFROOT).toString(), manFile); 
        }
        
        @Override public String toString() {
                return "Create IIIF Manifest - AIDS Papers Collection";
        }

        @Override public String getShortName() {
                return "IIIF EAD";
        }


        @Override public StatsItemConfig getStatsDetails() {
                return details;
        }

        @Override public String getDescription() {
                return "Create IIIF Manifest for files using the AIDS Papers EAD as supplemental input";
        }

}
