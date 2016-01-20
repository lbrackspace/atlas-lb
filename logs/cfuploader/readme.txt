Python based Cloud files uploader to be used to replace hadoop.

create a json file in /etc/openstack/atlas/cfuploader.json with the following structure
{
  "db": {
    "passwd": "DBPASSWD", 
    "host": "censored_db_host", 
    "db": "loadbalancing", 
    "user": "DBUSER"
  },
  "auth_user": "IdentityUser", 
  "auth_url": "https://identity.api.rackspacecloud.com", 
  "auth_passwd": "IDENTITYPASSWD",
  "n_workers": 40
}


