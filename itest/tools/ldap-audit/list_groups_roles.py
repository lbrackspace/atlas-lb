#!/usr/bin/env python

from utils import printf
import getpass
import utils
import sys
import os

def usage(prog):
    printf("usage is %s <conf_file>\n", prog)
    printf("get the user group and roles based on the conf file\n")
    printf("You will be prompted for your SSO user and passwd\n")
    printf("\n")

if __name__ == "__main__":
    prog = sys.argv[0]
    if len(sys.argv) < 2:
        usage(prog)
        sys.exit() 
    conf_file = sys.argv[1]
    printf("Enter username: ")
    sys.stdout.flush()
    user = utils.chop(sys.stdin.readline())
    passwd = getpass.getpass("Enter password: ")
    conn = utils.LdapConnecter(user, passwd, conf_file=conf_file)
    conn.bind()  # Login with user name and password
    (groups, roles) = conn.get_groups_and_roles()
    conn.unbind()
    printf("User %s is a member of %d groups\n", user, len(groups))
    printf("--------------------------------------------------------\n")
    for (group_cn, group_dn) in groups:
        printf("%s\n", group_dn)
    printf("\n")
    printf("User %s has %d roles\n", user, len(roles))
    printf("--------------------------------------------------------\n")
    for role in roles:
        printf("%s\n", role)
    printf("\n")
