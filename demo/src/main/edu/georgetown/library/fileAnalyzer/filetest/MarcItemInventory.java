package edu.georgetown.library.fileAnalyzer.filetest;

import gov.nara.nwts.ftapp.ActionResult;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.Timer;
import gov.nara.nwts.ftapp.filetest.DefaultFileTest;
import gov.nara.nwts.ftapp.filter.MrcFilter;
import gov.nara.nwts.ftapp.ftprop.FTPropString;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;
import gov.nara.nwts.ftapp.importer.Importer;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

public class MarcItemInventory extends DefaultFileTest implements Importer
{
    // name and description of the Marc Inventory Importer
    public String toString()
    {
        return "MARC Item Inventory";
    }
    
    public String getDescription()
    {
        return "The MARC Item Inventory Importer will report Item IDs and Bib ID's of marc records in a MARC file (.mrc or .dat)";
        
    }
    
    public String getShortName()
    {
        return "Item Inventory";
    }

    
    // resulting information to display
    private static enum InventoryStatsItem implements StatsItemEnum
    {
        Item_ID(StatsItem.makeStringStatsItem("Item IDs", 100)),
        File(StatsItem.makeStringStatsItem("File", 100)),
        RecNum(StatsItem.makeIntStatsItem("RecNum")),
        ShortItem_ID(StatsItem.makeStringStatsItem("Short Item ID", 100)),
        Bib_ID(StatsItem.makeStringStatsItem("Short Bib ID", 100)),
        ;
        
        StatsItem si;
        
        InventoryStatsItem (StatsItem si)
        {
            this.si = si;
        }
        
        public StatsItem si()
        {
            return si;
        }
    }
        
    public static enum Generator implements StatsGenerator
    {
        INSTANCE;
        public Stats create(String key)
        {
            return new Stats(details, key);
        }
    }

    
    public static StatsItemConfig details = StatsItemConfig.create(InventoryStatsItem.class);
    
    public static final String P_VTAGS = "subfields";
        
    public MarcItemInventory(FTDriver dt)
    {
        super(dt);
        this.ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  P_VTAGS, P_VTAGS,
                "Comma Separated list of subfields to extract (example: 907a)", "907a"));            
    }

    int ftni = 0;
    Vector<String> auxTags;
    static Pattern pTag = Pattern.compile("^(\\d+)([a-z])$");
    
    @Override public InitializationStatus init() {
        auxTags = new Vector<String>();
        ftni = 0;
        details = StatsItemConfig.create(InventoryStatsItem.class);
        String subfields = (String)this.getProperty(P_VTAGS);
        for(String subfield: subfields.split(",")) {
            if (!pTag.matcher(subfield).matches()) continue;
            auxTags.add(subfield);
            details.addStatsItem(subfield, StatsItem.makeStringStatsItem(subfield));
        }
        return super.init();
    }
    
    // file import rules
    public ActionResult importFile(File selectedFile) throws IOException
    {
        Timer timer = new Timer();
        TreeMap<String, Stats> types = new TreeMap<String, Stats>();
        ftni = 0;
        processFile(selectedFile, types);
        
        return new ActionResult(selectedFile, selectedFile.getName(), this.toString(), details, types, true, timer.getDuration());
        
    }

    public void processFile(File selectedFile, TreeMap<String, Stats> types) throws IOException
    {
        InputStream in = new FileInputStream(selectedFile);
        MarcReader reader = new MarcPermissiveStreamReader(in, true, true);
        
        int frec = 0;
        
        while( reader.hasNext() )
        {
            Record record = reader.next();
            
            String bib_id = "";
            String item_id = "ni"+(ftni++);
            String short_item_id = "";
            DataField df945 = (DataField) record.getVariableField("945");
            
            if (df945 != null) {
                Subfield df945y = df945.getSubfield('y');
                if (df945y != null) {
                    item_id = df945y.getData();
                }
                if (item_id.startsWith(".i")) {short_item_id=item_id.substring(2, 9);}
            }
            
            DataField df907 = (DataField) record.getVariableField("907");
            
            if (df907 != null) {
                Subfield df907a = df907.getSubfield('a');
                if (df907a != null) {
                    bib_id = df907a.getData();
                }
            }
            if (bib_id.startsWith(".b")) {bib_id=bib_id.substring(2, 9);}
            
                
            String key = item_id;
            Stats stat = Generator.INSTANCE.create(key);
            types.put(stat.key, stat);
            
            stat.setVal(InventoryStatsItem.ShortItem_ID, short_item_id);
            stat.setVal(InventoryStatsItem.File, selectedFile.getName());
            stat.setVal(InventoryStatsItem.RecNum, ++frec);
            stat.setVal(InventoryStatsItem.Bib_ID, bib_id);
            
            for(String subfield: auxTags) {
                Matcher m = pTag.matcher(subfield);
                StatsItem si = details.getByKey(subfield);
                if (si == null) continue;
                stat.setKeyVal(si, "");
                if (!m.matches()) continue;
                StringBuilder sb = new StringBuilder();
                for(VariableField vf: record.getVariableFields(m.group(1))) {
                    DataField df = (DataField)vf;
                    for(Subfield sf: df.getSubfields(m.group(2).charAt(0))){
                        String dd = sf.getData();
                        if (dd == null) continue;
                        if (sb.length() > 0 && !dd.isEmpty()) {
                            sb.append(";");
                        }
                        sb.append(dd);                        
                    }
                }
                stat.setKeyVal(si, sb.toString());
            }
        } // end while loop.
    }

    @Override
    public Object fileTest(File f) {
        try {
            processFile(f, this.dt.types);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void refineResults() {
        this.dt.types.remove(DEFKEY);
    }
    @Override
    public InitializationStatus initValidate(File refFile) {
        return init();
    }

    @Override
    public boolean allowForceKey() {
        return false;
    }
    
    public void initFilters() {
        filters.add(new MrcFilter());
    }
    public StatsItemConfig getStatsDetails() {
        return MarcItemInventory.details;
    }
    
    public static final String DEFKEY = "";
    public String getKey(File f) {
        return DEFKEY;
    }

}
