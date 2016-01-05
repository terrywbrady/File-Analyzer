package gov.nara.nwts.ftapp.importer;

import gov.nara.nwts.ftapp.ActionResult;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.ftprop.FTProp;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for importer behaviors.
 * @author TBrady
 *
 */
public abstract class DefaultImporter implements Importer {
	protected FTDriver dt;
	protected ArrayList<FTProp>ftprops;
	public DefaultImporter(FTDriver dt) {
		this.dt = dt;
		ftprops = new ArrayList<FTProp>();
	}
	
	public abstract String toString();
	public abstract ActionResult importFile(File selectedFile) throws IOException;
	public boolean allowForceKey() {
		return false;
	}
	public String getShortNameNormalized() {
		return getShortName().replaceAll("[\\s&]","");
	}
	public String getShortNameFormatted() {
		StringBuffer buf = new StringBuffer();
		buf.append(getShortNameNormalized());
		buf.append("                     ");
		return buf.substring(0,20);
	}
	public List<FTProp> getPropertyList() {
		return ftprops;
	}
	
	public Object getProperty(String name) {
		return getProperty(name, null);
	}
	public Object getProperty(String name, Object def) {
		for(FTProp ftprop: ftprops) {
			if (ftprop.getName().equals(name)) {
				return ftprop.getValue();
			}
		}
		return def;
	}
	public void setProperty(String name, String s) {
		for(FTProp ftprop: ftprops) {
			if (ftprop.getName().equals(name)) {
				ftprop.setValue(ftprop.validate(s));
				return;
			}
		}
	}
    public InitializationStatus initValidate(File refFile) { 
        InitializationStatus iStat = new InitializationStatus();
        for(FTProp prop: ftprops) {
            iStat.addMessage(prop.initValidation(refFile));
        }
        return iStat;
    }
}
