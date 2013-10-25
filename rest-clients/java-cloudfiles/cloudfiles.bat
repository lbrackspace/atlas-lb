@ECHO OFF
SET SCRIPT_DIR=%~dp0
java -classpath %SCRIPT_DIR%lib/httpcore-4.1.jar;%SCRIPT_DIR%lib/commons-cli-1.1.jar;%SCRIPT_DIR%lib/httpclient-4.0.3.jar;%SCRIPT_DIR%lib/commons-lang-2.4.jar;%SCRIPT_DIR%lib/junit.jar;%SCRIPT_DIR%lib/commons-codec-1.3.jar;%SCRIPT_DIR%lib/commons-io-1.4.jar;%SCRIPT_DIR%lib/commons-logging-1.1.1.jar;%SCRIPT_DIR%lib/log4j-1.2.15.jar;%SCRIPT_DIR%lib/json-simple-1.1.1.jar;%SCRIPT_DIR%dist/java-cloudfiles.jar;%SCRIPT_DIR%. com.rackspacecloud.client.cloudfiles.sample.FilesCli %*
@ECHO ON
