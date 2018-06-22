## PURPOSE
Project Page: http://georgetown-university-libraries.github.io/File-Analyzer/

The File Analyzer and Metadata Harvester is a general purpose desktop (and command line) 
tool designed to automate simple, file-based operations.  The File Analyzer assembles a toolkit of tasks a user can perform.

The tasks that have been written into the File Analyzer code base have been optimized for use by libraries, archives, 
and other cultural heritage institutions.

File Analyzer Wiki: https://github.com/Georgetown-University-Libraries/File-Analyzer/wiki

#### Demonstration Videos
[![Demonstration Video](https://i.ytimg.com/vi/kVi_k-HdH_4/1.jpg)](http://www.youtube.com/watch?v=kVi_k-HdH_4)
[![Demonstration Video](https://i.ytimg.com/vi/1I8n60ZrwHo/1.jpg)](http://www.youtube.com/watch?v=1I8n60ZrwHo)
[![Demonstration Video](https://i.ytimg.com/vi/5zYA04P0HPk/default.jpg)](http://www.youtube.com/watch?v=5zYA04P0HPk)

## History

This code has been derived from the NARA File Analyzer and Metadata Harvester which is available at 
https://github.com/usnationalarchives/File-Analyzer.

## PREREQUISITES
- JDK 1.7 or higher (for build)
- JRE 1.7 or higher (for runtime)
- (If you need to run with Java 6, see Releases for an older version)
- Maven (or you will need to compile the modules manually)

## INSTALLATION
- Clone this code to your computer
- Run mvn install
- Detailed Installation Instructions: https://github.com/Georgetown-University-Libraries/File-Analyzer/wiki/Installation-instructions

## DEPLOYMENTS
This code will build 3 flavors of the File Analyzer.

### Core File Analyzer 
* All code runs from a self-extracting jar file

### DSpace File Analyzer
* This version of the file analyzer is a self-extracting jar file that references the core file analyzer jar file. 
* It contains tools for automating the creation of DSpace ingestion folders

### Demo File Analyzer
* This version contains extensions illustrating various capabilities of the File Analyzer.  
* This version of the file analyzer is a self-extracting jar file that references both the core and dspace file analyzer jar files.
* This version of the application uses features of Apache Tika, BagIt, and Marc4j
 
***
[![Georgetown University Library IT Code Repositories](https://raw.githubusercontent.com/Georgetown-University-Libraries/georgetown-university-libraries.github.io/master/LIT-logo-small.png)Georgetown University Library IT Code Repositories](http://georgetown-university-libraries.github.io/)
