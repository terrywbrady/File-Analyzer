package edu.georgetown.library.fileAnalyzer.importer;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import edu.georgetown.library.fileAnalyzer.proquestXsl.GUProquestURIResolver;
import edu.georgetown.library.fileAnalyzer.proquestXsl.MarcUtil;

import gov.nara.nwts.ftapp.ActionResult;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.Timer;
import gov.nara.nwts.ftapp.YN;
import gov.nara.nwts.ftapp.ftprop.FTPropEnum;
import gov.nara.nwts.ftapp.ftprop.FTPropString;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;
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
public class EAD2DC extends DefaultImporter {

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
    public static String P_COLL = "Collection";
    public static String P_RIGHTS = "RIGHTS";
    

    public EAD2DC(FTDriver dt) {
        super(dt);
        this.ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),
                P_COLL, P_COLL,
                "DSpace Collection Handle",""));
        this.ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),
                P_RIGHTS, P_RIGHTS,
                "dc.rights statement",""));
    }

    public String toString() {
        return "EAD to DSpace Dublin Core";
    }

    public String getDescription() {
        return "This rule will take an exported EAD file and convert archival objects to dublin core metadata.";
    }

    public String getShortName() {
        return "Dspace2Marc";
    }

    public ActionResult importFile(File selectedFile) throws IOException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("collection", this.getProperty(P_COLL));
        params.put("rights", this.getProperty(P_RIGHTS));
        Timer timer = new Timer();
        TreeMap<String, Stats> types = new TreeMap<String, Stats>();
        
        try {
            Document d = XMLUtil.db_ns.parse(selectedFile);
            File csv = new File(selectedFile.getParent(), selectedFile.getName()+".csv");
            XMLUtil.doTransform(d, csv, "edu/georgetown/library/fileAnalyzer/ead.xsl", params);
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
                    String s = row.get(i);
                    String col = header.get(i);
                    if (col.equals("dc.date.created[en]")) {
                        s = normalizeDate(s);
                    }
                    stats.appendKeyVal(details.getByKey(col), s);
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return new ActionResult(selectedFile, selectedFile.getName(),
                this.toString(), details, types, true, timer.getDuration());
    }

    public String normalizeDate(String s) {
        if (Pattern.matches("^\\d\\d\\d\\d(-\\d\\d(-\\d\\d)?)?", s)) {
            return s;
        }
        try {
            Date d = new SimpleDateFormat("DD MMM yyyy").parse(s);
            return new SimpleDateFormat("yyyy-MM-DD").format(d);
        } catch (ParseException e1) {
            // no action
        }
        try {
            Date d = new SimpleDateFormat("MMM yyyy").parse(s);
            return new SimpleDateFormat("yyyy-MM").format(d);
        } catch (ParseException e1) {
            // no action
        }
        return s;
    }
}
