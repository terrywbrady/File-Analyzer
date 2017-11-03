package edu.georgetown.library.fileAnalyzer.filetest;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;

public class IIIFManifestDC extends IIIFManifest {
        
        public IIIFManifestDC(File root, String iiifRootPath, File manifestFile) {
                super(root, iiifRootPath, manifestFile);
                set2Page();
       }       
        
        public void setDC(Document d) {
                if (d == null) {
                        return;
                }
                setXPathValue(jsonObject, "description", d, "/dublin_core/dcvalue[@element='title']");
                setXPathValue(jsonObject, "label", d, "/dublin_core/dcvalue[@element='description'][@qualifier='none']");
                setXPathValue(jsonObject, "attribution", d, "/dublin_core/dcvalue[@element='rights']");
                this.addMetadata(jsonObject, METADATA, "Creator", 
                        getXPathValue(d, "/dublin_core/dcvalue[@element='creator']",""));
                this.addMetadata(jsonObject, METADATA, "Publisher", 
                        getXPathValue(d, "/dublin_core/dcvalue[@element='publisher']",""));
                this.addMetadata(jsonObject, METADATA, "Date Created", 
                        getXPathValue(d, "/dublin_core/dcvalue[@element='date'][@qualifier='created']",""));
        }
        
        private static Pattern pItem = Pattern.compile("^.*_(\\d+)\\.tif$");
        public String translateItemLabel(String label) {
                Matcher m = pItem.matcher(label);
                if (!m.matches()) {
                        return label;
                }
                try {
                        return String.format("p. %s", Integer.parseInt(m.group(1)));
                } catch (NumberFormatException e) {
                        return label;
                }
        }
}
