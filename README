Requirements
------------
  Java >= 1.5
  Apache Maven == 2.2.1 (Use the 'settings.xml' file located in the contrib/maven directory)
  Apache ActiveMQ == 5.5.0
  Glassfish >= 3.1
  MySql >= 5.x


Getting Started
---------------
  1) Create a MySql database called 'loadbalancing'

  2) Follow the steps in article below to create a mysql data-source in the Glassfish application server. This is the
     data-source used by the application to connect to the database:

       http://www.albeesonline.com/blog/2008/08/06/creating-and-configuring-a-mysql-datasource-in-glassfish-application-server/

       i)  For step 1 of that article, download and apply the latest version of Mysql JDBC driver ie. 5.0.8 version
       ii) For step 17 of that article, name the jndi name as 'jdbc/atlasCoreDB'

  3) Create a directory named '/etc/openstack/atlas' and copy over all of the files in the contrib/etc/openstack/atlas
     directory.

  4) Run 'mvn clean install' to build artifacts

  5) Start ActiveMQ on default port
  
  6) Deploy the core-public-web-x.y.z-SNAPSHOT.war located in the core-api/core-public-web/target directory to
     Glassfish, choosing '/v1.1' as the context root

  7) Seed the 'loadbalancing' database with fake data (cluster, hosts, virtual ips, etc.) provided in the file
     'core-seed.sql' located in the contrib/db/ directory

  8) Now you can access the Atlas REST API via http://<hostname>:8080/v1.1/<tenant_id>/<resource>


  For more information please visit the following:

    http://wiki.openstack.org/Atlas-LB (API Documentation)
    https://launchpad.net/atlas-lb (Process Management)
    http://openstack.org/ (Openstack)
