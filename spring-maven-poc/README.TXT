== Introduction ==

This project is a proof of concept implementation of NHIN Direct REST APIs using
maven, spring 3.0, spring security 3.0, and embedded jetty. 

Building and running this project requires the following software:

JDK 1.5+ (1.6_20 used in development)
Maven 2.2.x (2.2.1 used in development)


== Building == 

To build and run the software first install Java and Maven. Add the {maven_home}/bin 
directory to your path. Make sure the JAVA_HOME environment variable points to a JDK.

Execute the following command:

	mvn jetty:run

The first execution of maven will take some time as all project dependencies download into 
your local maven repository.


== SSL Notes ==

The project starts an HTTPS server running on port 8443. The server port is configurable in
pom.xml. 

	<connector implementation="org.mortbay.jetty.security.SslSocketConnector">
		<port>8443</port>
		<maxIdleTime>60000</maxIdleTime>
		<keystore>etc/keystore</keystore>
		<password>password</password>
		<keyPassword>password</keyPassword>
		<truststore>etc/truststore</truststore>
		<trustPassword>password</trustPassword>
		<wantClientAuth>true</wantClientAuth>
	</connector>   

To revert back to non-SSL HTTP remove (or comment out) the entire <connector>...</connector> 
section. HTTP will be enabled on port 8080. 

=== Keystore/Truststore ===

The key and certificate for the server are stored in etc/keystore. This keystore has 
a password of 'password'. The server key has been signed by a demo CA. 

The truststore contains a list of certificates or signing certificates for any
client that we trust to be a HISP. The truststore file in the repository contains
the demo CA certificate. 


== Authentication Notes ==

You can authenticate to the server either with a client certificate signed by a cert in
our truststore or with a certificate contained in the truststore itself. User principal
for x509 authenticated users will be the CN value from the cert. All x509 authenticated
users get the ROLE_HISP_SENDER role and are assumed to be HISPs.

Basic authentication is provided for edge senders. 

The following users are set up in this demo:

drsmith - password: drsmith
drjones - password: drjones

The nhin-d-rest-client project contains a sample certificate that can be used to call
as a HISP.


== Running the HISP ==

Once the project is running, you can try it out with a browser:

https://localhost:8443/nhin/v1/nhin.sunnyfamilypractice.example.org/drsmith/messages

You can't access the more interesting APIs unless you first post messages to the server. To
do this, either perform an HTTP POST to the same URL or unzip the included data.zip file.

Once the data has been submitted (or extracted) to the server you can perform the following
operations:

https://localhost:8443/nhin/v1/nhin.sunnyfamilypractice.example.org/drsmith/message/466af1ff-f27c-444c-ab49-466611d9d9a9
https://localhost:8443/nhin/v1/nhin.sunnyfamilypractice.example.org/drsmith/status/466af1ff-f27c-444c-ab49-466611d9d9a9

The nhin-d-rest-client project contains a command line application that can be used
to exercise the API as either an edge or HISP.


== Working With the Code ==

An eclipse project can be generated for the project using the following maven command:

	mvn eclipse:eclipse 


== TODO: ==

	Keep list of authenticated users mapped to health addresses. Perform authorization to restrict access
	Process correct content type headers for atom feed and other resources, ignored for now
	Parse submitted messages for from/to addresses
	Push messages to remote HISP
	Create a real message store via embedded DB or something less hokey than current file system store



Chris Moyer (MedPlus)
cmoyer@medplus.com 
