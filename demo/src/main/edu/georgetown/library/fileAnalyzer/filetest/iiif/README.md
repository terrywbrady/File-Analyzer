## Input Parameters
- Manifest Property File: manifestGenerate.prop
- ManifestProjectTranslate class - instance of a class that customizes aspects of manifest generation

## manifestGenerate.prop format
    # URL Prefix to prepend to IIIF resource URL's for this proejct
    IIIFRoot: https://your.image.server.edu/project
    
    # Manifest Output Directory
    # If blank, the current dir will be used
    # Enter the path as a linux style path even for windows
    #   \\server\share\path --> //server/share/path
    ManifestOuputDir: 

    # Manifest Output File
    # Name of the top level manifest file that will be generated
    ManifestOuputFile: manifest.json 
    
    # Create Collection Manifest - An individual manifest will be generated for each subfolder 
    # and registered in a collection level manifest
    # CreateCollectionManifest: true
    CreateCollectionManifest: false
    
    # Manifest Logo URL
    # URL to a logo image to embed within the manifest
    ManifestLogoURL: https://your.image.server.edu/logo.jpg
    
    # Manifest Metadata Input File
    # - EAD File containing metadata
    # - CSV File for each input directory of resources
    # If blank, this property file will be utilized
    ManifestMetadataInputFile: 
    
    # Manifest Metadata 
    # - If not defined in another external metadata file
    #Title: 
    #Attribution:
    #DateCreated:  
    #Creator: 
    #Description:
    #Rights: 
    
    # Get Item Identifier
    # - FolderName - determine the item identifier from the folder name
    # - MetataFile - extract the item identifer from metadata 
    #   - mets.xml from DSpace AIP export
    #   - handle from DSpace Simple Archive Format format
    #   - dublin_core.xml from DSpace Simple Archive Format metadata file
    #GetItemIdentifer: MetadataFile
    GetItemIdentifier: FolderName
    
    # Get Item Metadata
    # - MetadataFile - extract metadata from a file
    #   - mets.xml from DSpace AIP export
    #   - dublin_core.xml from DSpace Simple Archive Format metadata file
    # - RESTAPI - extract metadata using the DSpace REST API (Future)
    # - None - no metadata file exists
    #
    #In the future, we may add an option to extract metadata from the image file itself
    #GetItemMetadata: RESTAPI
    #GetItemMetadata: None
    GetItemMetadata: MetadataFile
    
    # Manifest Project
    # Name of the Manifest Project (in code) class that will provide custom translation.
    # If blank, a default value will be assigned.
    # The class should be the name of an Enum that implements ManifestProjectTranslateEnum
    #ManifestProject: 
    
