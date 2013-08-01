#!/usr/bin/env jython

import org.openstack.atlas.util.ca.primitives.RsaConst as RsaConst
import org.openstack.atlas.util.ca.zeus.ZeusUtils as ZeusUtils
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile as ZeusCrtFile
import org.openstack.atlas.util.ca.PemUtils as PemUtils
import org.openstack.atlas.util.ca.CertUtils as CertUtils
import org.openstack.atlas.util.ca.CsrUtils as CsrUtils
import org.openstack.atlas.util.ca.RSAKeyUtils as RSAKeyUtils
import org.openstack.atlas.util.ca.util.X509ReaderWriter as X509ReaderWriter
import org.openstack.atlas.util.ca.util.X509Inspector as X509Inspector

def fetchCrts(url):
    crts = X509ReaderWriter.getX509CertificateObjectsFromSSLServer(url)
    return crts
    

