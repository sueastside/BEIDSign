.be eID Sign
============

.be eID Sign is an Alfresco extension to sign PDF documents with the [Belgian Identity card](http://eid.belgium.be/en/)

![.be eID site](http://eid.belgium.be/en/binaries/logo_eid-en_tcm406-106385.jpg)


##Running in debug

###Start Alfresco and Share

    export MAVEN_OPTS="-Xms256m -Xmx2048m -XX:MaxPermSize=512m -Xdebug -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=n"
    mvn clean integration-test -Palfresco-share

###Start the service
    cd beidsign-service
    mvn clean integration-test -Pservice


##Installation
    mvn clean package

 Install the amps to alfresco and share and deploy the beidsign-service.war along side the alfresco-tier.
 
##Running in production
Create a 'beid-config.cfg' file in the 'Company Home' directory, make sure only admin has rights to it.
Add the following content:
	# This is how alfresco communicates with the service, this isn't external.
    serviceURL=http://localhost:8081/beidsign-service/
    # This is how the service communicates with the applet and how resources are served to the client, this is external.
	serviceURLHttps=https://server.my:8444/beidsign-service/

Notes
-----
The beidsign-service needs to run on https for the eid-applet to work!


Dependencies
-----
eid-applet - GPL 3 (https://code.google.com/p/eid-applet/source/browse/trunk/LICENSE.txt)

iText - Affero GPL 3 (http://itextpdf.com/terms-of-use/agpl.php)

jQuery - MIT License (https://jquery.org/license/)

JQuery SignaturePad - BSD (http://thomasjbradley.ca/lab/signature-pad/#license)


![screenshot](http://imgbin.org/images/thumbs/ext17440.png)
