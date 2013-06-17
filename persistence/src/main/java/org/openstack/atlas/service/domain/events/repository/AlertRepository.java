package org.openstack.atlas.service.domain.events.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.events.entities.AlertStatus;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.CustomQuery;
import org.openstack.atlas.service.domain.pojos.QueryParameter;
import org.openstack.atlas.util.common.exceptions.ConverterException;
import org.openstack.atlas.service.domain.util.Constants;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.openstack.atlas.util.converters.DateTimeConverters.isoTocal;
import static org.openstack.atlas.util.converters.PrimitiveConverters.integerList2cdString;

@Repository
@Transactional
public class AlertRepository {

    final Log LOG = LogFactory.getLog(AlertRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    public AlertRepository() {
    }

    public AlertRepository(EntityManager em) {
        this.entityManager = em;
    }

    public void save(Alert alert) {
        Calendar now = Calendar.getInstance();
        alert.setCreated(now);
        entityManager.persist(alert);
    }

    public Alert getById(Integer id) throws EntityNotFoundException {
        Alert cl = entityManager.find(Alert.class, id);
        if (cl == null) {
            String errMsg = String.format("Cannot access alert {id=%d}", id);
            LOG.warn(errMsg);
            throw new EntityNotFoundException(errMsg);
        }
        return cl;
    }

    public List<Alert> getByLoadBalancersByIds(List<Integer> loadBalancerIds, String startDate, String endDate) throws BadRequestException {
        List<Alert> alerts;
        CustomQuery cq;
        String intsAsString;
        String qStr;
        String qformat;
        Calendar startCal;
        Calendar endCal;
        Query q;
        try {
            intsAsString = integerList2cdString(loadBalancerIds);
        } catch (ConverterException ex) {
            Logger.getLogger(AlertRepository.class.getName()).log(Level.SEVERE, null, ex);
            throw new BadRequestException("loadBalancerIds can not be Null");
        }
        qformat = "SELECT al From Alert al where al.loadbalancerId "
                + "IN (%s)";

        qStr = String.format(qformat, intsAsString);
        cq = new CustomQuery(qStr);
        cq.setWherePrefix(""); // The where prefix was included above
        if (startDate != null) {
            try {
                startCal = isoTocal(startDate);
            } catch (ConverterException ex) {
                Logger.getLogger(AlertRepository.class.getName()).log(Level.SEVERE, null, ex);
                throw new BadRequestException("Invalid startDate", ex);
            }
            cq.addParam("al.created", ">=", "startDate", startCal);
        }

        if (endDate != null) {
            try {
                endCal = isoTocal(endDate);
            } catch (ConverterException ex) {
                Logger.getLogger(AlertRepository.class.getName()).log(Level.SEVERE, null, ex);
                throw new BadRequestException("Invalid endDate", ex);
            }
            cq.addParam("al.created", "<=", "endDate", endCal);
        }
        if (cq.getQueryParameters().size() > 0) {
            cq.setWherePrefix(" and ");
        }
        q = entityManager.createQuery(cq.getQueryString());
        for (QueryParameter qp : cq.getQueryParameters()) {
            String pname = qp.getPname();
            Object val = qp.getValue();
            q.setParameter(pname, val);
        }
        return q.getResultList();
    }

    public List<Alert> getByAccountId(Integer accountId, String startDate, String endDate) throws BadRequestException {
        List<Alert> alerts;
        CustomQuery cq;
        String intsAsString;
        String qStr;
        String qformat;
        Calendar startCal;
        Calendar endCal;
        Query q;


        cq = new CustomQuery("from Alert a");
        cq.addParam("a.accountId", "=", "aid", accountId);
        if (startDate != null) {
            try {
                startCal = isoTocal(startDate);
            } catch (ConverterException ex) {
                Logger.getLogger(AlertRepository.class.getName()).log(Level.SEVERE, null, ex);
                throw new BadRequestException("Invalid startDate", ex);
            }
            cq.addParam("a.created", ">=", "startDate", startCal);
        }

        if (endDate != null) {
            try {
                endCal = isoTocal(endDate);
            } catch (ConverterException ex) {
                Logger.getLogger(AlertRepository.class.getName()).log(Level.SEVERE, null, ex);
                throw new BadRequestException("Invalid endDate", ex);
            }
            cq.addParam("a.created", "<=", "endDate", endCal);
        }

        q = entityManager.createQuery(cq.getQueryString());
        for (QueryParameter qp : cq.getQueryParameters()) {
            String pname = qp.getPname();
            Object val = qp.getValue();
            q.setParameter(pname, val);
        }

        for (QueryParameter qp : cq.getUnquotedParameters()) {
            String pname = qp.getPname();
            Object val = qp.getValue();
            q.setParameter(pname, val);
        }
        return q.getResultList();
    }

    public List<Alert> getByClusterId(Integer clusterId, String startDate, String endDate) throws BadRequestException {
        List<Object> objects;
        List<Alert> alerts = new ArrayList<Alert>();
        CustomQuery customQuery;
        String queryString;
        Calendar startCal;
        Calendar endCal;
        Query query;

        queryString = "SELECT a.* FROM alert a INNER JOIN loadbalancer lb ON a.loadbalancer_id = lb.id INNER JOIN host h ON";
        queryString += " lb.host_id = h.id WHERE h.cluster_id = :cid";

        customQuery = new CustomQuery(queryString);
        customQuery.addUnquotedParam("cid", clusterId);
        customQuery.setWherePrefix(""); // The where prefix was included above
        if (startDate != null) {
            try {
                startCal = isoTocal(startDate);
            } catch (ConverterException ex) {
                Logger.getLogger(AlertRepository.class.getName()).log(Level.SEVERE, null, ex);
                throw new BadRequestException("Invalid startDate", ex);
            }
            customQuery.addParam("a.created", ">=", "startDate", startCal);
        }

        if (endDate != null) {
            try {
                endCal = isoTocal(endDate);
            } catch (ConverterException ex) {
                Logger.getLogger(AlertRepository.class.getName()).log(Level.SEVERE, null, ex);
                throw new BadRequestException("Invalid endDate", ex);
            }
            customQuery.addParam("a.created", "<=", "endDate", endCal);
        }
        if (customQuery.getQueryParameters().size() > 0) {
            customQuery.setWherePrefix(" AND ");
        }
        query = entityManager.createNativeQuery(customQuery.getQueryString());
        for (QueryParameter qp : customQuery.getQueryParameters()) {
            String pname = qp.getPname();
            Object val = qp.getValue();
            query.setParameter(pname, val);
        }

        for (QueryParameter qp : customQuery.getUnquotedParameters()) {
            String pname = qp.getPname();
            Object val = qp.getValue();
            query.setParameter(pname, val);
        }

        try {
            objects = query.getResultList();

            for (Object object : objects) {
                Object[] columns = (Object[]) object;

                Alert alert = new Alert();
                alert.setId((Integer) columns[0]);
                alert.setAccountId((Integer) columns[1]);
                alert.setLoadbalancerId((Integer)columns[2]);
                alert.setAlertType((String)columns[3]);
                alert.setMessage((String)columns[4]);
                alert.setMessageName((String)columns[5]);

                String status = (String)columns[6];
                if (status.compareTo(AlertStatus.ACKNOWLEDGED.toString()) == 0) {
                    alert.setStatus(AlertStatus.ACKNOWLEDGED);
                } else {
                    alert.setStatus(AlertStatus.UNACKNOWLEDGED);
                }

                Date date = (Date) columns[7];
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                alert.setCreated(cal);
                alerts.add(alert);
            }
        } catch(Exception e) {
            Logger.getLogger(AlertRepository.class.getName()).log(Level.SEVERE, null, e);
            throw new BadRequestException("Object mapping failure.", e);
        }

        return alerts;
    }

    public Alert update(Alert alert) {
        LOG.info("Updating Alert " + alert.getId() + "...");
        alert = entityManager.merge(alert);
        entityManager.flush();
        return alert;
    }

    public List<Alert> getForAccount() {
        return entityManager.createQuery("SELECT ht FROM Alert ht where ht.accountId is not null and ht.loadbalancerId is not null").getResultList();
    }

    public List<Alert> getForLoadBalancer(Integer loadbalancerId) {
        String queryStr = "SELECT ht FROM Alert ht where  ht.loadbalancerId = :loadbalancerId";
        List<Alert> reslts = entityManager.createQuery(queryStr).setParameter("loadbalancerId", loadbalancerId).getResultList();
        return reslts;

    }

    public List<Alert> getByAccountId(Integer marker, Integer limit, Integer accountId, String startDate, String endDate) throws BadRequestException {
        Calendar endCal, startCal;

        if (marker == null) {
            marker = 0;
        }

        if (limit == null) {
            limit = 10;
        }

        if (endDate == null) {
            endCal = Calendar.getInstance();
        } else {
            try {
                endCal = isoTocal(endDate);
            } catch (ConverterException ex) {
                Logger.getLogger(AlertRepository.class.getName()).log(Level.SEVERE, null, ex);
                throw new BadRequestException("Invalid endDate", ex);
            }
        }

        if (startDate == null) {
            startCal = new GregorianCalendar(2000, Calendar.OCTOBER, 10);
        } else {
            try {
                startCal = isoTocal(startDate);
            } catch (ConverterException ex) {
                Logger.getLogger(AlertRepository.class.getName()).log(Level.SEVERE, null, ex);
                throw new BadRequestException("Invalid startDate", ex);
            }
        }

        String queryStr = "SELECT ht FROM Alert ht WHERE ht.accountId = :accountId AND ht.created <= :endDate AND ht.created >= :startDate";

        List<Alert> results = entityManager.createQuery(queryStr).setFirstResult(marker).setMaxResults(limit).setParameter("accountId", accountId).setParameter("startDate", startCal).setParameter("endDate", endCal).getResultList();

        return results;
    }

    public List<Alert> getAll(String status, Integer... p) {
        List<Alert> alts = new ArrayList<Alert>();

        AlertStatus lbStatus = null;
        if (status != null) {
            try {
                lbStatus = AlertStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                lbStatus = null;
            }
        }

        Query query;
        query = entityManager.createQuery("SELECT h FROM Alert h");

        if (lbStatus != null) {
            query = entityManager.createQuery("SELECT h FROM Alert h WHERE h.status = :status").setParameter("status", lbStatus);
        }

        if (p.length >= 2) {
            Integer marker = p[0];
            Integer limit = p[1];
            if (limit == null || limit > 100) {
                limit = 100;
            }
            if (marker == null) {
                marker = 0;
            }
            query = query.setFirstResult(marker).setMaxResults(limit);
        }
        alts = query.getResultList();
        return alts;
    }

    public List<Alert> getAllUnacknowledged(Integer... p) {
        List<Alert> alts = new ArrayList<Alert>();
        Query query = entityManager.createQuery("SELECT h FROM Alert h where h.status = 'UNACKNOWLEDGED'");
        if (p.length >= 2) {
            Integer marker = p[0];
            Integer limit = p[1];
            if (limit == null || limit > 100) {
                limit = 100;
            }
            if (marker == null) {
                marker = 0;
            }
            query = query.setFirstResult(marker).setMaxResults(limit);
        }
        alts = query.getResultList();
        return alts;
    }

    public List<Alert> getAllUnacknowledgedByName(String type, String name, Integer... p) {
        List<Alert> alts = new ArrayList<Alert>();

        //quick...
        Query query;
        if (name == null) {
            query = entityManager.createQuery("SELECT h FROM Alert h where h.status = 'UNACKNOWLEDGED' and h.alertType = :type");
            query.setParameter("type", type);
        } else  {
            query = entityManager.createQuery("SELECT h FROM Alert h where h.status = 'UNACKNOWLEDGED' and h.messageName = :name and h.alertType = :type");
            query.setParameter("type", name).setParameter("type", type);
        }
        if (p.length >= 2) {
            Integer marker = p[0];
            Integer limit = p[1];
            if (limit == null || limit > 100) {
                limit = 100;
            }
            if (marker == null) {
                marker = 0;
            }
            query = query.setFirstResult(marker).setMaxResults(limit);
        }
        alts = query.getResultList();
        return alts;
    }

    public List<Alert> getAtomHopperByLoadBalancersByIds(List<Integer> loadBalancerIds, String startDate, String endDate, String queryName) throws BadRequestException {
        List<Alert> alerts;
        CustomQuery cq;
        String intsAsString;
        String qStr;
        String qformat;
        Calendar startCal;
        Calendar endCal;
        Query q;
        try {
            intsAsString = integerList2cdString(loadBalancerIds);
        } catch (ConverterException ex) {
            Logger.getLogger(AlertRepository.class.getName()).log(Level.SEVERE, null, ex);
            throw new BadRequestException("loadBalancerIds can not be Null");
        }
        qformat = "SELECT al From Alert al where al.loadbalancerId "
                + "IN (%s)";

        qStr = String.format(qformat, intsAsString);
        cq = new CustomQuery(qStr);
        cq.setWherePrefix(""); // The where prefix was included above
        if (startDate != null) {
            try {
                startCal = isoTocal(startDate);
            } catch (ConverterException ex) {
                Logger.getLogger(AlertRepository.class.getName()).log(Level.SEVERE, null, ex);
                throw new BadRequestException("Invalid startDate", ex);
            }
            cq.addParam("al.created", ">=", "startDate", startCal);
        }

        if (endDate != null) {
            try {
                endCal = isoTocal(endDate);
            } catch (ConverterException ex) {
                Logger.getLogger(AlertRepository.class.getName()).log(Level.SEVERE, null, ex);
                throw new BadRequestException("Invalid endDate", ex);
            }
            cq.addParam("al.created", "<=", "endDate", endCal);
        }
        cq.addParam("al.alertType", "=", "queryName", queryName);
        if (cq.getQueryParameters().size() > 0) {
            cq.setWherePrefix(" and ");
        }
        q = entityManager.createQuery(cq.getQueryString());
        for (QueryParameter qp : cq.getQueryParameters()) {
            String pname = qp.getPname();
            Object val = qp.getValue();
            q.setParameter(pname, val);
        }
        return q.getResultList();
    }


    public void removeAlertEntries() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -90);
        entityManager.createQuery("DELETE FROM Alert a where a.created <= :days and a.status ='ACKNOWLEDGED'").setParameter("days", cal).executeUpdate();
    }
}
