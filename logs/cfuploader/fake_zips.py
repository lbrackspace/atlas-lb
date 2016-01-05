#!/usr/bin/env python

from utils import *
import sys

if __name__ == "__main__":
    if len(sys.argv)<3:
        printf("usage is %s <account_id> <n_hours>\n",sys.argv[0])
        printf("\n")
        printf("creates fake zips\n")
        sys.exit()
    aid = int(sys.argv[1])
    n_hours = int(sys.argv[2])
    create_fake_zips(aid,n_hours)

