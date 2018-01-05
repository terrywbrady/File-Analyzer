package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFStandardProp;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFMetadataProp;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFType;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.MetadataInputFileBuilder.InputFileType;
import edu.georgetown.library.fileAnalyzer.util.XMLUtil;
import edu.georgetown.library.fileAnalyzer.util.XMLUtil.SimpleNamespaceContext;

class XMLInputFile extends DefaultInputFile {
        Document d;
        XPath xp = XMLUtil.xf.newXPath();
        XMLInputFile(File file) throws InputFileException {
                super(file);
                try {
                        d = XMLUtil.dbf_ns.newDocumentBuilder().parse(file);
                        if (d == null) {
                                throw new InputFileException(String.format("File [%s] cannot be parsed", file.getName()));
                        }
                        SimpleNamespaceContext nsContext = new XMLUtil().new SimpleNamespaceContext();
                        
                        //For mets.xml
                        nsContext.add("dim", "http://www.dspace.org/xmlns/dspace/dim");
                        nsContext.add("mets", "http://www.loc.gov/METS/");
                        nsContext.add("mods", "http://www.loc.gov/mods/v3");
                        
                        //For EAD files
                        nsContext.add("ead", "urn:isbn:1-931666-22-9");
                        nsContext.add("ns2", "http://www.w3.org/1999/xlink");

                        xp.setNamespaceContext(nsContext);
                        
                        String ns = d.getDocumentElement().getNamespaceURI();
                        String tag = d.getDocumentElement().getTagName();
                        if (ns == null) {
                                ns = "";
                        }
                        if (tag.equals("dublin_core")) {
                                fileType = InputFileType.DC;
                        } else if (ns.equals("urn:isbn:1-931666-22-9")) {
                                fileType = InputFileType.EAD;
                        } else if (ns.equals("http://www.loc.gov/METS/") || tag.equals("mets")) {
                                fileType = InputFileType.METS;
                        } else {                                        
                                throw new InputFileException(String.format("Cannot identify XML file [%s]", file.getName()));
                        }
                } catch (SAXException | IOException | ParserConfigurationException e) {
                        throw new InputFileException(e.getMessage());
                }
        }
        
        @Override
        public String getValue(IIIFLookup key, String def) {
                String xq = key.getFileTypeKey(fileType);
                if (xq != null) {
                        return getXPathValue(d, xq, def);
                }
                return def;
        }
        public String getXPathValue(Node d, String xq, String def) {
                return XMLUtil.getXPathValue(xp, d, xq, def);
        }

        @Override
        public List<RangePath> getInitRanges(IIIFManifest manifest, RangePath parent, ManifestProjectTranslate manifestTranslate) {
                ArrayList<RangePath> rangePaths = new ArrayList<>();
                RangePath rp = new RangePath(manifest, "Subjects", "Subjects");
                rangePaths.add(rp);
                rp.setParent(parent);
                parent.addChildRange(rp);
                if (fileType == InputFileType.EAD) {
                        try {
                                NodeList nl = (NodeList)xp.evaluate("//ead:c01", d, XPathConstants.NODESET);
                                for(int i=0; i<nl.getLength(); i++) {
                                       addRange(manifest, manifestTranslate, rangePaths, nl.item(i), rp);
                                }
                        } catch (XPathExpressionException e) {
                                e.printStackTrace();
                        }
                }
                return rangePaths;
        }
        
        public void addRange(IIIFManifest manifest, ManifestProjectTranslate manifestTranslate, List<RangePath> rangePaths, Node n, RangePath parent) throws XPathExpressionException {
                String rName = manifestTranslate.rangeTranslate(getXPathValue(n, "ead:did/ead:unittitle", "n/a"));
                String rPath = getPath(n);
                RangePath rp = new RangePath(manifest, rPath, rName);
                rp.setParent(parent);
                parent.addChildRange(rp);
                rangePaths.add(rp);
                manifestTranslate.registerEADRange(xp, n, rp);
                rp.setProperty(IIIFType.typeRange, IIIFMetadataProp.dateCreated, getXPathValue(n, "ead:did/ead:unitdate", ""));
                try {
                        NodeList nl = (NodeList)xp.evaluate("ead:did/ead:container", n, XPathConstants.NODESET);
                        for(int i=0; i<nl.getLength(); i++) {
                                Element elem = (Element)nl.item(i);
                                String type = elem.getAttribute("type");
                                String val = elem.getTextContent();
                                String label = elem.getAttribute("label");
                                if (!type.isEmpty() && !val.isEmpty()) {
                                        rp.addMetadata(type, label.isEmpty() ? val : String.format("%s (%s)", val, label));
                                }
                        }
                } catch (XPathExpressionException | DOMException e) {
                        e.printStackTrace();
                }

                NodeList nl = (NodeList)xp.evaluate("ead:c02|ead:c03|ead:c04", n, XPathConstants.NODESET);
                for(int i=0; i<nl.getLength(); i++) {
                        addRange(manifest, manifestTranslate, rangePaths, nl.item(i), rp);
                }
                nl = (NodeList)xp.evaluate("ead:dao", n, XPathConstants.NODESET);
                for(int i=0; i<nl.getLength(); i++) {
                        String url = this.getXPathValue(nl.item(i), "@ns2:href", "");
                        if (url.endsWith(".jpg")) {
                                IIIFCanvasWrapper canvasWrap = manifest.addEadCanvas(url, nl.item(i), this);
                                String canvasid = canvasWrap.getProperty(IIIFStandardProp.id, "");
                                rp.addCanvasId(canvasid);
                        }
                }
        }
        
        public String getPath(Node n) throws XPathExpressionException {
                StringBuilder sb = new StringBuilder();
                NodeList nl = (NodeList)xp.evaluate("ancestor-or-self::ead:c01|ancestor-or-self::ead:c02|ancestor-or-self::ead:c03|ancestor-or-self::ead:c04", n, XPathConstants.NODESET);
                for(int i=0; i<nl.getLength(); i++) {
                        Node cn = nl.item(i);
                        NodeList cnl = (NodeList)xp.evaluate("preceding-sibling::ead:c01|preceding-sibling::ead:c02|preceding-sibling::ead:c03|preceding-sibling::ead:c04", cn, XPathConstants.NODESET);
                        if (i > 0) {
                                sb.append("-");
                        }
                        sb.append(String.format("%03d", cnl.getLength()));
                }
                return sb.toString();
        }
}

