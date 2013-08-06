#!/bin/bash

###########################################
############## Public API #################
###########################################
war=`ls api/public-web/target/atlas-public-web-*.war`
name=`basename -s .war $war`
root="pub"
echo -n "Redeploying the public api '$name'... "
asadmin undeploy $name &> /dev/null
asadmin deploy --contextroot "$root" $war &> /dev/null
if [ $? -eq 0 ]
then
        echo "OK"
        echo "Successfully redeployed '$name' as '$root'."
else
        echo "FAIL"
        echo "Failed to deploy '$name' as '$root'."
fi

###########################################
############ Management API ###############
###########################################
war=`ls api/mgmt-web/target/atlas-mgmt-web-*.war`
name=`basename -s .war $war`
root="mgmt"
echo -n "Redeploying the management api '$name'... "
asadmin undeploy $name &> /dev/null
asadmin deploy --contextroot "$root" $war &> /dev/null
if [ $? -eq 0 ]
then
        echo "OK"
        echo "Successfully redeployed '$name' as '$root'."
else
        echo "FAIL"
        echo "Failed to deploy '$name' as '$root'."
fi
