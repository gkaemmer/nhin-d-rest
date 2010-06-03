Proof of concept SMTP to/from REST based HISP

This project contains JAMES 2.3.2. Currently the deploy to JAMES is somewhat hackish 
so I just included it and included a (stolen and modified) ant script that does the build and deploy.

You will need to edit the config.xml file in the resources directory. Currently there is a matcher and mailet installed:

<mailet match="NhinMatcher" class="NhinMailet">
  <nhindprops>C:/work/nhind/Testing/JamesInterface/james-2.3.2/apps/james-plus/conf/nhind.properties</nhindprops>
  <storefolder>C:/work/nhind/Testing/JamesInterface/james-2.3.2/apps/james-plus/conf/jamesstore</storefolder>
  <truststore>C:/NHIND/nhin-d-rest/spring-maven-poc/etc/keystore</truststore>
  <truststorePassword>password</truststorePassword>
  <nhinURL>https://nhin.sunnyfamilypractices.example.org:8443/nhin/v1/</nhinURL>
</mailet>

 The "nhindprops" setting is the properties file for connecting to the HISP. It contains the user name and the password 
   for each user. This allows basic auth to work. This is currectly a security risk as the passwords are in plain text. 
   In the future we may wish to set the James mailet up as a pseudo HISP and do client cert connectivity. Right now the 
   mailet simply acts as a proxy for a user.
 The "storefolder" is where to store the mail messages. This is simply for debugging - I wanted to see payloads and such. 
   All of this code could be removed at some point.
 The "truststore" is where the trusted CA certs are for the TLS connection to the HISP
 The "truststorePassword" is, well, the password for the above
 The "nhinURL" is the base url for the HISP. 
 
The machine that this is installed on will need to open up ports 465 and 995 for POP and SMTP (over SSL).
The JAMES mail server is currently configured to accept SMTPS (SMTP over SSL) on port 465. SPOP (POP over SSL) on port 995.
  Please configure your mail client appropriately. You will need to install the trusted cert if this has not been done already.
  For testing we have checked in the ca cert (ca-cert.pem) into the resources directory.
  All of this configuration is in the config.xml if you wish to change it.
  
HOSTS file may need to be modified if host is not in DNS or testing locally.
 
The default ant task is to build an deploy, but not start, JAMES. please run it from the base directory - cannot remember 
  the little ant trick on directory pathing.
  
The project is deployed as a "SAR" file (like a WAR file with slightly different internal structure). JAMES unpacks this - 
  so there will not be a directory structure until JAMES is run.

To start JAMES go to the james-2.3.2/bin directory and run run.bat (Windows) or run.sh (*nix). 
  Note that the *nix had some permissions issues when I first installed this may need to chmod 
  the .sh files for execute rights.

Once JAMES is started you will have to add users. First connect to the JAMES admin console - telnet localhost 4555. 
  The user name is currently "admin" and the password is "pass" these can be configured in config.xml. 
  The command to add a user is "adduser <name> <password>". You can list existing users with "listusers". 
  If you redeploy JAMES the existing users will be wiped.
  
  NOTE: you will need to add a user for the HISP to use to send mail out. This is spring configured - 
    currently the user is "NHIN", password is "password" and host is "localhost".

The main JAMES log files are in james-2.3.2/logs the actual logs for the mailet 
  and such are in james-2.3.2/apps/james-plus/logs
  
Configuring the HISP:
There is one file that needs to be edited in the HISP project. This is the smtp.properties file in the etc directory. 
  This file should contain name and and email address pairs. It is similar too (though backwards) the 
  domain.properties file. When a message is stored on the HISP the code will check and see if there is a match - 
  the endpoint of the address to the user entry in the properties file. If there is a match the code will attempt 
  to send this message on via SMTP. Please note configuration of this user above. 
