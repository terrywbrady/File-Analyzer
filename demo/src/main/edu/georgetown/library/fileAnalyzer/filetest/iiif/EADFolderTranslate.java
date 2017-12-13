package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;

import org.w3c.dom.Node;

import edu.georgetown.library.fileAnalyzer.util.XMLUtil;

public class EADFolderTranslate extends FileSystemProjectTranslate {
        private List<FolderIndex> folders = new ArrayList<>();
        class FolderIndex {
                String box = "";
                String folderStart = "";
                String folderEnd = "";
                RangePath rangePath;
                Pattern p = Pattern.compile("^(.*)-(.*)$");
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
                public boolean inRange(Matcher m) {
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
                
                public void setRangePath(RangePath rp) {
                        this.rangePath = rp;
                }
                public RangePath getRangePath() {
                        return rangePath;
                }
                public boolean hasRangePath() {
                        return rangePath != null;
                }
        }

        protected Pattern getBoxFolderPattern() {
                return Pattern.compile("\\/[Bb]ox\\s*(\\d+)\\/(\\d)$");
        }
        
        protected Matcher getBoxFolderMatcher(String key, File f) {
                return getBoxFolderPattern().matcher(f.getAbsolutePath());
        }
        
        @Override
        public RangePath getPrimaryRangePath(String key, File f, MetadataInputFile itemMeta) {
                RangePath rp = super.getPrimaryRangePath(key, f, itemMeta);
                Matcher m = getBoxFolderMatcher(key, f);
                
                if (m.matches()) {
                        for(FolderIndex fi: folders) {
                                if (fi.inRange(m)) {
                                        if (fi.hasRangePath()) {
                                                fi.getRangePath().addChildRange(rp);
                                                break;
                                        }
                                }
                        }
                }
                
                return rp;
        }
        @Override
        public void registerEADRange(XPath xp, Node n, RangePath rangePath) {
                String box = XMLUtil.getXPathValue(xp, n, "ead:did/ead:container[@type='Box']","");
                String folder = XMLUtil.getXPathValue(xp, n, "ead:did/ead:container[@type='Folder']","");
                if (!box.isEmpty() && !folder.isEmpty()) {
                        FolderIndex folderIndex = new FolderIndex(box, folder);
                        folderIndex.setRangePath(rangePath);
                        folders.add(folderIndex);
                }
        }
        
        

}
