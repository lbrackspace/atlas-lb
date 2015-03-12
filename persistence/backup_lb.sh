#!/bin/sh

mysqldump -h 127.0.0.1 --opt -Q --routines --skip-extended-insert --triggers -p -u root loadbalancing > backup_lb.sql
