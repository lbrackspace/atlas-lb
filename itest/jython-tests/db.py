
import org.openstack.atlas.util.itest.hibernate.HibernateDbConf as HibernateDbConf
import org.openstack.atlas.util.itest.hibernate.HuApp as HuApp
import org.openstack.atlas.util.crypto.CryptoUtil as CryptoUtil

import utils
import sys
import os

app = HuApp()

def printf(format,*args): sys.stdout.write(format%args)

def fprintf(fp,format,*args): fp.write(format%args)


def getDb(*args):
    global app
    if len(args)>0:
        huConf = HibernateDbConf.newHibernateConf(args[0])
    else:
        huConf = HibernateDbConf.newHibernateConf(utils.conf_file)
    app = HuApp()
    app.setDbMap(huConf)
    return app

def begin(*args):
    if len(args)>=1:
        app.setDb(args[0])
    printf("useing db = \"%s\"\n",app.getDb())
    tx = app.getSession().beginTransaction()
    return tx;

def commit():
    app.getSession().getTransaction().commit()

def rollback():
    app.getSession().getTransaction().rollback()

def getSession():
    return app.getSession()

def qq(query):
    s = app.getSession()
    q = s.createQuery(query)
    return q.list()

def getZxtmCreds():
    eps = {}
    begin()
    cs = qq("select c.id, c.username, c.password from Cluster c")
    q = "select h.id, h.cluster.id, h.endpoint from Host h"
    q += " where h.hostStatus = 'SOAP_API_ENDPOINT'"
    hs = qq(q)    
    commit()
    username = cs[0][1]
    ctext = cs[0][2]
    ptext = CryptoUtil.decrypt(ctext)
    for h in hs:
        cid = h[1]
        soap = h[2]
        eps[cid] = {"username":username, "passwd":ptext, "endpoint":soap}
    return eps

def buildClassImportFile(outfile,infile):
    fp = open(utils.fullPath(outfile),"w")
    fp.write("#!/usr/bin/env jython\n")
    for className in HibernateDbConf.getHibernateClasses(infile):
        fp.write("import %s as %s\n"%(className,className.split(".")[-1]))
    fp.close()

def save_list(obj_list):
    for obj in obj_list:
        app.save(obj)

