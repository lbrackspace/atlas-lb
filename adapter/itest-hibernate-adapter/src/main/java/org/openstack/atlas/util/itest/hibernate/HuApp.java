package org.openstack.atlas.util.itest.hibernate;

import java.util.List;
import java.util.Map;
import org.hibernate.Transaction;
import org.openstack.atlas.util.itest.hibernate.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class HuApp {

    private HibernateUtil hu;
    private String db;

    public HuApp() {
        hu = new HibernateUtil();
    }

    public void clearDbMap() {
        hu.clearDbMap();
    }

    public void setDbMap(HibernateDbConf conf) {
        setDbMap(conf.getDbKey(), conf.getUrl(), conf.getUser(), conf.getPasswd(),
                conf.getHbm2ddl(), conf.getPackageName(), conf.getDriver(),
                conf.getDialect(), conf.getClassNames());
    }

    public void setDbMap(String db_key, String url, String user, String passwd,
            String hbm2ddl, String packageName, String driverClass,
            String dialect, List<String> classList) {
        hu.setDbMap(db_key, url, user, passwd, hbm2ddl, packageName, driverClass, dialect, classList);
        db = db_key;
    }

    public Map<String, SessionFactory> getDbMaps() {
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
        } catch (Exception e) {
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

    public Transaction begin() {
        return getSession().beginTransaction();
    }

    public Transaction begin(String dbKey){
        setDb(dbKey);
        return getSession().beginTransaction();
    }

    public void commit() {
        getSession().getTransaction().commit();
    }

    public void rollback() {
        getSession().getTransaction().rollback();
    }

    public void close() {
        getSession().close();
    }
}
