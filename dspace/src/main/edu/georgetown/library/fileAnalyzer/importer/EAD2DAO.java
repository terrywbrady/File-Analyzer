package edu.georgetown.library.fileAnalyzer.importer;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;


import gov.nara.nwts.ftapp.ActionResult;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.Timer;
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

    public static enum EAD2DCStatsItems implements StatsItemEnum {
        Record(StatsItem.makeStringStatsItem("Record", 100).setExport(false));

        StatsItem si;

        EAD2DCStatsItems(StatsItem si) {
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
            .create(EAD2DCStatsItems.class);
    

    public EAD2DAO(FTDriver dt) {
        super(dt);
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

    public ActionResult importFile(File selectedFile) throws IOException {
        details = StatsItemConfig.create(EAD2DCStatsItems.class);
        HashMap<String, Object> params = new HashMap<>();
        Timer timer = new Timer();
        TreeMap<String, Stats> types = new TreeMap<String, Stats>();
        
        try {
            Document d = XMLUtil.db_ns.parse(selectedFile);
            File csv = new File(selectedFile.getParent(), selectedFile.getName()+".csv");
            XMLUtil.doTransform(d, csv, "edu/georgetown/library/fileAnalyzer/ead-dao.xsl", params);
            DelimitedFileReader dfr = new DelimitedFileReader(csv, ",");
            Vector<String> header = dfr.getRow();
            for(String col: header) {
                details.addStatsItem(col, StatsItem.makeStringStatsItem(col));
            }
            int rownum = 1_000_000;
            for(Vector<String>row=dfr.getRow(); row!=null; row=dfr.getRow()) {
                String key = ""+rownum++;
                Stats stats = Generator.INSTANCE.create(key);
                types.put(key, stats);
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
