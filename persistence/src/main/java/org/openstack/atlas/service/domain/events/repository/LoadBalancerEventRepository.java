package org.openstack.atlas.service.domain.events.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.entities.*;
import org.openstack.atlas.service.domain.events.pojos.AccountLoadBalancerServiceEvents;
import org.openstack.atlas.service.domain.events.pojos.LoadBalancerServiceEvents;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.*;
import org.openstack.atlas.service.domain.util.Constants;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

import static org.openstack.atlas.service.domain.services.impl.VirtualIpServiceImpl.DEL_PTR_FAILED;
import static org.openstack.atlas.service.domain.services.impl.VirtualIpServiceImpl.DEL_PTR_PASSED;

@Repository
@Transactional
public class LoadBalancerEventRepository {

    final Log LOG = LogFactory.getLog(LoadBalancerEventRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;
    private final Integer PAGE_SIZE = 10;
    private final int MAX_SERVICE_EVENT = 20;

    public LoadBalancerEventRepository() {
    }

    public LoadBalancerEventRepository(EntityManager em) {
        this.entityManager = em;
    }

    public void save(AccessListEvent accessListEvent) {
        entityManager.persist(accessListEvent);
    }

    public void save(ConnectionLimitEvent connectionLimitEvent) {
        entityManager.persist(connectionLimitEvent);
    }

    public void save(HealthMonitorEvent healthMonitorEvent) {
        entityManager.persist(healthMonitorEvent);
    }

    public void save(LoadBalancerEvent loadbalancerEvent) {
        entityManager.persist(loadbalancerEvent);
    }

    public void save(LoadBalancerServiceEvent loadbalancerServiceEvent) {
        entityManager.persist(loadbalancerServiceEvent);
    }

    public void save(NodeEvent nodeEvent) {
        entityManager.persist(nodeEvent);
    }

    public void save(NodeServiceEvent nodeServiceEvent) {
        entityManager.persist(nodeServiceEvent);
    }

    public void save(VirtualIpEvent virtualIpEvent) {
        entityManager.persist(virtualIpEvent);
    }

    public void save(SessionPersistenceEvent sessionPersistenceEvent) {
        entityManager.persist(sessionPersistenceEvent);
    }

    public LoadBalancerServiceEvents getAllPTREventsByLoadBalancer(Integer loadbalancerId) {
        String qStr = "SELECT e FROM LoadBalancerServiceEvent e "
                + "where e.loadbalancerId = :loadbalancerId and e.type = :type";
        Query q = entityManager.createQuery(qStr).setParameter("loadbalancerId", loadbalancerId).setParameter("type", EventType.DELETE_VIRTUAL_IP);
        List<LoadBalancerServiceEvent> events = q.getResultList();
        LoadBalancerServiceEvents lbsEvents = new LoadBalancerServiceEvents();
        for (LoadBalancerServiceEvent event : events) {
            String title = event.getTitle();
            if (title == null) {
                continue;
            }
            if (title.equals(DEL_PTR_FAILED) || title.equals(DEL_PTR_PASSED)) {
                lbsEvents.getLoadBalancerServiceEvents().add(event);
            }
        }
        lbsEvents.setLoadbalancerId(loadbalancerId);
        return lbsEvents;
    }

     public LoadBalancerServiceEvents getAllAtomHopperEventsByLoadBalancer(Integer loadbalancerId) {
        String qStr = "SELECT e FROM LoadBalancerServiceEvent e "
                + "where e.loadbalancerId = :loadbalancerId and e.type = :type";
        Query q = entityManager.createQuery(qStr).setParameter("loadbalancerId", loadbalancerId).setParameter("type", EventType.AH_USAGE_EXECUTION);
        List<LoadBalancerServiceEvent> events = q.getResultList();
        LoadBalancerServiceEvents lbsEvents = new LoadBalancerServiceEvents();
        for (LoadBalancerServiceEvent event : events) {
            String title = event.getTitle();
            if (title == null) {
                continue;
            }
            if (title.equals(Constants.AH_USAGE_EVENT_FAILURE)) {
//                lbsEvents.getLoadBalancerServiceEvents().add(event);
            }
            lbsEvents.getLoadBalancerServiceEvents().add(event);
        }
        lbsEvents.setLoadbalancerId(loadbalancerId);
        return lbsEvents;
    }

    public List<LoadBalancerServiceEvent> getAllEventsForAccount(Integer accountId, Integer page) {
        Query query = entityManager.createQuery("SELECT evt FROM LoadBalancerServiceEvent evt where evt.accountId = :accountId order by evt.created desc").setParameter("accountId", accountId).setMaxResults(PAGE_SIZE);

        if (page != null && page > 0) {
            query = query.setFirstResult((page - 1) * PAGE_SIZE);
        }

        return query.getResultList();
    }

    public LoadBalancerServiceEvents getAllEventsForUsername(String username, Integer page, Calendar startDate, Calendar endDate) {
        Query query = entityManager.createQuery("SELECT lbe FROM LoadBalancerEvent lbe WHERE lbe.author = :author AND lbe.created BETWEEN :startDate AND :endDate ORDER BY lbe.created ASC").setParameter("author", username).setParameter("startDate", startDate).setParameter("endDate", endDate);

        List<LoadBalancerServiceEvent> lsv = query.getResultList();
        LoadBalancerServiceEvents events = new LoadBalancerServiceEvents();
        events.getLoadBalancerServiceEvents().addAll(lsv);
        return events;
    }

    public List<NodeEvent> getAllNodeEvents(Integer accountId, Integer loadbalancerId, Integer page) {
        Query query = entityManager.createQuery("SELECT evt FROM NodeEvent evt where evt.accountId = :accountId and evt.loadbalancerId = :loadbalancerId order by evt.created desc").setParameter("accountId", accountId).setParameter("loadbalancerId", loadbalancerId).setMaxResults(PAGE_SIZE);

        if (page != null && page > 0) {
            query = query.setFirstResult((page - 1) * PAGE_SIZE);
        }

        return query.getResultList();
    }

    public List<VirtualIpEvent> getAllVirtualIpEvents(Integer accountId, Integer loadbalancerId, Integer page) {
        Query query = entityManager.createQuery("SELECT evt FROM VirtualIpEvent evt where evt.accountId = :accountId and evt.loadbalancerId = :loadbalancerId order by evt.created desc").setParameter("accountId", accountId).setParameter("loadbalancerId", loadbalancerId).setMaxResults(PAGE_SIZE);

        if (page != null && page > 0) {
            query = query.setFirstResult((page - 1) * PAGE_SIZE);
        }

        return query.getResultList();
    }

    public List<NodeEvent> getNodeEvents(Integer accountId, Integer loadbalancerId, Integer nodeId, Integer page) {
        Query query = entityManager.createQuery("SELECT evt FROM NodeEvent evt where evt.accountId = :accountId and evt.loadbalancerId = :loadbalancerId and evt.nodeId = :nodeId order by evt.created desc").setParameter("accountId", accountId).setParameter("loadbalancerId", loadbalancerId).setParameter("nodeId", nodeId).setMaxResults(PAGE_SIZE);

        if (page != null && page > 0) {
            query = query.setFirstResult((page - 1) * PAGE_SIZE);
        }

        return query.getResultList();
    }

    public List<NodeServiceEvent> getNodeServiceEvents(Integer accountId, Integer loadbalancerId, Integer page, Integer... p) {
        Integer offset = 0;
        Integer limit = 100;
        Integer marker = 0;

        CustomQuery cq;
        String selectClause = "SELECT evt FROM NodeServiceEvent evt";
        cq = new CustomQuery(selectClause);

        cq.addParam("evt.accountId", "=", "accountId", accountId);
        cq.addParam("evt.loadbalancerId", "=", "loadbalancerId", loadbalancerId);

        if (p.length >= 2) {
            offset = p[0];
            limit = p[1];
            marker = p[2];
            int i = 0;
            if (offset == null) {
                offset = 0;
            }
            if (limit == null || limit > 100) {
                limit = 100;
            }
            if (marker == null) {
                marker = 0;
            }
            cq.addParam("evt.nodeId", ">=", "nodeId", marker);
        }

        String qStr = cq.getQueryString();

        Query query = entityManager.createQuery(qStr);

        for (QueryParameter param : cq.getQueryParameters()) {
            query.setParameter(param.getPname(), param.getValue());
        }

        //ATOM
        if (page != null && page > 0) {
            query.setFirstResult((page - 1) * PAGE_SIZE);
        } else {
            query.setFirstResult(offset);
        }
        query.setMaxResults(limit);
        return query.getResultList();
    }

    public List<HealthMonitorEvent> getAllHealthMonitorEvents(Integer accountId, Integer loadbalancerId, Integer page) {
        Query query = entityManager.createQuery("SELECT evt FROM HealthMonitorEvent evt where evt.accountId = :accountId and evt.loadbalancerId = :loadbalancerId order by evt.created desc").setParameter("accountId", accountId).setParameter("loadbalancerId", loadbalancerId).setMaxResults(PAGE_SIZE);

        if (page != null && page > 0) {
            query = query.setFirstResult((page - 1) * PAGE_SIZE);
        }

        return query.getResultList();
    }

    public List<AccessListEvent> getAllAccessListEvents(Integer accountId, Integer loadbalancerId, Integer page) {
        Query query = entityManager.createQuery("SELECT evt FROM AccessListEvent evt where evt.accountId = :accountId and evt.loadbalancerId = :loadbalancerId order by evt.created desc").setParameter("accountId", accountId).setParameter("loadbalancerId", loadbalancerId).setMaxResults(PAGE_SIZE);

        if (page != null && page > 0) {
            query = query.setFirstResult((page - 1) * PAGE_SIZE);
        }

        return query.getResultList();
    }

    public List<ConnectionLimitEvent> getAllConnectionLimitEvents(Integer accountId, Integer loadbalancerId, Integer page) {
        Query query = entityManager.createQuery("SELECT evt FROM ConnectionLimitEvent evt where evt.accountId = :accountId and evt.loadbalancerId = :loadbalancerId order by evt.created desc").setParameter("accountId", accountId).setParameter("loadbalancerId", loadbalancerId).setMaxResults(PAGE_SIZE);

        if (page != null && page > 0) {
            query = query.setFirstResult((page - 1) * PAGE_SIZE);
        }

        return query.getResultList();
    }

    public List<SessionPersistenceEvent> getAllSessionPersistenceEvents(Integer accountId, Integer loadbalancerId, Integer page) {
        Query query = entityManager.createQuery("SELECT evt FROM SessionPersistenceEvent evt where evt.accountId = :accountId and evt.loadbalancerId = :loadbalancerId order by evt.created desc").setParameter("accountId", accountId).setParameter("loadbalancerId", loadbalancerId).setMaxResults(PAGE_SIZE);

        if (page != null && page > 0) {
            query = query.setFirstResult((page - 1) * PAGE_SIZE);
        }

        return query.getResultList();
    }

    public List<LoadBalancerEvent> getAllLoadBalancerEvents(Integer accountId, Integer loadbalancerId, Integer page) {
        Query query = entityManager.createQuery("SELECT evt FROM LoadBalancerEvent evt where evt.accountId = :accountId and evt.loadbalancerId = :loadbalancerId order by evt.created desc").setParameter("accountId", accountId).setParameter("loadbalancerId", loadbalancerId).setMaxResults(PAGE_SIZE);

        if (page != null && page > 0) {
            query = query.setFirstResult((page - 1) * PAGE_SIZE);
        }

        return query.getResultList();
    }

    public AccountLoadBalancerServiceEvents getRecentLoadBalancerServiceEvents(Integer accountId, String startDate, String endDate) throws DateTimeToolException {
        Query q;
        String qStr;
        String qHead;
        StringBuilder qMid = new StringBuilder();
        String qTail;
        Calendar startCal;
        Calendar endCal;
        AccountLoadBalancerServiceEvents out = new AccountLoadBalancerServiceEvents();
        out.setAccountId(accountId);
        LoadBalancerServiceEvents lbEvents = new LoadBalancerServiceEvents();
        List<Integer> lbIds = new ArrayList<Integer>();
        List<LoadBalancerServiceEvent> resultList;
        Map<Integer, List<LoadBalancerServiceEvent>> lbid2EventList;
        lbid2EventList = new HashMap<Integer, List<LoadBalancerServiceEvent>>();
        endCal = null;

        qHead = "select l from LoadBalancerServiceEvent l "
                + "where  l.accountId = :accountId ";


        qTail = " order by created";

        qMid.append(" and created >= :startDate ");

        if (startDate != null) {
            startCal = DateTimeTools.parseDate(startDate);
            DateTimeTools.setCalendarAttrs(startCal, null, null, null, 0, 0, 0); // Early morning
        } else {
            startCal = Calendar.getInstance();
            startCal.add(Calendar.DAY_OF_MONTH, -60);
            DateTimeTools.setCalendarAttrs(startCal, null, null, null, 0, 0, 0); // Early morning
        }

        if (endDate != null) {
            endCal = DateTimeTools.parseDate(endDate);
            DateTimeTools.setCalendarAttrs(endCal, null, null, null, 23, 59, 59); // Midnight;
            qMid.append(" and created <= :endDate ");
        }

        qStr = String.format("%s%s%s", qHead, qMid.toString(), qTail);
        q = entityManager.createQuery(qStr).setParameter("accountId", accountId);

        q.setParameter("startDate", startCal);
        if (endDate != null) {
            q.setParameter("endDate", endCal);
        }

        resultList = q.getResultList();
        for (LoadBalancerServiceEvent resultEvent : resultList) {
            Integer lbId = resultEvent.getLoadbalancerId();
            if (!lbid2EventList.containsKey(lbId)) {
                lbid2EventList.put(lbId, new ArrayList<LoadBalancerServiceEvent>());
                lbIds.add(lbId);
            }
            LoadBalancerServiceEvent eventOut = new LoadBalancerServiceEvent();
            eventOut.setCategory(resultEvent.getCategory());
            eventOut.setSeverity(resultEvent.getSeverity());
            eventOut.setDescription(resultEvent.getDescription());
            eventOut.setAuthor(resultEvent.getAuthor());
            eventOut.setCreated(resultEvent.getCreated());
            lbid2EventList.get(lbId).add(eventOut);
        }
        Collections.sort(lbIds);
        for (Integer lbId : lbIds) {
            lbEvents = new LoadBalancerServiceEvents();
            lbEvents.setLoadbalancerId(lbId);
            lbEvents.getLoadBalancerServiceEvents().addAll(lbid2EventList.get(lbId));
            out.getLoadBalancerServiceEvents().add(lbEvents);
        }
        return out;
    }

    public AccountLoadBalancerServiceEvents getRecentLoadBalancerServiceEventsByLbId(LoadBalancer lb, String startDate, String endDate) throws DateTimeToolException, EntityNotFoundException {
        Query q;
        String qStr;
        String qHead;
        StringBuilder qMid = new StringBuilder();
        String qTail;
        Calendar startCal;
        Calendar endCal;
        AccountLoadBalancerServiceEvents out = new AccountLoadBalancerServiceEvents();
        out.setAccountId(lb.getAccountId());
        LoadBalancerServiceEvents lbEvents = new LoadBalancerServiceEvents();
        List<Integer> lbIds = new ArrayList<Integer>();
        List<LoadBalancerServiceEvent> resultList;
        Map<Integer, List<LoadBalancerServiceEvent>> lbid2EventList;
        lbid2EventList = new HashMap<Integer, List<LoadBalancerServiceEvent>>();
        endCal = null;

        qHead = "select l from LoadBalancerServiceEvent l " + "where  l.accountId = :accountId and loadbalancer_id = :lbId";
        qTail = " order by created";
        qMid.append(" and created >= :startDate ");

        if (startDate != null) {
            startCal = DateTimeTools.parseDate(startDate);
            DateTimeTools.setCalendarAttrs(startCal, null, null, null, 0, 0, 0); // Early morning
        } else {
            startCal = Calendar.getInstance();
            startCal.add(Calendar.DAY_OF_MONTH, -1);
            DateTimeTools.setCalendarAttrs(startCal, null, null, null, 0, 0, 0); // Early morning
        }

        if (endDate != null) {
            endCal = DateTimeTools.parseDate(endDate);
            DateTimeTools.setCalendarAttrs(endCal, null, null, null, 23, 59, 59); // Midnight;
            qMid.append(" and created <= :endDate ");
        }

        qStr = String.format("%s%s%s", qHead, qMid.toString(), qTail);
        q = entityManager.createQuery(qStr).setParameter("accountId", lb.getAccountId()).setParameter("lbId", lb.getId()).setMaxResults(MAX_SERVICE_EVENT).setParameter("startDate", startCal);
        if (endDate != null) {
            q.setParameter("endDate", endCal);
        }

        resultList = q.getResultList();
        lbEvents = new LoadBalancerServiceEvents();
        LoadBalancerServiceEvent loadBalancerServiceEvent;
        for (LoadBalancerServiceEvent lbse : resultList) {
            loadBalancerServiceEvent = new LoadBalancerServiceEvent();
            loadBalancerServiceEvent.setCategory(lbse.getCategory());
            loadBalancerServiceEvent.setSeverity(lbse.getSeverity());
            loadBalancerServiceEvent.setDescription(lbse.getDescription());
            loadBalancerServiceEvent.setAuthor(lbse.getAuthor());
            loadBalancerServiceEvent.setCreated(lbse.getCreated());
            lbEvents.getLoadBalancerServiceEvents().add(loadBalancerServiceEvent);
        }
        out.getLoadBalancerServiceEvents().add(lbEvents);

        return out;
    }

    public void removeLoadBalancerServiceEventEntries() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -90);
        entityManager.createQuery("DELETE FROM LoadBalancerServiceEvent a where a.created <= :days").setParameter("days", cal).executeUpdate();
    }

    public void removeNodeEventEntries() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -90);
        entityManager.createQuery("DELETE FROM NodeEvent a where a.created <= :days").setParameter("days", cal).executeUpdate();
    }

    public void removeNodeServiceEventEntries() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -90);
        entityManager.createQuery("DELETE FROM NodeServiceEvent a WHERE a.created <= :days")
                .setParameter("days", cal).executeUpdate();
    }

    public void removeVirtualIpEventEntries() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -90);
        entityManager.createQuery("DELETE FROM VirtualIpEvent a where a.created <= :days").setParameter("days", cal).executeUpdate();
    }

    public void removeHealthMonitorEventEntries() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -90);
        entityManager.createQuery("DELETE FROM HealthMonitorEvent a where a.created <= :days").setParameter("days", cal).executeUpdate();
    }

    public void removeAccessListEventEntries() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -90);
        entityManager.createQuery("DELETE FROM AccessListEvent a where a.created <= :days").setParameter("days", cal).executeUpdate();
    }

    public void removeConnectionLimitEventEntries() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -90);
        entityManager.createQuery("DELETE FROM ConnectionLimitEvent a where a.created <= :days").setParameter("days", cal).executeUpdate();
    }

    public void removeSessionPersistenceEventEntries() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -90);
        entityManager.createQuery("DELETE FROM SessionPersistenceEvent a where a.created <= :days").setParameter("days", cal).executeUpdate();
    }

    public void removeLoadBalancerEventEntries() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -90);
        entityManager.createQuery("DELETE FROM LoadBalancerEvent a where a.created <= :days").setParameter("days", cal).executeUpdate();
    }

    public Set<LbIdAccountId> getLoadBalancersActiveDuringPeriod(Calendar startTime, Calendar endTime) {
        Set<LbIdAccountId> lbIds = new HashSet<LbIdAccountId>();

//        Query query = entityManager.createQuery("SELECT l.loadbalancerId, l.accountId FROM LoadBalancerEvent l where (l.status != 'DELETED' or l.updated >= :startTime) and l.created < :endTime and l.status not in ('BUILD', 'PENDING_DELETE')")
        Query query = entityManager.createQuery("SELECT l.loadbalancerId, l.accountId FROM LoadBalancerEvent l where (l.type != 'DELETED' or l.updated >= :startTime) and l.created < :endTime and l.status not in ('BUILD', 'PENDING_DELETE')")
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime);

        final List<Object[]> resultList = query.getResultList();

        for (Object[] row : resultList) {
            Integer loadBalancerId = (Integer) row[0];
            Integer accountId = (Integer) row[1];
            LbIdAccountId lbIdAccountId = new LbIdAccountId(loadBalancerId, accountId);
            lbIds.add(lbIdAccountId);
        }

        return lbIds;
    }
}
