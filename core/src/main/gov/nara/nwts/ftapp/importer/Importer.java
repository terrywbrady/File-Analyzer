package gov.nara.nwts.ftapp.importer;

import gov.nara.nwts.ftapp.ActionResult;
import gov.nara.nwts.ftapp.ftprop.FTProp;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Contract that Importers will fullfill.
 * @author TBrady
 *
 */
public interface Importer {
    public InitializationStatus initValidate(File refFile);
    
	public ActionResult importFile(File selectedFile) throws IOException;
	public String getDescription();
	public boolean allowForceKey();
    public String getShortName();
    public String getShortNameFormatted();
    public String getShortNameNormalized();

	public List<FTProp> getPropertyList();
	public Object getProperty(String name);
	public void setProperty(String name, String str);
}
