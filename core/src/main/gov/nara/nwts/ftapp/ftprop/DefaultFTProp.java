package gov.nara.nwts.ftapp.ftprop;

import java.io.File;

import gov.nara.nwts.ftapp.FTDriver;

/**
 * Abstract base class for File Test Properties
 * @author TBrady
 *
 */

public abstract class DefaultFTProp implements FTProp {
	String name;
	String shortname;
	String description;
	Object def;
	FTDriver ft;
	String prefix = ""; 
	
	public enum RUNMODE {
		TEST,
		PROD;
	}

	public DefaultFTProp(FTDriver ft, String prefix, String name, String shortname, String description, Object def) {
		this.name = name;
		this.shortname = shortname;
		this.description = description;
		this.ft = ft;
		this.def =  def;
		this.prefix = prefix;
	}
	
	public void init() {
		if (ft.hasPreferences()) {
			def = ft.getPreferences().get(getPrefString(), def.toString());
		}
	}
	public void init(Object[] vals) {
		if (ft.hasPreferences()) {
			String s = ft.getPreferences().get(getPrefString(), def.toString());
			if (s == null) return;
			for(Object obj: vals) {
				if (s.equals(obj.toString())) {
					def = obj;
					return;
				}
			}
		}
	}
	
	public String getPrefString() {
		return "ftprop--"+prefix+"--"+shortname;
	}

	public String describe() {
		return description;
	}
	public String describeFormatted() {
		return "\t\t\t"+description;
	}

	public Object getDefault() {
		return def;
	}

	public String getName() {
		return name;
	}
	public String getShortName() {
		return shortname;
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

    public InitializationStatus initValidation(File refFile){
        InitializationStatus iStat = new InitializationStatus();
        return iStat;
    }

}
