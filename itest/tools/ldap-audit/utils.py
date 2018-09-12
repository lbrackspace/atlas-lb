#!/usr/bin/env python

from requests.structures import CaseInsensitiveDict as UncasedDict
import ldap
import json
import sys
import os


def printf(format,*args): sys.stdout.write(format%args)


def load_json(pathIn):
    return json.loads(open(os.path.expanduser(pathIn),"r").read())


def save_json(pathOut,obj):
    open(os.path.expanduser(pathOut),"w").write(json.dumps(obj,indent=2))


def chop(line_in):
    return line_in.replace("\r", "").replace("\n", "")


class LdapConnecter(object):
    def __init__(self,user, passwd,
                 conf_file="/etc/openstack/atlas/ldap.json"):
        self.user = user
        self.passwd = passwd
        self.conf = load_json(conf_file)
        if self.conf["scope"] == "subtree":
            self.scope = ldap.SCOPE_SUBTREE
        elif self.conf["scope"] == "onelevel":
            self.scope = ldap.SCOPE_ONELEVEL
        self.l = None
        self.groups2roles = UncasedDict()
        for(role_name, group_comma_list) in self.conf["roles"].items():
            for group_name in group_comma_list.split(","):
                self.groups2roles[group_name] = role_name

    def search(self, rdn, scope, query):
        results = self.l.search_s(rdn, scope, query)
        return results

    def get_groups_and_roles(self):
        query_fmt = self.conf["groupConfig"]["userQuery"]
        memberField = self.conf["groupConfig"]["memberField"]
        user_rdn = self.conf["groupConfig"]["dn"]
        sdn = self.conf["groupConfig"]["dn"]
        query = query_fmt % (self.user,)
        results = self.l.search_s(user_rdn, self.scope, query)
        roles = []
        groups = []
        out = (groups, roles)
        memberField = self.conf["groupConfig"]["memberField"]
        if  memberField not in results[0][1]:
            return out
        for memberShip in results[0][1][memberField]:
            tup = ["", memberShip]
            cn = None
            ava_array = ldap.dn.str2dn(memberShip)
            for rdn in ava_array:
                if len(rdn) != 1:
                    continue  # We won't consider multivalued rdns
                (key, value, junk) = rdn[0]
                if key.lower() == "cn":
                    cn = value
                    break
            if cn:
                tup[0] = cn
            groups.append(tuple(tup))
        for(group_cn, group_dn) in groups:
            if group_cn in self.groups2roles:
                roles.append(self.groups2roles[group_cn])
        return out

    def bind(self):
        ldap.set_option(ldap.OPT_X_TLS_REQUIRE_CERT, ldap.OPT_X_TLS_NEVER)
        #ldap.set_option(ldap.OPT_DEBUG_LEVEL, 4095)
        host = self.conf["host"]
        port = int(self.conf["port"])
        if self.conf["isactivedirectory"]:
            user_dn = "%s%s" % (self.user, self.conf["appendtoname"])
        else:
            sdn = self.conf["userConfig"]["sdn"]
            dn = self.conf["userConfig"]["dn"]
            user_dn = "%s=%s,%s" % (sdn, self.user, dn)
        passwd = self.passwd
        printf("%s\n", user_dn)
        self.l = ldap.initialize("ldaps://%s:%s" % (host, port))
        #self.l = ldap.initialize("ldaps://%s:%s"%(host,port),trace_level=2)
        self.l.simple_bind_s(user_dn, passwd)

    def unbind(self):
        if self.l is None:
            return
        self.l.unbind_s()
        self.l = None
        return
