package org.hexp.hibernateexp;

import java.util.List;
import java.util.Map;
import org.hibernate.Transaction;
import org.hexp.hibernateexp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class HuApp {
    private HibernateUtil hu;
    private String db;


    public static void main(String[] args) {
        System.out.printf("I don't do anything I'm just here cause my developer doesn't know how to do mvn assembly:assembly\n");
        System.out.printf("Correctly for Library Packages.");
    }

    public HuApp() {
        hu = new HibernateUtil();
    }

    public void clearDbMap() {
        hu.clearDbMap();
    }

    public void setDbMap(String db_key,String url, String user, String passwd, String hbm2ddl,String packageName,List<String> classList) {
        hu.setDbMap(db_key,url,user,passwd,hbm2ddl,packageName,classList);
        db = db_key;
    }

    public Map <String,SessionFactory>getDbMaps() {
       return hu.getSfm();
    }

    public void delete(Object obj) {
        Session session = hu.getInstance(db).getCurrentSession();
        session.beginTransaction();
        session.delete(obj);
        session.flush();
        session.getTransaction().commit();
    }

    public Object getHibernateObjectbyStringCol(String table, String col, String val) {
        List resp;
        Session session = hu.getInstance(db).getCurrentSession();
        String q_str = String.format("from %s where %s=:val", table, col);

        resp = session.createQuery(q_str).setString("val", val).list();
        if (resp.size() != 1) {
            return null;
        }
        return resp.get(0);
    }

    public List getList(String query) {
        List resp;
        Session session;

        session = hu.getInstance(db).getCurrentSession();
        //session.beginTransaction();
        resp = session.createQuery(query).list();
        //session.getTransaction().commit();
        return resp;
    }

    public Session getSession() {
        return hu.getInstance(db).getCurrentSession();
    }

    public List getAll(String class_name) throws ClassNotFoundException {
        // example usage App.getAll("VirtualIpType")
        List resp;
        Class cls;
        String package_name = "com.rackspace.config.service.domain.entities";
        cls = Class.forName( package_name + "." + class_name);

        Session session = hu.getInstance(db).getCurrentSession();
        //session.beginTransaction();
        resp = session.createCriteria(cls).list();
        //session.getTransaction().commit();
        return resp;
    }

    public void addObj(Object obj) {
        Session session;
        Class cls;

        cls = obj.getClass();
        session = hu.getInstance(db).getCurrentSession();
        try {
        session.beginTransaction();
        session.save(obj);
        session.flush();
        session.getTransaction().commit();
        }catch(Exception e) {
            session.getTransaction().rollback();
        }

    }

    public void saveOrUpdate(Object obj) {
        Session session = hu.getInstance(db).getCurrentSession();
        session.beginTransaction();
        session.saveOrUpdate(obj);
        session.flush();
        session.getTransaction().commit();
    }

    public static void addviptype(String name, String description) {
        System.out.printf("Invalid\n");
    }

    public HibernateUtil getHu() {
        return hu;
    }

    public void setHu(HibernateUtil hu) {
        this.hu = hu;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }
}
