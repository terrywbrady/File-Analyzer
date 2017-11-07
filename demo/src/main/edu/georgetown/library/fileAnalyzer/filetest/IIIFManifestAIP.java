package edu.georgetown.library.fileAnalyzer.filetest;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.georgetown.library.fileAnalyzer.util.XMLUtil;
import edu.georgetown.library.fileAnalyzer.util.XMLUtil.SimpleNamespaceContext;

public class IIIFManifestAIP extends IIIFManifest {

        protected HashMap<String,JSONObject> subjranges = new HashMap<>();
        JSONObject allsubjects;
        JSONObject allphotos;
        public IIIFManifestAIP(File root, String iiifRootPath, File manifestFile) {
                super(root, iiifRootPath, manifestFile);
                jsonObject.put("label", "Photograph Selections from University Archives");
                jsonObject.put("attribution", "Permission to copy or publish photographs from this collection must be obtained from the Georgetown University Archives.");
                this.addMetadata(jsonObject, METADATA, "Collection", 
                        "<a href='https://repository.library.georgetown.edu/handle/10822/549423'>DigitalGeorgetown Collection</a>");
                SimpleNamespaceContext nsContext = new XMLUtil().new SimpleNamespaceContext();
                nsContext.add("dim", "http://www.dspace.org/xmlns/dspace/dim");
                xp.setNamespaceContext(nsContext);
                allsubjects = makeRange(root, "All Subjects","Photo Listing", true);
                addArray(top, RANGES).put(allsubjects.getString("@id"));
                allphotos = this.makeRangeObject("All Photos", "all-photos", "all label");
        }       
        
        @Override public void addCanvasMetadata(JSONObject canvas, File f) {
                File mets = new File(f.getParentFile(), "mets.xml");
                try {
                        Document d = XMLUtil.db_ns.parse(mets);
                        setXPathValue(canvas, "label", d, "//dim:field[@element='title']");
                        addMetadata(canvas, METADATA, "name", f.getName());
                        addMetadata(canvas, METADATA, "Digital Georgetown",
                                        "<a href='" +
                                        getXPathValue(d, "//dim:field[@element='identifier'][@qualifier='uri']","") +
                                        "'>Item Page</a>");
                        addMetadata(canvas, METADATA, "Description", 
                                getXPathValue(d, "//dim:field[@element='description'][not(@qualifier)]",""));
                        addMetadata(canvas, METADATA, "Date Created", 
                                        getXPathValue(d, "//dim:field[@element='date'][@qualifier='created']",""));
                        try {
                                NodeList nl = (NodeList)xp.evaluate("//dim:field[@element='subject'][@qualifier='other']", d, XPathConstants.NODESET);
                                for(int i=0; i<nl.getLength(); i++) {
                                        Element selem = (Element)nl.item(i);
                                        String subj = selem.getTextContent();
                                        this.addMetadata(canvas, METADATA, "subject", subj);
                                        JSONObject subrange = subjranges.get(subj);
                                        if (subrange == null) {
                                                String subjid = subj.replaceAll(" ", "");
                                                subrange = makeRangeObject(subj, subjid, "Subject");
                                                subjranges.put(subj, subrange);
                                                addArray(allsubjects, RANGES).put(subjid);
                                        }
                                        JSONObject ir = ranges.get(f.getParentFile());
                                        if (ir != null) {
                                                addArray(subrange, RANGES).put(ir.get("@id"));
                                        }
                                }
                        } catch (XPathExpressionException e) {
                        }
                } catch (JSONException e) {
                       e.printStackTrace();
                } catch (SAXException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        public JSONObject makeRange(File dir, String label, String id, boolean isTop) {
                if (ranges.containsKey(dir)) {
                        return ranges.get(dir);
                }
                File mets = new File(dir, "mets.xml");
                if (!mets.exists()) {
                        return makeRangeObject(label, id, "Title");
                }
                try {
                        Document d = XMLUtil.db_ns.parse(mets);
                        String title = getXPathValue(d, "//dim:field[@element='title']","");
                        JSONObject obj = makeRangeObject(title, id, "Title");
                        addDirLink(dir, RANGES, id);
                        ranges.put(dir, obj);
                        //addArray(allphoto, RANGES).put(id);
                        return obj;
                } catch (JSONException e) {
                       e.printStackTrace();
                } catch (SAXException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                }
                
                return makeRangeObject(label, id, "Title");
        }       

        @Override public void linkCanvas(File f, String canvasid) {
                addDirLink(f, CANVASES, canvasid);
        }

}
