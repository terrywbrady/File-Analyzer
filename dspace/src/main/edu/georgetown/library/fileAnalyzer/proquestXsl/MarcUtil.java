package edu.georgetown.library.fileAnalyzer.proquestXsl;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.ftprop.FTProp;
import gov.nara.nwts.ftapp.ftprop.FTPropString;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MarcUtil {
	public static final String P_UNIV_NAME = "university-name";
	public static final String P_UNIV_LOC = "university-loc";
	public static final String P_EMBARGO_SCHEMA = "embargo-schema";
	public static final String P_EMBARGO_ELEMENT = "embargo-element";
	public static final String P_EMBARGO_TERMS = "embargo-terms";
	public static final String P_EMBARGO_CUSTOM = "embargo-custom-date";
	public static final String P_EMBARGO_LIFT = "embargo-lift-date";
	
	public static SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss.SS");
	
    HashMap<String,Object> pmap;
	public MarcUtil() {
	    pmap = new HashMap<String,Object>();
        pmap.put("recdate", df.format(new Date()));
	}
	
	public void addProps(FTDriver dt, ArrayList<FTProp> ftprops) {
        ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  MarcUtil.P_UNIV_NAME, MarcUtil.P_UNIV_NAME,
                "University Name", "My University"));
        ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  MarcUtil.P_UNIV_LOC, MarcUtil.P_UNIV_LOC,
                "University Location", "My University Location"));
        ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  MarcUtil.P_EMBARGO_SCHEMA, MarcUtil.P_EMBARGO_SCHEMA,
                "Embargo Schema Prefix", "local"));
        ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  MarcUtil.P_EMBARGO_ELEMENT, MarcUtil.P_EMBARGO_ELEMENT,
                "Embargo Element", "embargo"));
        ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  MarcUtil.P_EMBARGO_TERMS, MarcUtil.P_EMBARGO_TERMS,
                "Embargo Policy Qualifier", "terms"));
        ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  MarcUtil.P_EMBARGO_CUSTOM, MarcUtil.P_EMBARGO_CUSTOM,
                "Embargo Custom Date Qualifier", "custom-date"));
        ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  MarcUtil.P_EMBARGO_LIFT, MarcUtil.P_EMBARGO_LIFT,
                "Embargo Lift Date Qualifier", "lift-date"));       	    
	}
	
	public void setProps(ArrayList<FTProp> props) {
        for(FTProp prop: props) {
            String key = prop.getShortName();
            pmap.put(key, prop.getValue());
        }	    
	}
	
	public HashMap<String, Object> getXslParm() {
		return pmap;
	}
	
	public String getLocalSchema() {
	    return pmap.containsKey(P_EMBARGO_SCHEMA) ? pmap.get(P_EMBARGO_SCHEMA).toString() : "local"; 
	}
    public String getEmbargoElement() {
        return pmap.containsKey(P_EMBARGO_ELEMENT) ? pmap.get(P_EMBARGO_ELEMENT).toString() : "embargo"; 
    }
    public String getEmbargoTerms() {
        return pmap.containsKey(P_EMBARGO_TERMS) ? pmap.get(P_EMBARGO_TERMS).toString() : "terms"; 
    }
    public String getEmbargoLift() {
        return pmap.containsKey(P_EMBARGO_LIFT) ? pmap.get(P_EMBARGO_LIFT).toString() : "lift-date"; 
    }
}
