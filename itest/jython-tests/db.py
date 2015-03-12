
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

def getZxtmCreds(cid):
    eps = []
    begin()
    c = qq("from Cluster where id=%s"%cid)
    commit()
    username = c[0].getUsername()
    ctext = c[0].getPassword()
    ptext = CryptoUtil.decrypt(ctext)
    return (username,ptext)

def buildClassImportFile(outfile,infile):
    fp = open(utils.fullPath(outfile),"w")
    fp.write("#!/usr/bin/env jython\n")
    for className in HibernateDbConf.getHibernateClasses(infile):
        fp.write("import %s as %s\n"%(className,className.split(".")[-1]))
    fp.close()

def save_list(obj_list):
    for obj in obj_list:
        app.save(obj)

