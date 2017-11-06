package edu.georgetown.library.fileAnalyzer.filetest;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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

                this.addMetadata(jsonObject, METADATA, "Collection", 
                        "<a href='https://repository.library.georgetown.edu/handle/10822/552780'>DigitalGeorgetown - Hoya Collection</a>");
                this.addMetadata(jsonObject, METADATA, "Creator", 
                        getXPathValue(d, "/dublin_core/dcvalue[@element='creator']",""));
                this.addMetadata(jsonObject, METADATA, "Publisher", 
                        getXPathValue(d, "/dublin_core/dcvalue[@element='publisher']",""));
                this.addMetadata(jsonObject, METADATA, "Date Created", 
                        getXPathValue(d, "/dublin_core/dcvalue[@element='date'][@qualifier='created']",""));
                try {
                        NodeList nl = (NodeList)xp.evaluate("/dublin_core/dcvalue[@element='subject']", d, XPathConstants.NODESET);
                        for(int i=0; i<nl.getLength(); i++) {
                                Element s = (Element)nl.item(i);
                                this.addMetadata(jsonObject, METADATA, "subject", s.getTextContent());
                        }
                } catch (XPathExpressionException e) {
                }

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
