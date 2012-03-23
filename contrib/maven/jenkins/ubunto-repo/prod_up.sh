#!/bin/sh
cd /var/lib/jenkins/repo/ubuntu-repo
./markset prod
./markset all

rsync -avz -e ssh --delete /var/lib/jenkins/repo/ubuntu-repo/rs-repo root@prodrepo.host.com:/var/www/ubuntu
sudo rsync -av --delete /var/lib/jenkins/repo/ubuntu-repo/rs-repo /var/prodrepo/ubuntu

