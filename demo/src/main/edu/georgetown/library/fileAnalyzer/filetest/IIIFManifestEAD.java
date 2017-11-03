package edu.georgetown.library.fileAnalyzer.filetest;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.georgetown.library.fileAnalyzer.util.XMLUtil;
import edu.georgetown.library.fileAnalyzer.util.XMLUtil.SimpleNamespaceContext;

public class IIIFManifestEAD extends IIIFManifest {
        HashMap<FolderIndex,JSONObject> folderRanges = new HashMap<>();
        class FolderIndex {
                String box = "";
                String folderStart = "";
                String folderEnd = "";
                Pattern p = Pattern.compile("^(.*)-(.*)$");
                Pattern pdir = Pattern.compile("^b(.*)_f(.*)$");
                FolderIndex(String box, String folder) {
                        this.box = normalize(box);
                        Matcher m = p.matcher(folder);
                        if (m.matches()) {
                                folderStart = normalize(m.group(1));
                                folderEnd = normalize(m.group(2));
                        } else {
                                folderStart = normalize(folder);
                                folderEnd = normalize(folder);                                
                        }
                }
                
                public String normalize(String s) {
                        try {
                                int i = Integer.parseInt(s);
                                return String.format("%06d", i);
                        } catch(NumberFormatException e) {
                                return s;
                        }
                }
                public boolean inRange(String dirName) {
                        Matcher m = pdir.matcher(dirName);
                        if (m.matches()) {
                                if (normalize(m.group(1)).equals(box)) {
                                        String f = normalize(m.group(2));
                                        if (f.compareTo(folderStart) >= 0 && f.compareTo(folderEnd) <= 0) {
                                                return true;
                                        }
                                }
                        }
                        return false;
                }
        }
        
        public IIIFManifestEAD(File root, String iiifRootPath, File manifestFile) {
                super(root, iiifRootPath, manifestFile);
                SimpleNamespaceContext nsContext = new XMLUtil().new SimpleNamespaceContext();
                nsContext.add("ead", "urn:isbn:1-931666-22-9");
                nsContext.add("ns2", "http://www.w3.org/1999/xlink");
                xp.setNamespaceContext(nsContext);
                jsonObject.put("attribution", "Georgetown Law Library");
        }       
        
        public void setEAD(Document d) {
                if (d == null) {
                        return;
                }
                setXPathValue(jsonObject, "label", d, "concat(/ead:ead/ead:archdesc/ead:did/ead:unitid,': ',/ead:ead/ead:archdesc/ead:did/ead:unittitle)");
                makeEADRanges(top, d, "//ead:c01");
                JSONObject fs = makeRange(root, "All Boxes and Folders","file-system", true);
                addArray(top, RANGES).put(fs.getString("@id"));
        }
        
        public void makeEADRanges(JSONObject parent, Node n, String xq) {
                try {
                        NodeList nl = (NodeList)xp.evaluate(xq, n, XPathConstants.NODESET);
                        for(int i=0; i<nl.getLength(); i++) {
                                Element c0 = (Element)nl.item(i);
                                JSONObject range = makeRangeObject(getXPathValue(c0, "ead:did/ead:unittitle","label"), c0.getAttribute("id"), "Container Title");
                                addArray(parent, RANGES).put(range.getString("@id"));
                                addMetadata(range, METADATA, "level", c0.getAttribute("level"));
                                makeEADRanges(range, c0, "ead:c02");
                                String box = getXPathValue(c0, "ead:did/ead:container[@type='Box']","");
                                String folder = getXPathValue(c0, "ead:did/ead:container[@type='Folder']","");
                                String boxlab = "";
                                if (!box.isEmpty()) {
                                        boxlab = String.format("Box %s; ", box);
                                        if (!folder.isEmpty()) {
                                                boxlab += String.format("Folder %s", folder);
                                        }
                                }
                                if (!boxlab.isEmpty()) {
                                        addMetadata(range, METADATA, "Container", String.format("Box %s; Folder %s", box, folder));
                                        FolderIndex folderIndex = new FolderIndex(box, folder);
                                        folderRanges.put(folderIndex, range);
                                }
                        }
                } catch (XPathExpressionException e) {
                        e.printStackTrace();
                }
                
        }
        

        public String translateLabel(String label) {
                return label
                        .replaceAll("box_","Box ")
                        .replaceAll("^b", "Box ")
                        .replaceAll("_f", " Folder ")
                        ;
        }
        
        public JSONObject makeRange(File dir, String label, String id, boolean isTop) {
                if (ranges.containsKey(dir)) {
                        return ranges.get(dir);
                }
                if (!isTop) {
                        File pfile = dir.getParentFile();
                        if (!ranges.containsKey(pfile)) {
                                //root path will always be found
                                makeRange(pfile, pfile.getName(), pfile.getName(), false);
                        }                        
                }
                
                JSONObject obj = makeRangeObject(label, id, "Directory");
                addDirLink(dir, RANGES, id);
                for(FolderIndex folderIndex: folderRanges.keySet()) {
                        if (folderIndex.inRange(label)) {
                                JSONObject range = folderRanges.get(folderIndex);
                                addArray(range, RANGES).put(id);
                        }
                }
                ranges.put(dir, obj);
                return obj;
        }       

        public String translateItemLabel(String label) {
                return label
                        .replaceAll(".*_item_", "");
        }

        @Override public void linkCanvas(File f, String canvasid) {
                addDirLink(f, CANVASES, canvasid);
        }

 }
