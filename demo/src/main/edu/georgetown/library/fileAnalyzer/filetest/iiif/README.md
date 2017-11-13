## Input Parameters
- IIIF Server root path
- Manifest filename
- Initial Metadata File (optional)
  - Property File
  - Collection mets.xml file (from DSpace)
  - EAD file (from ArchivesSpace)
- Metadata Conversion (XSL, optional)
  - If not present, read property file
  - CollectionMets.xsl
  - EAD.xsl
    - Specialized EAD conversion tools
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

## Workflow

## Questions
- Subject terms behavior (multi link)