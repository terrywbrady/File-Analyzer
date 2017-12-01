## Input Parameters
- Manifest Property File: manifestGenerate.prop
- ManifestProjectTranslate class - instance of a class that customizes aspects of manifest generation

## manifestGenerate.prop format
    # URL Prefix to prepend to IIIF resource URL's for this proejct
    IIIFRoot: https://your.image.server.edu/project
    
    # Manifest Output Directory
    # If blank, the current dir will be used
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
    # - RESTAPI - extract metadata using the DSpace REST API
    # - None - no metadata file exists
    #GetItemMetata: RESTAPI
    #GetItemMetata: None
    GetItemMetadata: MetadataFile
    
    # Manifest Project
    # Name of the Manifest Project (in code) class that will provide custom translation.
    # If blank, a default value will be assigned.
    # The class should be the name of an Enum that implements ManifestProjectTranslate
    #ManifestProject: 
    

## Implementation of Use Cases

Parameter     | McLaughlin | AIDS EAD   | Photo Arch     | Hoya         | Notes
------------- | ---------- | ---------- | -------------- | ------------ | ------------------- 
Server Root   | dev        | dev        | dev            | dev          |
Manifest      | ead        | ead2       | ua_photos2     | hoya*        |
Create Coll   | N          | N          | N              | Y(1)         | Sequenced in CSV
Indiv Manifest| N          | N          | N(1)           | Folder       | 1. manifests by high level subject?
Init Metadata | ead        | ead        | Prop file      | N/a          |
Ranges        | ead        | ead        | Create Date(1) | N/A          | 1. presumes subj index not generated
Sequence      | ead        | ead+path   | Create Date    | Folder? CSV? | 1. unmapped items in folder order
Item ID       | ead dao    | path       | path           | CSV          | 1. not yet in DG - at what level of granularity is a handle used?
Item Metadata | ead        | ead        | mets or REST   | CSV or REST  |


Sample ID's
- McLaughlin:  10822/NNNNNN/pNNN
- AIDS EAD:    10822/111111/boxNNN/folderNNN/itemNNN/pageNNN - would the whole collection share the same handle?
- Photo UArch: 10822/NNNNN
- Hoya:        10822/NNNNN/pNNN

## Workflow

### Create Collection
- McLaughlin:  N/A
- AIDS EAD:    N/A
- Photo UArch: N/A
- Hoya:        Boilerplate

### Create Manifest
- McLaughlin:  Generate from EAD
- AIDS EAD:    Generate from EAD
- Photo UArch: Generate from Property File
- Hoya:        Generate manifest (1) from item data in CSV (2) REST, using handle file in each dir 

### Create Ranges
- McLaughlin:  Generate from EAD
- AIDS EAD:    Generate from EAD
- Photo UArch: Generate from Item Metadata
- Hoya:        N/A

### Create Canvases
- McLaughlin:  Generate Item Metadata from EAD, get identifier from EAD/DAO and match to folder
- AIDS EAD:    Generate from path info, link to sequence
- Photo UArch: Get item id from folder, get item metadata (via REST)
- Hoya:        Get from CSV or REST

### Create IIIF Path
- McLaughlin:  Get from EAD/DAO
- AIDS EAD:    Generate from path info
- Photo UArch: Get item id from path
- Hoya:        Get item id from path
