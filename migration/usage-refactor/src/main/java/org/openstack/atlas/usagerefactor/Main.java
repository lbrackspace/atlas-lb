package org.openstack.atlas.usagerefactor;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.openstack.atlas.service.domain.usage.entities.*;

import java.util.Iterator;
import java.util.List;

public class Main {

    private static UsageRefactorConfig usageRefactorConfig = new UsageRefactorConfig();
    private static Configuration hibernateConfig = new Configuration();

    public static void main(String[] args) {
        printConfiguration();
        loadEntities();
        getAllUsageRecords();
    }

    private static void printConfiguration() {
        final Iterator keys = usageRefactorConfig.getKeys();

        while (keys.hasNext()) {
            final String key = (String) keys.next();
            final String value = usageRefactorConfig.getString(key);
            System.out.println(key + "=" + value);
            hibernateConfig.setProperty(key, value);
        }

        System.out.println(usageRefactorConfig.getString(ConfigKeys.loaadbalancing_usage_jdbc_connection));
        System.out.println(usageRefactorConfig.getString(ConfigKeys.loadbalancing_usage_username));
        System.out.println(usageRefactorConfig.getString(ConfigKeys.loadbalancing_usage_password));
    }

    private static void getAllUsageRecords() {
        final SessionFactory sessionFactory = hibernateConfig.buildSessionFactory(); // TODO: Better manage sessionFactory
        final Session session = sessionFactory.openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            List<LoadBalancerUsage> list = session.createQuery("FROM LoadBalancerUsage").list();

            System.out.println(String.format("Number usages retrieved: %d", list.size()));
            for (LoadBalancerUsage loadBalancerUsage : list) {
                System.out.print(loadBalancerUsage);
            }
            tx.commit();
        } catch (Exception e) {
            System.err.print(e);
            if (tx != null) tx.rollback();
        } finally {
            session.close();
            sessionFactory.close();
        }
    }

    private static void loadEntities() {
        hibernateConfig.addAnnotatedClass(Entity.class);
        hibernateConfig.addAnnotatedClass(HostUsage.class);
        hibernateConfig.addAnnotatedClass(LoadBalancerHostUsage.class);
        hibernateConfig.addAnnotatedClass(LoadBalancerMergedHostUsage.class);
        hibernateConfig.addAnnotatedClass(LoadBalancerUsage.class);
        hibernateConfig.addAnnotatedClass(LoadBalancerUsageEvent.class);
    }
}
