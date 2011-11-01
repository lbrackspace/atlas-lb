package org.openstack.atlas.service.domain.event.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.event.entity.*;
import org.openstack.atlas.service.domain.event.pojo.AccountLoadBalancerServiceEvents;
import org.openstack.atlas.service.domain.event.pojo.LoadBalancerServiceEvents;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojo.DateTimeToolException;
import org.openstack.atlas.service.domain.pojo.DateTimeTools;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

@Repository
@Transactional
public class LoadBalancerEventRepository {

    final Log LOG = LogFactory.getLog(LoadBalancerEventRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;
    private final Integer PAGE_SIZE = 10;

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

    public void save(VirtualIpEvent virtualIpEvent) {
        entityManager.persist(virtualIpEvent);
    }

    public void save(SessionPersistenceEvent sessionPersistenceEvent) {
        entityManager.persist(sessionPersistenceEvent);
    }

    public List<LoadBalancerServiceEvent> getAllEventsForAccount(Integer accountId, Integer page) {
        Query query = entityManager.createQuery("SELECT evt FROM LoadBalancerServiceEvent evt where evt.accountId = :accountId order by evt.created desc").setParameter("accountId", accountId).setMaxResults(PAGE_SIZE);

        if (page != null && page > 0) {
            query = query.setFirstResult((page - 1) * PAGE_SIZE);
        }

        return query.getResultList();
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
        StringBuffer qMid = new StringBuffer();
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

        if(startDate != null) {
            startCal = DateTimeTools.parseDate(startDate);
            DateTimeTools.setCalendarAttrs(startCal,null,null,null,0,0,0); // Early morning
        }else{
            startCal = Calendar.getInstance();
            startCal.add(Calendar.DAY_OF_MONTH,-60);
            DateTimeTools.setCalendarAttrs(startCal,null,null,null,0,0,0); // Early morning
        }

        if(endDate != null) {
            endCal = DateTimeTools.parseDate(endDate);
            DateTimeTools.setCalendarAttrs(endCal,null,null,null,23,59,59); // Midnight;
            qMid.append(" and created <= :endDate ");
        }

        qStr = String.format("%s%s%s", qHead, qMid.toString(), qTail);
        q = entityManager.createQuery(qStr).setParameter("accountId",accountId);

        q.setParameter("startDate",startCal);
        if(endDate != null) {
            q.setParameter("endDate",endCal);
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
        StringBuffer qMid = new StringBuffer();
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

        qHead = "select l from LoadBalancerServiceEvent l "
                + "where  l.accountId = :accountId";


        qTail = " order by created";

        qMid.append(" and created >= :startDate ");

        if(startDate != null) {
            startCal = DateTimeTools.parseDate(startDate);
            DateTimeTools.setCalendarAttrs(startCal,null,null,null,0,0,0); // Early morning
        }else{
            startCal = Calendar.getInstance();
            startCal.add(Calendar.DAY_OF_MONTH,-1);
            DateTimeTools.setCalendarAttrs(startCal,null,null,null,0,0,0); // Early morning
        }

        if(endDate != null) {
            endCal = DateTimeTools.parseDate(endDate);
            DateTimeTools.setCalendarAttrs(endCal,null,null,null,23,59,59); // Midnight;
            qMid.append(" and created <= :endDate ");
        }

        qStr = String.format("%s%s%s", qHead, qMid.toString(), qTail);
        q = entityManager.createQuery(qStr).setParameter("accountId",lb.getAccountId());

        q.setParameter("startDate",startCal);
        if(endDate != null) {
            q.setParameter("endDate",endCal);
        }

        resultList = q.getResultList();
        Integer lbId = null;
        for (LoadBalancerServiceEvent resultEvent : resultList) {
            if (resultEvent.getLoadbalancerId().equals(lb.getId())) {
                lbId = resultEvent.getLoadbalancerId();
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
        }
        lbEvents = new LoadBalancerServiceEvents();
        if (lbid2EventList.containsKey(lbId)) {
            lbEvents.setLoadbalancerId(lbId);
            lbEvents.getLoadBalancerServiceEvents().addAll(lbid2EventList.get(lbId));
            out.getLoadBalancerServiceEvents().add(lbEvents);
        }

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
}
