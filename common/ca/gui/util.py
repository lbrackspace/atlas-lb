#!/usr/bin/env jython

import org.openstack.atlas.util.ca.RSAKeyUtils as RSAKeyUtils
import org.openstack.atlas.util.ca.PemUtils as PemUtils
import org.openstack.atlas.util.ca.CsrUtils as CsrUtils
import org.openstack.atlas.util.ca.CertUtils as CertUtils
import org.openstack.atlas.util.ca.util.ConnectUtils as ConnectUtils

ConnectUtils.getSupportedCiphers()

def getCiphers(host,port):
    available = ConnectUtils.getSupportedCiphers()
    ciphers = ConnectUtils.getServerCiphers(host,port)
    return (set(available)-set(ciphers), ciphers)
