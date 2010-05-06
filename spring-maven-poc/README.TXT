This project is a proof of concept implementation of NHIN Direct REST APIs using
maven, spring 3.0, and embedded jetty. It is not meant to be anything other than
a demonstration on bootstrapping a Java development environment for use in the  
NHIN Direct REST Implementation workgroup.

Building and running this project requires the following software:

JDK 1.5+ (1.6_20 used in development)
Maven 2.2.x (2.2.1 used in development)

To build and run the software first install Java and Maven. Add the {maven_home}/bin 
directory to your path. Make sure the JAVA_HOME environment variable points to a JDK.

Execute the following command:

	mvn jetty:run

The first execution of maven will take quite a bit of time as all project dependencies 
are downloaded into your local maven repository.

Once the project is running, you can try it out with a browser:

http://localhost:8080/nhin/v1/nhin.sunnyfamilypractice.example.org/drsmith/messages

You can't access the more interesting APIs unless you first post messages to the server. To
do this, either perform an HTTP POST to the same URL or unzip the included data.zip file.

Once the data has been submitted (or extracted) to the server you can perform the following
operations:

http://localhost:8080/nhin/v1/nhin.sunnyfamilypractice.example.org/drsmith/message/466af1ff-f27c-444c-ab49-466611d9d9a9
http://localhost:8080/nhin/v1/nhin.sunnyfamilypractice.example.org/drsmith/status/466af1ff-f27c-444c-ab49-466611d9d9a9

Changing the status of a message is an exercise left to the reader.

An eclipse project can be generated for the project using the following maven command:

	mvn eclipse:eclipse 


TODO:
	Process correct content type headers for atom feed and other resources, ignored for now
	Parse submitted messages for from/to addresses
	Push messages to remote HISP
	Create a real message store via embedded DB or something less hokey than current file system store
	Make sure the to/from semantics on the URI are correct 

	DONE Sort atom feed by message date
	DONE Filter non-new messages from atom feed

Chris Moyer (MedPlus)
cmoyer@medplus.com 
