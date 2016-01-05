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
 
# License information is contained below.

    ------------------------------------------------------------------------
    
    Copyright (c) 2013, Georgetown University Libraries
    All rights reserved.
    
    Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
    
    Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
    
# The NARA license for the base code is contained below.
    -------------------------------------------------------------------------
    
    NARA OPEN SOURCE AGREEMENT VERSION 1.3, 
    Based on NASA Open Source Agreement for Government Agencies, as approved by the Open Source Initiative. 
    THIS OPEN SOURCE AGREEMENT ("AGREEMENT") DEFINES THE RIGHTS OF USE,
    REPRODUCTION, DISTRIBUTION, MODIFICATION AND REDISTRIBUTION OF CERTAIN
    COMPUTER SOFTWARE ORIGINALLY RELEASED BY THE UNITED STATES GOVERNMENT
    AS REPRESENTED BY THE GOVERNMENT AGENCY LISTED BELOW ("GOVERNMENT
    AGENCY"). THE UNITED STATES GOVERNMENT, AS REPRESENTED BY GOVERNMENT
    AGENCY, IS AN INTENDED THIRD-PARTY BENEFICIARY OF ALL SUBSEQUENT
    DISTRIBUTIONS OR REDISTRIBUTIONS OF THE SUBJECT SOFTWARE. ANYONE WHO
    USES, REPRODUCES, DISTRIBUTES, MODIFIES OR REDISTRIBUTES THE SUBJECT
    SOFTWARE, AS DEFINED HEREIN, OR ANY PART THEREOF, IS, BY THAT ACTION,
    ACCEPTING IN FULL THE RESPONSIBILITIES AND OBLIGATIONS CONTAINED IN
    THIS AGREEMENT.
    Government Agency: National Archives and Records Administration (NARA)
    Government Agency Original Software Designation: org.nara.nwts.fileAnalyzer
    Government Agency Original Software Title: NARA File Analyzer and Metadata Harvester.
    User Registration Requested. Please Visit http://www.archives.gov/social-media/github.html.
    Government Agency Point of Contact for Original Software: OpenGov@nara.gov.
    1. DEFINITIONS
    A. "Contributor" means Government Agency, as the developer of the
    Original Software, and any entity that makes a Modification.
    B. "Covered Patents" mean patent claims licensable by a Contributor
    that are necessarily infringed by the use or sale of its Modification
    alone or when combined with the Subject Software.
    C. "Display" means the showing of a copy of the Subject Software,
    either directly or by means of an image, or any other device.
    D. "Distribution" means conveyance or transfer of the Subject
    Software, regardless of means, to another.
    E. "Larger Work" means computer software that combines Subject
    Software, or portions thereof, with software separate from the Subject
    Software that is not governed by the terms of this Agreement.
    F. "Modification" means any alteration of, including addition to or
    deletion from, the substance or structure of either the Original
    Software or Subject Software, and includes derivative works, as that
    term is defined in the Copyright Statute, 17 USC 101. However, the
    act of including Subject Software as part of a Larger Work does not in
    and of itself constitute a Modification.
    G. "Original Software" means the computer software first released
    under this Agreement by Government Agency with Government Agency
    designation National Archives and Records Administration (NARA) and entitled
    NARA File Analyzer and Metadata Harvester, including source code,
    object code and accompanying documentation, if any.
    H. "Recipient" means anyone who acquires the Subject Software under
    this Agreement, including all Contributors.
    I. "Redistribution" means Distribution of the Subject Software after a
    Modification has been made.
    J. "Reproduction" means the making of a counterpart, image or copy of
    the Subject Software.
    K. "Sale" means the exchange of the Subject Software for money or
    equivalent value.
    L. "Subject Software" means the Original Software, Modifications, or
    any respective parts thereof.
    M. "Use" means the application or employment of the Subject Software
    for any purpose.
    2. GRANT OF RIGHTS
    A. Under Non-Patent Rights: Subject to the terms and conditions of
    this Agreement, each Contributor, with respect to its own contribution
    to the Subject Software, hereby grants to each Recipient a
    non-exclusive, world-wide, royalty-free license to engage in the
    following activities pertaining to the Subject Software:
    1. Use
    2. Distribution
    3. Reproduction
    4. Modification
    5. Redistribution
    6. Display
    B. Under Patent Rights: Subject to the terms and conditions of this
    termination, a Recipient agrees to immediately cease use and
    distribution of the Subject Software. All sublicenses to the Subject
    Software properly granted by the breaching Recipient shall survive any
    such termination of this Agreement.
    B. Severability: If any provision of this Agreement is invalid or
    unenforceable under applicable law, it shall not affect the validity
    or enforceability of the remainder of the terms of this Agreement.
    C. Applicable Law: This Agreement shall be subject to United States
    federal law only for all purposes, including, but not limited to,
    determining the validity of this Agreement, the meaning of its
    provisions and the rights, obligations and remedies of the parties.
    D. Entire Understanding: This Agreement constitutes the entire
    understanding and agreement of the parties relating to release of the
    Subject Software and may not be superseded, modified or amended except
    by further written agreement duly executed by the parties.
    E. Binding Authority: By accepting and using the Subject Software
    under this Agreement, a Recipient affirms its authority to bind the
    Recipient to all terms and conditions of this Agreement and that that
    Recipient hereby agrees to all terms and conditions herein.
    F. Point of Contact: Any Recipient contact with Government Agency is
    to be directed to the designated representative as follows:
    ___________________________________________________________.

