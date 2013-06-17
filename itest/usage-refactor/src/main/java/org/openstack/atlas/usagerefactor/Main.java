package org.openstack.atlas.usagerefactor;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.openstack.atlas.service.domain.usage.entities.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main {

    private final static int BATCH_SIZE = 1000;
    private static LoadbalancingUsageConfig loadbalancingUsageConfig = new LoadbalancingUsageConfig();
    private static Configuration hibernateConfig = new Configuration();
    private static MigrationProcessor migrationProcessor = new MigrationProcessor();

    public static void main(String[] args) {
        try {
            loadHibernateConfigs();
            loadEntities();
            List<LoadBalancerUsage> loadBalancerUsages = getAllLoadBalancerUsages();
            List<LoadBalancerUsageEvent> loadBalancerUsageEvents = getAllLoadBalancerUsageEvents();
            List<LoadBalancerHostUsage> loadBalancerHostUsages = getAllLoadBalancerHostUsages();
            List<LoadBalancerMergedHostUsage> newUsages = migrationProcessor.process(loadBalancerUsages, loadBalancerUsageEvents, loadBalancerHostUsages);

            System.out.println(String.format("Inserting %d new usages into the 'lb_merged_host_usage' table...", newUsages.size()));
            LoadBalancerMergedHostUsageBatchAction batchAction = new LoadBalancerMergedHostUsageBatchAction(hibernateConfig);
            ExecutionUtilities.ExecuteInBatches(newUsages, BATCH_SIZE, batchAction);
            System.out.println(String.format("Successfully inserted %d new usages into the 'lb_merged_host_usage' table.", newUsages.size()));
        } catch (Exception e) {
            System.err.println("FATAL ERROR!");
            e.printStackTrace();
        }
    }

    private static void loadHibernateConfigs() {
        final Iterator keys = loadbalancingUsageConfig.getKeys();

        while (keys.hasNext()) {
            final String key = (String) keys.next();
            final String value = loadbalancingUsageConfig.getString(key);
            hibernateConfig.setProperty(key, value);
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

    private static List<LoadBalancerUsage> getAllLoadBalancerUsages() {
        final SessionFactory sessionFactory = hibernateConfig.buildSessionFactory();
        final Session session = sessionFactory.openSession();
        List<LoadBalancerUsage> loadBalancerUsageList = new ArrayList<LoadBalancerUsage>();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            System.out.println(String.format("Retrieving items from lb_usage..."));
            loadBalancerUsageList = session.createQuery("SELECT u FROM LoadBalancerUsage u ORDER BY u.startTime").list();
            System.out.println(String.format("Number of items retrieved from lb_usage: %d", loadBalancerUsageList.size()));
            tx.commit();
        } catch (Exception e) {
            System.err.print(e);
            if (tx != null) tx.rollback();
        } finally {
            session.close();
            sessionFactory.close();
        }

        return loadBalancerUsageList;
    }

    private static List<LoadBalancerUsageEvent> getAllLoadBalancerUsageEvents() {
        final SessionFactory sessionFactory = hibernateConfig.buildSessionFactory();
        final Session session = sessionFactory.openSession();
        List<LoadBalancerUsageEvent> loadBalancerUsageList = new ArrayList<LoadBalancerUsageEvent>();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            System.out.println(String.format("Retrieving items from lb_usage_event..."));
            loadBalancerUsageList = session.createQuery("SELECT e FROM LoadBalancerUsageEvent e ORDER BY e.startTime").list();
            System.out.println(String.format("Number of items retrieved from lb_usage_event: %d", loadBalancerUsageList.size()));
            tx.commit();
        } catch (Exception e) {
            System.err.print(e);
            if (tx != null) tx.rollback();
        } finally {
            session.close();
            sessionFactory.close();
        }

        return loadBalancerUsageList;
    }

    private static List<LoadBalancerHostUsage> getAllLoadBalancerHostUsages() {
        final SessionFactory sessionFactory = hibernateConfig.buildSessionFactory();
        final Session session = sessionFactory.openSession();
        List<LoadBalancerHostUsage> loadBalancerUsageList = new ArrayList<LoadBalancerHostUsage>();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            System.out.println(String.format("Retrieving items from lb_host_usage..."));
            loadBalancerUsageList = session.createQuery("SELECT h FROM LoadBalancerHostUsage h ORDER BY h.pollTime").list();
            System.out.println(String.format("Number of items retrieved from lb_host_usage: %d", loadBalancerUsageList.size()));
            tx.commit();
        } catch (Exception e) {
            System.err.print(e);
            if (tx != null) tx.rollback();
        } finally {
            session.close();
            sessionFactory.close();
        }

        return loadBalancerUsageList;
    }
}
