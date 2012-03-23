#!/bin/sh
cd /var/lib/jenkins/repo/ubuntu-repo
./markset qa
./markset all
rsync -avz -e ssh --delete /var/lib/jenkins/repo/ubuntu-repo/rs-repo root@qarepo.host.com:/var/www/ubuntu
sudo rsync -av --delete /var/lib/jenkins/repo/ubuntu-repo/rs-repo /var/qarepo/ubuntu
