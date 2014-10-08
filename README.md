CMISFileBridgeWithMetadata
==========================

Simple CMIS 1.1 implementation with custom types and metadata support. 
If you want to know more about the CMIS Standard (see https://www.oasis-open.org/committees/cmis)
This server was developed with the great "server development guide" (see https://github.com/cmisdocs/ServerDevelopmentGuide)

Compilation
===========
It's a maven project. Just run
    
    mvn clean install

Usage
=====
The configuration file is /src/main/webapp/WEB_INF/classes/repository.properties.

    # Do not change unless if you know what your are doing
    class=org.example.cmis.server.FileBridgeCmisServiceFactory
    
    # typeDefinition directory
    # in this directory, you could save CMIS Type Definition in JSON format
    typeDefinition.test = /temp/CMISTypeDefinition
    
    # By default, only basic authentication is implemented
    # in future, token bearer will be available
    auth.mode = basic
    
    # Specified available users
    login.1 = test:test
    login.2 = reader:reader
    
    # CMIS Repository information
    # Directory for repository 'tenant1'
    repository.tenant1 = /temp/cmisFileBridge/tenant1
    # test's rights for repository 'tenant1'
    repository.tenant1.readwrite = test
    # reader's rights for repository 'tenant1'
    repository.tenant1.readonly = reader
    
    # Directory for repository 'tenant2'
    repository.tenant2 = /temp/cmisFileBridge/tenant2
    # test's rights for repository 'tenant2'
    repository.tenant2.readwrite = test
    # reader's rights for repository 'tenant2'
    repository.tenant2.readonly = reader

Installation
============
To install war file, you should use a Tomcat or Jetty server. For example, in Tomcat, just copy the war into /webapp directory and start your service. Every web services should be available at this address (http://localhost:8080/cmis-server).

Testing
=======
Which CMIS client should I use for testing this service?
    There is a good CMIS client developed by Apache Chemistry. Just download the archive available here (http://chemistry.apache.org/java/developing/tools/dev-tools-workbench.html). Uncompress it and run workbench.bat or workbench.sh (platform dependent).
    
    URL : http://localhost:8080/server/browser
    Binding : Browser
    Username : test
    Password : test
    Authentication : Standard
    Compression : On
    Client Compression : Off
    Cookies : On
