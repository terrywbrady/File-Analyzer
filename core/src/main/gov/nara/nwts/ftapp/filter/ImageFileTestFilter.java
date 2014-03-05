package gov.nara.nwts.ftapp.filter;

/**
 * Filter for images files
 * @author TBrady
 *
 */
public class ImageFileTestFilter extends DefaultFileTestFilter {

	public String getSuffix() {
		return ".*\\.(tiff?|gif|jpg|png|bmp)$";
	}
	public boolean isReSuffix() {
		return true;
	}
    public String getName(){return "Images";}

}
