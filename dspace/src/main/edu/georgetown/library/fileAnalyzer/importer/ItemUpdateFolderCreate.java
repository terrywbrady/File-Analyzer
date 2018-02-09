package edu.georgetown.library.fileAnalyzer.importer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


import gov.nara.nwts.ftapp.ActionResult;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.Timer;
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
 * @author TBrady
 *
 */
public abstract class ItemUpdateFolderCreate extends DefaultImporter {
        static enum ItemUpdateStatsItems implements StatsItemEnum {
                LineNo(StatsItem.makeStringStatsItem("Line No").setExport(false).setWidth(60)),
                Handle(StatsItem.makeStringStatsItem("Handle")),
                Folder(StatsItem.makeStringStatsItem("Folder")),
                Status(StatsItem.makeEnumStatsItem(status.class, "Status").setWidth(60)),
                Message(StatsItem.makeStringStatsItem("Message", 300).setExport(false))
                ;
                
                StatsItem si;
                ItemUpdateStatsItems(StatsItem si) {this.si=si;}
                public StatsItem si() {return si;}
        }

        public static enum Generator implements StatsGenerator {
                INSTANCE;
                public Stats create(String key) {return new Stats(details, key);}
        }
        static StatsItemConfig details = StatsItemConfig.create(ItemUpdateStatsItems.class);
        class CreateException extends Exception {
                private static final long serialVersionUID = 5042987495219000857L;

                CreateException(String s) {
                        super(s);
                }
        }
        
        public StatsItemConfig getDetails() {
                return details;
        }
        
        protected enum status {INIT,PASS,WARN,FAIL}
        
        NumberFormat nf;
        MetadataRegPropFile metadataPropFile;
        
        public static final String CONTENTS = "contents";
        public static final String DELCONTENTS = "delete_contents";
        public static final String DUBLINCORE = "dublin_core.xml";

        public static final String HANDLE_PREFIX = "Handle Prefix";

        public ItemUpdateFolderCreate(FTDriver dt) {
                super(dt);
                nf = NumberFormat.getNumberInstance();
                nf.setMinimumIntegerDigits(8);
                nf.setGroupingUsed(false);

                this.ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(), HANDLE_PREFIX, "handle",
                "Handle prefix to create match existing item", "http://hdl.handle.net/"));
        }

        @Override 
        public InitializationStatus initValidate(File selectedFile) {
                InitializationStatus is = super.initValidate(selectedFile);
                File parent = selectedFile.getParentFile();
                currentIngestDir = new File(parent, "itemupdate");
                if (currentIngestDir.exists()) {
                       is.addFailMessage(String.format("Directory [%s] already exists - rename or delete this dir", currentIngestDir.getAbsolutePath())); 
                } else {
                        currentIngestDir.mkdirs();
                }
                return is;
        }
        public abstract String getSubtask();
        public abstract String getCmdOpt();
        public abstract String getColDesc();
        public abstract int getColCount();
        public String toString() {
                return "Ingest: Create Item Update Folders - " + getSubtask();
        }
        public String getDescription() {
                return "This will create folders for a the dspace itemupdate "+ getCmdOpt() +" task.  \n"+
                        "(This is an experimental feature.  Test carefully before using this.)\n\n"+
                        "Expected input format\n"+
                                "\t1) Item Handle - A unique folder will be created for each item to be ingested.  Names must be unique\n"+
                                getColDesc() + "\n" +
                        "See https://wiki.duraspace.org/display/DSDOC3x/Updating+Items+via+Simple+Archive+Format#UpdatingItemsviaSimpleArchiveFormat-ItemUpdateCommands"
                                ;
        }
        public String getShortName() {
                return "ItemUpdate " + getSubtask();
        }
        
        public void createMetadataFile(Stats stats, File dir, String handle) {
                String filename = DUBLINCORE;
                String schema = "dc";
                
                Document d = XMLUtil.db.newDocument();
                Element e = d.createElement("dublin_core");
                e.setAttribute("schema", schema);
                d.appendChild(e);
                addElement(e, "identifier", "uri", this.getProperty(HANDLE_PREFIX) + handle);

                File f = new File(dir, filename);

                try {
                        XMLUtil.doSerialize(d, f);
                } catch (TransformerException|IOException e2) {
                        stats.setVal(ItemUpdateStatsItems.Status, status.FAIL);
                        stats.setVal(ItemUpdateStatsItems.Message, e2.getMessage());
                }
                
        }

        public abstract void createItem(Stats stats, File selectedFile, Vector<String> cols);
        
        public void addElement(Element e, String name, String qual, String val) {
                Element el = e.getOwnerDocument().createElement("dcvalue");
                e.appendChild(el);
                el.setAttribute("element",name);
                el.setAttribute("qualifier", qual);
                el.appendChild(e.getOwnerDocument().createTextNode(val));
        }
        

        protected File currentIngestDir;
        public ActionResult importFile(File selectedFile) throws IOException {
                Timer timer = new Timer();
                Vector<Vector<String>> data = DelimitedFileReader.parseFile(selectedFile, ",");

                TreeMap<String,Stats> types = new TreeMap<String,Stats>();
                int rowKey = 0;
                for(int r=1; r<data.size(); r++) {
                        Vector<String> cols = data.get(r);
                        String key = nf.format(rowKey++);
                        Stats stats = Generator.INSTANCE.create(key);
                        if (cols.size() == this.getColCount()) {
                                this.createItem(stats, selectedFile, cols);
                        } else {
                                stats.setVal(ItemUpdateStatsItems.Status, status.PASS);
                                stats.setVal(ItemUpdateStatsItems.Message, "Bad Input column count");
                        }
                        
                        
                        types.put(key, stats);
                }
                
                
                return new ActionResult(selectedFile, selectedFile.getName(), this.toString(), getDetails(), types, true, timer.getDuration());
        }

        public void writeContents(File dir, String bitfile, String bundle, String desc) throws IOException {
                File f = new File(dir, CONTENTS);
                try(BufferedWriter bw = new BufferedWriter(new FileWriter(f, true))) {
                        bw.write(String.format("%s\tbundle:%s\tdescription:%s\n", bitfile, bundle, desc));
                }
        }

        public void writeDeleteContents(File dir, String bitid) throws IOException {
                File f = new File(dir, DELCONTENTS);
                try(BufferedWriter bw = new BufferedWriter(new FileWriter(f, true))) {
                        bw.write(String.format("%s\n", bitid));
                }
        }

        public File makeItemDir(Stats stats, String handle) {
                String itemdir = handle.replace("/", "_");
                
                stats.setVal(ItemUpdateStatsItems.Handle, handle);
                stats.setVal(ItemUpdateStatsItems.Folder, itemdir);
        
                File dir = new File(currentIngestDir, itemdir);
                if (!dir.exists()) {
                        dir.mkdirs();
                        createMetadataFile(stats, dir, handle);
                }
                return dir;
        }
        

}
