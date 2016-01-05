package gov.nara.nwts.ftapp.filter;

/**
 * Filter for XML files
 * @author TBrady
 *
 */
public class TarFilter extends DefaultFileTestFilter {
	public String getSuffix() { 
		return ".tar";
	}
    public String getName(){return "TAR";}

}
