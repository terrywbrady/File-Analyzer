package edu.georgetown.library.fileAnalyzer.filetest;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        protected TreeMap<String,JSONObject> subjranges = new TreeMap<>();
        protected TreeMap<String,JSONObject> datecanvases = new TreeMap<>();
        protected TreeMap<String,JSONObject> dateranges = new TreeMap<>();
        JSONObject allsubjects;
        JSONObject alldates;
        public IIIFManifestAIP(File root, String iiifRootPath, File manifestFile) {
                super(root, iiifRootPath, manifestFile);
                jsonObject.put("label", "Photograph Selections from University Archives");
                jsonObject.put("attribution", "Permission to copy or publish photographs from this collection must be obtained from the Georgetown University Archives.");
                this.addMetadata(jsonObject, METADATA, "Collection", 
                        "Photograph Selections from University Archives");
                SimpleNamespaceContext nsContext = new XMLUtil().new SimpleNamespaceContext();
                nsContext.add("dim", "http://www.dspace.org/xmlns/dspace/dim");
                xp.setNamespaceContext(nsContext);
                alldates = makeRange(root, "Date Ranges","Date Listing", true);
                addArray(top, RANGES).put(alldates.getString("@id"));
                allsubjects = makeRange(root, "All Subjects","Photo Listing", true);
                addArray(top, RANGES).put(allsubjects.getString("@id"));
        }       
        
        public JSONObject getDateRange(String dateCreated) {
                Pattern p = Pattern.compile("^(\\d\\d\\d)\\d.*");
                Matcher m = p.matcher(dateCreated);
                String name = null;
                if (m.matches()) {
                        int year = Integer.parseInt(m.group(1));
                        name = String.format("%d0 - %d0", year, year+1);
                } else {
                        name = "Unspecified";
                }
                JSONObject range = dateranges.get(name);
                if (range == null) {
                        String rangeid = "date-" + name.replaceAll(" ", "");
                        range = makeRangeObject(name, rangeid, "Date Range");
                        dateranges.put(name, range);
                }
                return range;
        }
        
        @Override public void addCanvasMetadata(JSONObject canvas, File f) {
                File mets = new File(f.getParentFile(), "mets.xml");
                try {
                        Document d = XMLUtil.db_ns.parse(mets);
                        setXPathValue(canvas, "label", d, "//dim:field[@element='title']");
                        addMetadata(canvas, METADATA, "Title", getXPathValue(d, "//dim:field[@element='title']", ""));
                        String dateCreated = getXPathValue(d, "//dim:field[@element='date'][@qualifier='created']","");
                        addMetadata(canvas, METADATA, "Date Created", dateCreated);
                        String dateKey = dateCreated + " " + canvas.getString("@id");
                        datecanvases.put(dateKey, canvas);
                        JSONObject dateRange = getDateRange(dateCreated);
                        if (dateRange != null) {
                                addArray(dateRange, CANVASES).put(canvas.get("@id"));
                        }

                        addMetadata(canvas, METADATA, "Creator", 
                                        getXPathValue(d, "//dim:field[@element='creator']",""));
                        addMetadata(canvas, METADATA, "Description", 
                                        getXPathValue(d, "//dim:field[@element='description'][not(@qualifier)]",""));
                        
                        StringBuilder sbSubjects = new StringBuilder();
                        try {
                                NodeList nl = (NodeList)xp.evaluate("//dim:field[@element='subject'][@qualifier='other']", d, XPathConstants.NODESET);
                                for(int i=0; i<nl.getLength(); i++) {
                                        Element selem = (Element)nl.item(i);
                                        String subj = selem.getTextContent();
                                        if (sbSubjects.length() > 0) {
                                                sbSubjects.append("; ");
                                        }
                                        //String ref = "https://repository-dev.library.georgetown.edu/handle/10822/549423#?cv=12";
                                        //sbSubjects.append("<a href='"+ref+"'>"+subj+"</a>");
                                        sbSubjects.append(subj);
                                        JSONObject subrange = subjranges.get(subj);
                                        if (subrange == null) {
                                                String subjid = subj.replaceAll(" ", "");
                                                subrange = makeRangeObject(subj, subjid, "Subject");
                                                subjranges.put(subj, subrange);
                                        }
                                        JSONObject ir = ranges.get(f.getParentFile());
                                        if (ir != null) {
                                                addArray(subrange, RANGES).put(ir.get("@id"));
                                        }
                                }
                        } catch (XPathExpressionException e) {
                        }
                        addMetadata(canvas, METADATA, "Subject(s)", sbSubjects.toString());
                        addMetadata(canvas, METADATA, "Rights", 
                                        getXPathValue(d, "//dim:field[@element='rights']",""));
                        String permalink = getXPathValue(d, "//dim:field[@element='identifier'][@qualifier='uri']",""); 
                        addMetadata(canvas, METADATA, "Permanent URL",
                                        "<a href='" + permalink + "'>" + permalink + "</a>");
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

        @Override public void refine() {
                for(JSONObject canvas: datecanvases.values()) {
                        addArray(seq, CANVASES).put(canvas);
                }
                for(JSONObject range: dateranges.values()) {
                        String name = String.format("%s (%d)", range.getString("label"), range.getJSONArray("canvases").length());
                        range.put("label", name);
                        addArray(alldates, RANGES).put(range);
                }
                for(JSONObject subrange: subjranges.values()) {
                        addArray(allsubjects, RANGES).put(subrange.get("@id"));
                }
        }

        @Override public void addCanvasToManifest(JSONObject canvas) {
                //no op - will add based on dates
        }

}
