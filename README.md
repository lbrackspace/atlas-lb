Requirements
------------
  * Java >= 1.8_152
  * Apache Maven == 2.2.1/Maven3 can optionally be used.
  * Apache ActiveMQ == 5.15.2
  * Glassfish >= 5.0
  * MySql/MariaDB


TEST DO NOT MERGE

Installation
------------

* Clone this repo
    * git clone https://github.com/lbrackspace/atlas-lb.git
    * checkout the latest version branch

* Install JDK 1.8.0_152
    * Set JAVA_HOME Environment variable equal to the path to the JDK 1.8.0_152 directory
        * export JAVA_HOME=/path/to/java-jdk-1.8.0_152
    
* Install Maven
  * Ubuntu: - sudo apt-get install maven2
  * Set M2_HOME environment variable equal to the path to the maven install directory
  * export M2_HOME=/path/to/maven_install_directory
  * add settings.xml file to user's maven directory(settings.xml@contrib/maven)
  
* Install glassfish
    * http://download.oracle.com/glassfish/5.0/release/glassfish-5.0.zip
    * copy dependent .jars to /path/to/glassfish/lib
        * requires mysql-connector(will be transitioning to mariadb)
            * https://github.com/MariaDB/mariadb-connector-j
            
* Install ActiveMQ
    * https://archive.apache.org/dist/activemq/5.15.2/apache-activemq-5.15.2-bin.tar.gz
        * Install to /opt or desired install directory
  
* Install MySql or MariaDB
    * https://help.ubuntu.com/lts/serverguide/mysql.html
    * https://mariadb.org/
    
Configuration
------------- 
  
* Setup configurations
    * examples found in contrib/etc/openstack/atlas
    * should be placed in /etc/openstack/atlas
    
* Create databases
    * Create a database called 'loadbalancing' using the schema under contrib/db/loadbalancing_schema.sql
    * Create a database called 'loadbalancing_usage' using the schema under contrib/db/loadbalancing_usage_schema.sql
    * Seed the 'loadbalancing' database with sample data, using the file under contrib/db/loadbalancing_seed.sql

* Configure glassfish
    * Follow the steps in article below to create 2 mysql datasources in glassfish application server. These are the datasources used by the application to connect to the databases:
       http://www.albeesonline.com/blog/2008/08/06/creating-and-configuring-a-mysql-datasource-in-glassfish-application-server/

       * For the step 1 of that article, download the latest version of Mysql JDBC driver
            * optionally use mariadb connector - https://github.com/MariaDB/mariadb-connector-j
       * For the first data source pointing to database loadbalancing, in the step 17 of that article, name the jndi name as jdbc/loadBalancerDB.
       * For the second  data source pointing to database loadbalancing_usage, in the step 17 of that article, name the jndi name as jdbc/loadBalancerUsageDB.

 
Build
-----
  
* mvn clean install from the project root
     - creates public-web/target/atlas-public-web-x.y.z-SNAPSHOT.war
     - creates mgmt-web/target/atlas-mgmt-web-x.y.z-SNAPSHOT.war


Run/Deploy
---

*  Start ActiveMQ on default port.
    * java -jar /path/to/activemq/bin/run.jar start

* Deploy to Glassfish, choosing "/v1.0" as the Context Root
    * /path/to/glassfish/bin/asadmin deploy --contextroot "v1.0" /path/to/atlas-lb/api/public-web/target/atlas-public-web-VERSION-SNAPSHOT.war

*  Now you can access the Atlas REST APIs on port http://<hostname>:8080/v1.0/<tenant_id>/<resource>
    * Check create load balancer
      * POST http://localhost:8080/pub/{account_id}/loadbalancers
      
      
Info
----
For more info on the Atlas REST API, see the "RackSpace Cloud Load Balancers API 1.0" at https://developer.rackspace.com/docs/cloud-load-balancers/v1/getting-started/.


