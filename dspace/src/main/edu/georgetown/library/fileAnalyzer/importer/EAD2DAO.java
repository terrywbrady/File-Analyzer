package edu.georgetown.library.fileAnalyzer.importer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;


import gov.nara.nwts.ftapp.ActionResult;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.Timer;
import gov.nara.nwts.ftapp.ftprop.FTPropEnum;
import gov.nara.nwts.ftapp.ftprop.FTPropFile;
import gov.nara.nwts.ftapp.ftprop.FTPropString;
import gov.nara.nwts.ftapp.importer.DefaultImporter;
import gov.nara.nwts.ftapp.importer.DelimitedFileReader;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;
import edu.georgetown.library.fileAnalyzer.util.XMLUtil;

/**
 * Importer for tab delimited files
 * 
 * @author TBrady
 * 
 */
public class EAD2DAO extends DefaultImporter {

    public static final String P_MATCHTYPE = "match-type";
    public static final String P_DCCSV = "csv-file";
    public static final String P_MATCH = "match-col";
    public static final String P_NAME = "name-col";
    public static final String P_DAOID = "daoid-col";
    public static final String P_LINK = "link-col";
    public static final String P_THUMB = "thumb-col";
    private FTPropFile dcFile; 
    public static enum EAD2DAOStatsItems implements StatsItemEnum {
        Record(StatsItem.makeStringStatsItem("Record", 100).setExport(false)),
        Field_Name(StatsItem.makeStringStatsItem("Field Name").setInitVal("TBD")),
        EAD_ID(StatsItem.makeStringStatsItem("EAD ID", 100)),
        REF_ID(StatsItem.makeStringStatsItem("REF ID", 150)),
        DigitalObjectId(StatsItem.makeStringStatsItem("Digital Object ID", 150)),
        DigitalObjectTitle(StatsItem.makeStringStatsItem("Digital Object Title", 150)),
        PublishDAO(StatsItem.makeEnumStatsItem(TF.class, "Publish Digital Object Record")),
        DAOLink(StatsItem.makeStringStatsItem("File URL of Linked-to digital object", 150)),
        DAOThumbnail(StatsItem.makeStringStatsItem("File URL of Thumbnail", 150));
;
        StatsItem si;

        EAD2DAOStatsItems(StatsItem si) {
            this.si = si;
        }

        public StatsItem si() {
            return si;
        }
    }

    public static enum Generator implements StatsGenerator {
        INSTANCE;
        public Stats create(String key) {
            return new Stats(details, key);
        }
    }

    public static StatsItemConfig details = StatsItemConfig
            .create(EAD2DAOStatsItems.class);
    
    public static enum TF {TRUE,FALSE}
    public static enum EAD_MATCHER {
        TITLE(4),
        AS_REFID(2);
        int index;
        EAD_MATCHER(int index) {
            this.index = index;
        }
    }
    

    public EAD2DAO(FTDriver dt) {
        super(dt);
        dcFile = new FTPropFile(this.dt, this.getClass().getSimpleName(), P_DCCSV, P_DCCSV, "CSV file containing columns to match, Optional", "");
        this.ftprops.add(new FTPropEnum(dt, this.getClass().getSimpleName(),
                P_MATCHTYPE, P_MATCHTYPE,
                "Name of EAD field to match in CSV file", EAD_MATCHER.values(), EAD_MATCHER.TITLE));
        ftprops.add(dcFile);
        this.ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),
                P_MATCH, P_MATCH,
                "Name of column to match","dc.title[en]"));
        this.ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),
                P_DAOID, P_DAOID,
                "Name of column to assign as a DAO identifier","dc.identifier.uri[en]"));
        this.ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),
                P_NAME, P_NAME,
                "Name of column to use as a DAO name","dc.title[en]"));
        this.ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),
                P_LINK, P_LINK,
                "Name of column to assign as a DAO link","dc.identifier.uri[en]"));
        this.ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),
                P_THUMB, P_THUMB,
                "Name of column to assign as a thumbnail url","thumbnail-link"));
    }

    public String toString() {
        return "EAD to DAO Template";
    }
    public String getDescription() {
        return "This rule will create a template file for the ArchivesSpace DAO Import Plugin from Harvard.";
    }

    public String getShortName() {
        return "EAD2DAO";
    }

    private HashMap<String,Vector<String>> mapVals = new HashMap<>();
    private int i_match = -1;
    private int i_dao = -1;
    private int i_name = -1;
    private int i_link = -1;
    private int i_thumb = -1;
    
    public void initMapFile() throws IOException {
        i_match = -1;
        i_dao = -1;
        i_name = -1;
        i_link = -1;
        i_thumb = -1;
        mapVals.clear();
        File f = dcFile.getFile();
        if (f == null) {
            return;
        }
        if (!f.exists()) {
            return;
        }
        DelimitedFileReader dfr = new DelimitedFileReader(f, ",");
        Vector<String> header = dfr.getRow();
        for(int i=0; i<header.size(); i++) {
            String s = header.get(i);
            if (this.getProperty(P_MATCH).equals(s)) {
                i_match = i;
            }
            if (this.getProperty(P_DAOID).equals(s)) {
                i_dao = i;
            }
            if (this.getProperty(P_NAME).equals(s)) {
                i_name = i;
            }
            if (this.getProperty(P_LINK).equals(s)) {
                i_link = i;
            }
            if (this.getProperty(P_THUMB).equals(s)) {
                i_thumb = i;
            }
        }
        if (i_match == -1) {
            return;
        }
        for(Vector<String>row=dfr.getRow(); row!=null; row=dfr.getRow()) {
            String key = normalizeKey(row.get(i_match));
            mapVals.put(key, row);
        }
    }
    
    public String normalizeKey(String s) {
        return s.toLowerCase()
            .replaceAll("[^a-z0-9]", " ")
            .replaceAll(" +", " ");
    }
    
    public String getMapValue(String key, int col, String def) {
        if (col < 0) {
            return def;
        }
        key = normalizeKey(key);
        if (!mapVals.containsKey(key)) {
            return def;
        }
        return mapVals.get(key).get(col);
    }
    
    public ActionResult importFile(File selectedFile) throws IOException {
        details = StatsItemConfig.create(EAD2DAOStatsItems.class);
        EAD_MATCHER matcher = (EAD_MATCHER)getProperty(P_MATCHTYPE);
        initMapFile();
        HashMap<String, Object> params = new HashMap<>();
        Timer timer = new Timer();
        TreeMap<String, Stats> types = new TreeMap<String, Stats>();
        
        try {
            Document d = XMLUtil.db_ns.parse(selectedFile);
            File csv = new File(selectedFile.getParent(), selectedFile.getName()+".csv");
            XMLUtil.doTransform(d, csv, "edu/georgetown/library/fileAnalyzer/ead-dao.xsl", params);
            DelimitedFileReader dfr = new DelimitedFileReader(csv, ",");
            Vector<String> header = dfr.getRow();
            int rownum = 1_000_000;
            for(Vector<String>row=dfr.getRow(); row!=null; row=dfr.getRow()) {
                String key = ""+rownum++;
                Stats stats = Generator.INSTANCE.create(key);
                types.put(key, stats);
                if (row.size() >= 8) {
                    String matchkey = row.get(matcher.index);
                    stats.setVal(EAD2DAOStatsItems.Field_Name, row.get(0));
                    stats.setVal(EAD2DAOStatsItems.EAD_ID, row.get(1));
                    stats.setVal(EAD2DAOStatsItems.REF_ID, row.get(2));
                    stats.setVal(EAD2DAOStatsItems.DigitalObjectId, getMapValue(matchkey, i_dao, row.get(3)));
                    stats.setVal(EAD2DAOStatsItems.DigitalObjectTitle, getMapValue(matchkey, i_name, row.get(4)));
                    stats.setVal(EAD2DAOStatsItems.PublishDAO, row.get(5));
                    stats.setVal(EAD2DAOStatsItems.DAOLink, getMapValue(matchkey, i_link, row.get(6)));
                    stats.setVal(EAD2DAOStatsItems.DAOThumbnail, getMapValue(matchkey, i_thumb, row.get(7)));
                }
                for(int i=0; i<header.size(); i++) {
                    String s = row.size() > i ? row.get(i) : "";
                    String col = header.get(i);
                    stats.appendKeyVal(details.getByKey(col), s);
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return new ActionResult(selectedFile, "EAD2DAO",
                this.toString(), details, types, true, timer.getDuration());
    }

}
