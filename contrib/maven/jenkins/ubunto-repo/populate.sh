#!/bin/bash
set -x 

REPO=rs-repo
TOPDIR=/var/lib/jenkins/repo/ubuntu-repo
RELEASES="lucid"
CATEGORIES="main"
ARCHES="amd64"

cd ${TOPDIR}

# serialize this
lockfile-create LOCKFILE
trap "lockfile-remove LOCKFILE; exit $?" INT TERM EXIT

echo "PID $$ updating"

umask 002

for category in ${CATEGORIES}; do
    for d in `ls ${REPO}/incoming/${category}`; do
	firstletter=`echo ${d} | cut -b1`
	package=`echo ${d} | cut -d_ -f1`

	echo "Putting $d in ${REPO}/pool/${category}/${firstletter}/${package}"
	mkdir -p ${REPO}/pool/${category}/${firstletter}/${package}
	mv ${REPO}/incoming/${category}/${d} ${REPO}/pool/${category}/${firstletter}/${package}
    done
done


#apt-ftparchive generate apt-ftparchive.conf
cd ${REPO}
for release in ${RELEASES}; do
     apt-ftparchive packages --db cache/packages_all.db pool/${CATEGORIES} > Packages
     rm dists/${release}/${CATEGORIES}/binary-${ARCHES}/Packages.gz
     rm dists/${release}/${CATEGORIES}/binary-${ARCHES}/Packages.bz2
     cp Packages dists/${release} dists/${release}/${CATEGORIES}/binary-${ARCHES}
     gzip -9 dists/${release}/${CATEGORIES}/binary-${ARCHES}/Packages
     cp Packages dists/${release} dists/${release}/${CATEGORIES}/binary-${ARCHES}
     bzip2 -9 dists/${release}/${CATEGORIES}/binary-${ARCHES}/Packages
     cp Packages dists/${release} dists/${release}/${CATEGORIES}/binary-${ARCHES}
     rm Packages

     pushd dists/${release}
     rm -f Release Release.gpg
     apt-ftparchive release . -o APT::FTPArchive::Release::Origin="Rackspace Cloud" -o APT::FTPArchive::Release::Codename=${release}> Release
     gpg --batch -abs -o Release.gpg Release
     popd
done

echo "PID $$ done"

cd ${TOPDIR}

lockfile-remove LOCKFILE
