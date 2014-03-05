package gov.nara.nwts.ftapp.filter;

/**
 * Filter for TIF files
 * @author TBrady
 *
 */
public class TiffFileTestFilter extends DefaultFileTestFilter {

	public String getSuffix() {
		return ".*\\.(tiff?)$";
	}
	public boolean isReSuffix() {
		return true;
	}
	public String getPrefix() {
		return "^[^\\.].*";
	}
	public boolean isRePrefix() {
		return true;
	}
    public String getName(){return "Tiffs";}

}
