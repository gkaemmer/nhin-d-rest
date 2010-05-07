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

http://localhost:8080/nhin/1_0/urn:nhin:nhin.sunnyfamilypractice.example.org:drsmith/messages

You can't access the more interesting APIs unless you first post messages to the server. To
do this, either perform an HTTP POST to the same URL or unzip the included data.zip file.

Once the data has been submitted (or extracted) to the server you can perform the following
operations:

http://localhost:8080/nhin/1_0/urn:nhin:nhin.sunnyfamilypractice.example.org:drsmith/message/1
http://localhost:8080/nhin/1_0/urn:nhin:nhin.sunnyfamilypractice.example.org:drsmith/status/2

Changing the status of a message is an exercise left to the reader.

An eclipse project can be generated for the project using the following maven command:

	mvn eclipse:eclipse 





Chris Moyer (MedPlus)
cmoyer@medplus.com 
