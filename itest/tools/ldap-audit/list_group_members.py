#!/usr/bin/env python

from utils import printf
import getpass
import utils
import sys
import os

def usage(prog):
    printf("usage is %s <conf_file> <group_name>\n", prog)
    printf("\b")
    printf("list the members of the specified group name\n")
    printf("\n")

if __name__ == "__main__":
    prog = sys.argv[0]
    if len(sys.argv) < 3:
        usage(prog)
        sys.exit() 
    conf_file = sys.argv[1]
    group_name = sys.argv[2]
    printf("Enter username: ")
    sys.stdout.flush()
    user = utils.chop(sys.stdin.readline())
    passwd = getpass.getpass("Enter password: ")
    conn = utils.LdapConnecter(user, passwd, conf_file=conf_file)
    conn.bind()  # Login with user name and password
    members = conn.get_group_members_ssos(group_name)
    conn.unbind()
    ssos = members.keys()
    ssos.sort()
    n_groups = len(ssos)
    printf("group %s has %d members\n", group_name, n_groups);
    printf("-----------------------------------------------------------\n")
    llen = max([len(sso) for sso in ssos])
    for sso in ssos:
        lsso = "%s" % ((sso + ":").ljust(llen + 2),)
        printf("%s%s\n", lsso, members[sso])
    printf("\n")
