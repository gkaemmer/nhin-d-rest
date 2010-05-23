== Introduction ==

This project is a proof of concept implementation of NHIN Direct REST APIs using
maven, spring 3.0, spring security 3.0, and embedded jetty. 

Building and running this project requires the following software:

JDK 1.6+ (1.6_20 used in development)
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
                <user name="drruby" password="drruby" authorities="ROLE_EDGE"/>
                <user name="drmoyer" password="drmoyer" authorities="ROLE_EDGE"/>
                <user name="sunnydocs" password="sunnydocs" authorities="ROLE_EDGE"/>                
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

	drsmith@nhin.sunnyfamilypractice.example.org: drsmith,
	drruby@nhin.sunnyfamilypractice.example.org: drruby,sunnydocs
	drmarten@nhin.sunnyfamilypractice.example.org: drmarten,sunnydocs 

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

	Require 'accept-type' headers?
	

Chris Moyer (MedPlus)
cmoyer@medplus.com 
