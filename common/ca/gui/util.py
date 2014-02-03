#!/usr/bin/env jython

import org.openstack.atlas.util.ca.RSAKeyUtils as RSAKeyUtils
import org.openstack.atlas.util.ca.PemUtils as PemUtils
import org.openstack.atlas.util.ca.CsrUtils as CsrUtils
import org.openstack.atlas.util.ca.CertUtils as CertUtils

kp = RSAKeyUtils.genKeyPair(1024)
x = RSAKeyUtils.toPKCS8(kp)
out = PemUtils.toPemString(x)
