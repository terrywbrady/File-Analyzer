package gov.nara.nwts.ftapp.filter;

/**
 * Filter for text files
 * @author TBrady
 *
 */
public class MrcFilter extends DefaultFileTestFilter {
	public String getSuffix() {
		return ".*\\.(mrc|unx)$";
	}
	public boolean isReSuffix() {
		return true;
	}
    public String getName(){return "MRC";}

}
