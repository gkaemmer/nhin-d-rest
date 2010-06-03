== Changelog ==

6/02/2010 	- Added JCE Policy Installation
			- Added installation of nhin-d-agent
			- Noted that the existing keystore/truststore documentation needs updating
			- Added S/MIME section with notes on disabling as well as configuring certs/trust.

== Introduction ==

This project is a proof of concept implementation of NHIN Direct REST APIs using
maven, spring 3.0, spring security 3.0, and embedded jetty. 

Building and running this project requires the following software:

JDK 1.6+ (1.6_20 used in development)
Maven 2.2.x (2.2.1 used in development)

== Installing Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files ==

You will need to install the JCE unlimited strength policy files in order for the S/MIME
encryption to function. To do this, either unpack and install the file
nhin-d-rest/spring-maven-poc/support/jce_policy-6.zip or retrieve the file yourself from
http://java.sun.com/javase/downloads/index.jsp. It's not clear to me the legality of 
restributing these files, so they may be removed from the hg repo in the future.

== Building == 

A prerequisite to building nhin-d-rest is having installed the nhin-d-jagent project. The 
most straightforward way to do that would be to check it out of svn and run 'mvn install' 
yourself (see http://code.google.com/p/nhin-d-jagent/source/checkout).

Because nhin-d-jagent is unstable at the moment, it's recommended that you instead install
the snapshot of nhin-d-jagent included in the nhin-d-rest project. To do this, go to the 
nhin-d-rest/spring-maven-poc/support directory and run:

	mvn install:install-file -DgroupId=org.nhind -DartifactId=nhin-d-agent -Dversion=1.0.0-SNAPSHOT -Dpackaging=jar -Dfile=nhin-d-agent-1.0.0-SNAPSHOT.jar
 
This will install the code to your local maven repository and make it available for nhin-d-rest.

To build and run the software first install Java and Maven. Add the {maven_home}/bin 
directory to your path. Make sure the JAVA_HOME environment variable points to a JDK.

Execute the following command:

	mvn jetty:run

The first execution of maven will take some time as all project dependencies download into 
your local maven repository.

== S/MIME ==

S/MIME support is achieved through use of nhin-d-jagent. Certificate/key/trust is managed
entirely within the HISP itself.

S/MIME is enabled by default but can be disabled by changeing the smimeEnabled property in
nhin-d-rest-servlet.xml:

	<bean id="messageService" class="org.nhindirect.platform.basic.BasicMessageService">
		<property name="smimeEnabled" value="true"/>
	</bean>

=== Endpoint Certificates/Trust Anchors ===

Each endpoint must have a file called "certs.jks" that stores the certificate and private key
for that endpoint. The file is stored in the same directory as the endpoint's message store:
data/endpoint@domain/certs.jks.

IMPORTANT: The certs.jks file must have a blank password. The file format is a Java Keystore.
Alias is unimportant. 

Each endpoint must have two trust stores called "trust-in.jks" and "trust-out.jks". These 
are also located in data/endpoint@domain. 

IMPORTANT: The trust-in.jks and trust-out.jks files must have blank passwords. The file format 
is a Java Keystore. Alias is unimportant. 

=== S/MIME Encryption/Decryption ===

When S/MIME is enabled all messages from the edge are encrypted upon receipt. All messages
from external HISPs (or internal messages from within the same HISP) are decrypted before
storing. Messages are stored decrypted in the data store.  

=== You mean for every user/endpoint I set up I have to generate a key/cert, store it manually, and copy in trust anchor files? ===

Yes. It's a little unwieldy. We may look into a demo mode where the HISP has the CA cert and 
can generate keys/certs/anchors for new users if they don't already exist.

If you need help setting up certs or anchors, contact the authors.  

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
a password of 'password'. The server key has been signed by a demo CA and is for 
"nhin.sunnyfamilypractice.example.org". There are two keystores in the etc/stores 
directory, one for nhin.sunnyfamilypractice.example.org and one for 
nhin.happyvalleypractice.example.org. Replace etc/keystore with either of these files
to swap out the server cert. 

The truststore contains a list of certificates or signing certificates for any
client that we trust to be a HISP. The truststore file in the repository contains
the demo CA certificate. 

The /driver/keys directory contains two key/cert combinations that have been signed by
the cert in the default truststore. These are appropriate for use by client software.

THIS INFORMATION IS NOW OBSOLETE, INSTEAD USE THE KEYSTORE AND TRUSTSTORE IN 
/etc/stores/nhintestca. TODO, FIX THIS. 

== Authentication & Authorization Notes ==

There are two methods for authenticating:

* HTTP Basic Authentication
* Client Cert Authentication

The SSL server will ask for a client cert. If it's provided, then Client Cert Authentication
takes place. If a client cert is not provided, HTTP Basic Authentication takes place.

=== Basic Authentication ===

Basic authentication is driven off the /src/main/webapp/WEB-INF/nhin-d-rest-security.xml file. 

Each user is specified along with their password and roles. There are two supported roles, 
ROLE_EDGE and ROLE_HISP. 

    <authentication-manager>
        <authentication-provider>
            <user-service>
                <user name="drsmith" password="drsmith" authorities="ROLE_EDGE"/>                
                <user name="drmarten" password="drmarten" authorities="ROLE_EDGE"/>
                <user name="nhin.healthypractice.example.org" authorities="ROLE_HISP"/>
            </user-service>
        </authentication-provider>
    </authentication-manager>

=== Roles ===    
    
A user with the ROLE_EDGE role will be considered an edge user. The user will be able to access 
the entire REST API but his POST /messages logic will be appropriate for an EDGE user.

A user with the ROLE_HISP role will be considered a HISP user. The user will only be able to 
access the POST /messages and PUT /messages/<MID>/status resources. Message processing logic 
will follow the remote HISP model and the username must match the domain of healthcare addresses 
for messages sent by this user.  

=== Client Cert Authentication ===

Any client cert that is either in our truststore or signed by a cert in our truststore will 
be accepted. The CN in the cert will be the username of the caller. This user will implicitly 
be given the ROLE_HISP role. These users are not required to be specified in the 
nhin-d-rest-security.xml file. 

== Domain Configuration ==

Health domains and endpoints are configured in a single properties file: /etc/domain.properties

The format looks like this:

	<healthaddress>: user1, user2, user3

For example:

	drsmith@nhin.sunnyfamilypractice.example.org: drsmith
	drmarten@nhin.sunnyfamilypractice.example.org: drmarten 

Note that a user can access and operate on behalf of multiple addresses and an address can
be accessed by multiple users. It's possible for multiple domains to be configured on a
single server.

This domain/endpoint registry is used for validation when messages are posted to the HISP, 
either from an edge user or a remote HISP. Any changes requires a restart of the server.

== Security ==

Each resource is protected with the following logic:

=== /messages GET ===

* User must have ROLE_EDGE. User must be provisioned to address specified in URI.
 
=== /messages POST ===

* User must have ROLE_EDGE or ROLE_HISP. 

For edge user: 'From' address must be provisioned to user. 'To' address in URI must 
match 'To' address in message. Local 'To' addresses are stored on HISP, remote 'To' 
addresses forwarded to remote HISP.

For HISP user: 'To' address must be valid local HISP address. 
	// DISABLED HISP user name must match domain in 'From' address.

=== /messages/<ID> GET ===

* User must have ROLE_EDGE. 
* User must be provisioned to address specified in URI.

=== /messages/<ID>/status GET ===

* User must have ROLE_EDGE. 
* User must be provisioned to address specified in URI.

=== /messages/<ID>/status PUT ===

* User must have ROLE_EDGE or ROLE_HISP. 

For Edge User: User must be provisioned to address specified in URI.

For HISP user: We pretty much trust that the HISP can set the status of any message. 
THIS IS DANGEROUS. We need a way to restrict the messages that the HISP can operate on. 

== Running the HISP ==

Once the project is running, you can try it out with a browser:

https://localhost:8443/nhin/v1/nhin.sunnyfamilypractice.example.org/drsmith/messages

You can't access the more interesting APIs unless you first post messages to the server.

The /driver/nhin-d-rest-client project contains a command line application that can be used
to exercise the API as either an edge or HISP.

== Working With the Code ==

An eclipse project can be generated for the project using the following maven command:

	mvn eclipse:eclipse 

== TODO ==

*	EDGE CASES, EDGE CASES, EDGE CASES... in particular problems with jagent aren't reported well.
*	Require 'accept-type' headers?	
*	Message ID should be set from the source, not the destination
*	Validate HISP to HISP interaction via DNS resolution of cert and address
*	Support multiple recipients
*	Support multiple user certificates in jagent
*	Formalize nhin-d-jagent relationship
*	"sent" messages stored on the source HISP are encrypted at the moment, unless the 
		message was to a local endpoint. Status should still work.
	

Chris Moyer (MedPlus)
cmoyer@medplus.com 
