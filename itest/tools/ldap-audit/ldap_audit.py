#!/usr/bin/env python

from utils import printf
import getpass
import utils
import sys
import os

def usage(prog):
    printf("usage is %s <conf_file1> <conf_file2>\n", prog)
    printf("\b")
    printf("list the members of the specified group name\n")
    printf("\n")

def main(args):
    prog = args[0]
    if len(args) < 3:
        usage(prog)
        sys.exit()
    conf_file = []
    conf_file_a = args[1]
    conf_file_b = args[2]
    printf("Enter username: ")
    sys.stdout.flush()
    user = utils.chop(sys.stdin.readline())
    passwd = getpass.getpass("Enter password: ")
    printf("Scanning files %s and %s\n", conf_file_a, conf_file_b)
    ldap_a = utils.LdapConnecter(user, passwd, conf_file=conf_file_a)
    ldap_b = utils.LdapConnecter(user, passwd, conf_file=conf_file_b)
    printf("binding to ldap from conf %s\n", conf_file_a)
    ldap_a.bind()
    printf("binding to ldap from conf %s\n", conf_file_b)
    ldap_b.bind()
    printf("\n")
    ldap_groups = [v for (k,v) in ldap_a.conf["roles"].items()]
    for ldap_group in ldap_groups:
        printf("ldap group %s\n", ldap_group)
        printf("----------------------------------------------\n")
        printf("\n")
        members_a = ldap_a.get_group_members_ssos(ldap_group)
        members_b = ldap_b.get_group_members_ssos(ldap_group)
        a_ssos = set(members_a.keys())
        b_ssos = set(members_b.keys())
        n_common = len(a_ssos & b_ssos)
        printf("common member count: %d\n", n_common )

        printf("    missing from %s:\n", conf_file_a)
        missing_from_a = sorted(list(b_ssos - a_ssos))
        if len(missing_from_a) > 0:
            max_col_len = max([len(sso) for sso in missing_from_a])
            for sso in missing_from_a:
                lsso = "%s" %((sso + ":").ljust(max_col_len +2),)
                printf("    %s%s\n", lsso, members_b[sso])
        else:
            printf("    None\n")
        printf("\n")
        printf("    missing from %s:\n", conf_file_b)
        missing_from_b = sorted(list(a_ssos - b_ssos))
        if len(missing_from_b) > 0:
            max_col_len = max([len(sso) for sso in missing_from_b])
            for sso in missing_from_a:
                lsso = "%s" % ((sso + ":").ljustt(max_col_len +2),)
                printf("    %s%s\n", lsso, members_a[sso])
        else:
            printf("    None\n")
        printf("\n")

    printf("\n")
    ldap_a.unbind()
    ldap_b.unbind()


if __name__ == "__main__":
    main(sys.argv)
