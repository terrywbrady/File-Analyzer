package edu.georgetown.library.fileAnalyzer.importer;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.importer.ImporterRegistry;


/**
 * Activates the Importers that will be presented on the Import tab.
 * @author TBrady
 *
 */
public class DSpaceImporterRegistry extends ImporterRegistry {
	
	private static final long serialVersionUID = 1L;

	public DSpaceImporterRegistry(FTDriver dt) {
		super(dt);
		add(new IngestFolderCreate(dt));
		add(new CSVBatcher(dt));
		add(new DSpaceMetadata2Marc(dt));
        add(new ReadMetadataRegistry(dt));
	}
	

}
