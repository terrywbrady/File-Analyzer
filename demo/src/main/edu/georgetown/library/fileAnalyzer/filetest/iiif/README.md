## Input Parameters
- IIIF Server root path
- Manifest filename
- Initial Metadata File (optional)
  - Property File
  - Collection mets.xml file (from DSpace)
  - EAD file (from ArchivesSpace)
- Create Collection Manifest
  - Assume top level directories would become separate manifests (is this sufficient?)
- Get Item Identifier
  - From directory name
  - From CSV
  - From metadata - mets.xml
  - From metadata - handle
  - From metadata - dublin_core.xml
- Get Metadata
  - None (or use EAD)
  - Rest API
  - From CSV
  - From metadata - mets.xml
  - From metadata - handle
  - From metadata - dublin_core.xml
  
## Stats Results Fields

- Key (iiif path and/or file path)
- Iiif path
- Status
  - Matched_FileAndMetadata)
  - File_Default
  - File_NoMetadata
  - Metadata_NoFileFound
- Width
- Height
- Identifier
- Sequence
- ParentRange
- Terms
- HasFullText

## Questions
- Subject terms behavior (multi link)

## Implementation

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
