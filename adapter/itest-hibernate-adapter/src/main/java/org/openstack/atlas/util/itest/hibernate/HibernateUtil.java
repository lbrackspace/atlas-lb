package org.openstack.atlas.util.itest.hibernate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private Map<String, SessionFactory> sfm;

    public HibernateUtil() {
        sfm = new HashMap<String, SessionFactory>();
    }

    public void clearDbMap() {
        getSfm().clear();
    }

    public void setDbMap(String db_key, String url, String user, String passwd, String hbm2ddl, String packageName, String driverClass, String dialect, List<String> classList) {
        try {
            AnnotationConfiguration ac;
            ac = new AnnotationConfiguration().setProperty("hibernate.connection.url", url).
                    setProperty("hibernate.connection.username", user).
                    setProperty("hibernate.connection.password", passwd).
                    setProperty("hibernate.hbm2ddl.auto", hbm2ddl).
                    setProperty("hibernate.connection.driver_class", driverClass).
                    setProperty("hibernate.connection.pool_size", "1").
                    setProperty("hibernate.dialect", dialect).
                    setProperty("hibernate.current_session_context_class", "thread").
                    setProperty("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider").
                    setProperty("hibernate.show_sql", "true").
                    setProperty("hibernate.jdbc.batch_size", "50");

            if (packageName != null && !packageName.equals("")) {
                ac = ac.addPackage(packageName);
            }

            for (String mappedClassName : classList) {
                ac = ac.addAnnotatedClass(Class.forName(mappedClassName));
            }
            getSfm().put(db_key, ac.buildSessionFactory());
        } catch (Throwable t) {
            System.err.println(t.getMessage());
            t.printStackTrace();
        }
    }

    public SessionFactory getInstance(String db_key) {
        return getSfm().get(db_key);
    }

    public Session openSession(String db_key) {
        return getSfm().get(db_key).openSession();


    }

    public Session getCurrentSession(String db_key) {
        return getSfm().get(db_key).getCurrentSession();
    }

    public void close(String db_key) {
        if (getSfm().get(db_key) != null) {
            getSfm().get(db_key).close();


        }
        getSfm().put(db_key, null);
    }

    public Map<String, SessionFactory> getSfm() {
        return sfm;
    }

    public void setSfm(Map<String, SessionFactory> sfm) {
        this.sfm = sfm;
    }
}
