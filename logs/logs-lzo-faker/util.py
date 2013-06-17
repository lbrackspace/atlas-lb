#!/usr/bin/env jython

import org.openstack.atlas.logs.hibernatetoy.HibernateDbConf as HibernateDbConf
import org.openstack.atlas.logs.hibernatetoy.HuApp as HuApp
import org.openstack.atlas.logs.lzofaker.LzoFakerMain as LzoFakerMain

import sys
import os

app = HuApp() #Database app

def printf(format,*args): sys.stdout.write(format%args)

def fprintf(fp,format,*args): fp.write(format%args)


def setDbConfig(confFile):
    huConf = HibernateDbConf.newHibernateConf("./local.json")
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


