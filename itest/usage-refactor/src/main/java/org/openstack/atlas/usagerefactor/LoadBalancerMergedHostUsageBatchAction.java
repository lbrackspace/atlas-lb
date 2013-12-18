package org.openstack.atlas.usagerefactor;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.util.common.CalendarUtils;

import java.util.Collection;

public class LoadBalancerMergedHostUsageBatchAction implements BatchAction<LoadBalancerMergedHostUsage> {

    private Configuration hibernateConfig;

    public LoadBalancerMergedHostUsageBatchAction(Configuration hibernateConfig) {
        this.hibernateConfig = hibernateConfig;
    }

    @Override
    public void execute(Collection<LoadBalancerMergedHostUsage> loadBalancerMergedHostUsages) throws Exception {
        insertIntoDatabase(loadBalancerMergedHostUsages);
    }

    private void insertIntoDatabase(Collection<LoadBalancerMergedHostUsage> loadBalancerMergedHostUsages) throws Exception {
        final SessionFactory sessionFactory = hibernateConfig.buildSessionFactory();
        final Session session = sessionFactory.openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            System.out.println(String.format("Batch inserting %d records into lb_merged_host_usage table...", loadBalancerMergedHostUsages.size()));
            String query = generateBatchInsertQuery(loadBalancerMergedHostUsages);
            int i = session.createSQLQuery(query).executeUpdate();
            System.out.println(String.format("Inserted %d records into lb_merged_host_usage table.", i));
            tx.commit();
        } catch (Exception e) {
            System.err.print(e);
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
            sessionFactory.close();
        }
    }

    private String generateBatchInsertQuery(Collection<LoadBalancerMergedHostUsage> usages) {
        String sql = getNativeBatchInsertSql();
        NativeSqlStatement nativeSqlStatement = new NativeSqlStatement(sql);

        for (LoadBalancerMergedHostUsage usage : usages) {
            nativeSqlStatement.setString(1, Integer.toString(usage.getAccountId()));
            nativeSqlStatement.setString(2, Integer.toString(usage.getLoadbalancerId()));
            nativeSqlStatement.setString(3, Long.toString(usage.getOutgoingTransfer()));
            nativeSqlStatement.setString(4, Long.toString(usage.getIncomingTransfer()));
            nativeSqlStatement.setString(5, Long.toString(usage.getOutgoingTransferSsl()));
            nativeSqlStatement.setString(6, Long.toString(usage.getIncomingTransferSsl()));
            nativeSqlStatement.setString(7, Long.toString(usage.getConcurrentConnections()));
            nativeSqlStatement.setString(8, Long.toString(usage.getConcurrentConnectionsSsl()));
            nativeSqlStatement.setString(9, Integer.toString(usage.getNumVips()));
            nativeSqlStatement.setString(10, Integer.toString(usage.getTagsBitmask()));
            nativeSqlStatement.setString(11, "'" + CalendarUtils.calendarToString(usage.getPollTime()) + "'");

            if (usage.getEventType() != null) {
                nativeSqlStatement.setString(12, "'" + usage.getEventType() + "'");
            } else {
                nativeSqlStatement.setString(12, "NULL");
            }

            nativeSqlStatement.addBatch();
        }

        return nativeSqlStatement.generateSql();
    }

    private String getNativeBatchInsertSql() {
        return "INSERT INTO lb_merged_host_usage(account_id, loadbalancer_id, outgoing_transfer, incoming_transfer, outgoing_transfer_ssl, incoming_transfer_ssl, concurrent_connections, concurrent_connections_ssl, num_vips, tags_bitmask, poll_time, event_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
    }
}
