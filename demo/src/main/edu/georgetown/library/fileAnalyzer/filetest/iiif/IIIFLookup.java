package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.MetadataInputFileBuilder.InputFileType;

public class IIIFLookup {
        private String property = null;
        private String dc = null;
        private String metsXpath = null; 
        private String eadXPath = null;
        private String dcXPath = null;
        public IIIFLookup(String property) {
                this(property, null, null);
        }
        public IIIFLookup(String property, String dcelem) {
                this(property, dcelem, null, null);
        }
        public IIIFLookup(String property, String dcelem, String dcqual) {
                this(property, dcelem, dcqual, null);
        }
        public IIIFLookup(String property, String dcelem, String dcqual, String eadXPath) {
                this.property  = property;
                dcelem = (dcelem == null) ? "" : dcelem;
                dcqual = (dcqual == null) ? "" : dcqual;
                if (!dcelem.isEmpty()) {
                        if (dcqual.isEmpty()) {
                                this.dc = String.format("dc.%s", dcelem);
                                this.dcXPath = String.format("/dublin_core/dcvalue[@element='%s'][@qualifier='none']", dcelem);
                                this.metsXpath = String.format("//mets:mdWrap[@OTHERMDTYPE='DIM']//dim:field[@element='%s'][not(@qualifier)]", dcelem);
                        } else if (dcqual.equals("*")) {
                                this.dc = String.format("dc.%s", dcelem);
                                this.dcXPath = String.format("/dublin_core/dcvalue[@element='%s']", dcelem);
                                this.metsXpath = String.format("//mets:mdWrap[@OTHERMDTYPE='DIM']//dim:field[@element='%s']", dcelem);
                        } else {
                                this.dc = String.format("dc.%s.%s", dcelem, dcqual);
                                this.dcXPath = String.format("/dublin_core/dcvalue[@element='%s'][@qualifier='%s']", dcelem, dcqual);
                                this.metsXpath = String.format("//mets:mdWrap[@OTHERMDTYPE='DIM']//dim:field[@element='%s'][@qualifier='%s']", dcelem, dcqual);
                        }
                }
                this.eadXPath  = eadXPath;
        }
        public String getFileTypeKey(InputFileType fileType) {
                if (fileType == InputFileType.METS) {
                        return this.metsXpath;
                } else if (fileType == InputFileType.EAD) {
                        return this.eadXPath;
                } else if (fileType == InputFileType.DC) {
                        return this.dcXPath;
                } else if (fileType == InputFileType.CSV) {
                        return this.dc;
                } else if (fileType == InputFileType.REST) {
                        return this.dc;
                }
                return property;
        }

        public String getProperty() {
                return property;
        }
}
