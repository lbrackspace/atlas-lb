#!/bin/sh

mysqldump -h 173.203.200.79 --opt -Q --routines --skip-extended-insert --triggers -h -p -u loadbalancing loadbalancing_usage > backup_usage.sql
