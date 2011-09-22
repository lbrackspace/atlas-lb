#!/bin/sh

mysqldump -h 173.203.200.79 --opt -Q --routines --skip-extended-insert --triggers -p -u loadbalancing loadbalancing > backup_lb.sql
