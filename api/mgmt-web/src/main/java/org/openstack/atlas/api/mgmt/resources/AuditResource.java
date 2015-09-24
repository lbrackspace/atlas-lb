package org.openstack.atlas.api.mgmt.resources;

import java.util.Collections;
import org.openstack.atlas.api.mgmt.helpers.SslTermInfoComparator;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.CertInfo;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.SslTermInfo;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import org.joda.time.DateTime;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.SslTermInfos;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.service.domain.services.helpers.RdnsHelper;
import org.openstack.atlas.util.config.MossoConfigValues;
import org.openstack.atlas.cfg.ConfigurationKey;
import org.openstack.atlas.osgi.cfg.commons.ApacheCommonsConfiguration;
import org.openstack.atlas.util.config.LbConfiguration;
import org.openstack.atlas.util.debug.Debug;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Alerts;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerAudit;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerAudits;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ListOfStrings;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.util.b64aes.Aes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.openstack.atlas.api.mgmt.helpers.CertInfoUtils;
import org.openstack.atlas.service.domain.pojos.SslTermInfoDb;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;


import static org.openstack.atlas.util.converters.DateTimeConverters.isoTocal;
import static javax.ws.rs.core.MediaType.APPLICATION_ATOM_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

public class AuditResource extends ManagementDependencyProvider {

    private static final SslTermInfoComparator sslTermCmp;
    private static Log LOG = LogFactory.getLog(AuditResource.class.getName());
    private static final ZeusUtils zu;

    static {
        zu = new ZeusUtils();
        sslTermCmp = new SslTermInfoComparator();
    }

    @GET
    @Path("lbconfig")
    @Produces({APPLICATION_JSON})
    public Response retrieveLbConfig() {
        LbConfiguration conf = new LbConfiguration();
        MossoConfigValues[] keys = MossoConfigValues.values();
        if (!isUserInRole("ops")) {
            return ResponseFactory.accessDenied();
        }
        String jsonResponse = retrieveConfig(conf, keys);
        return Response.status(200).entity(jsonResponse).build();
    }

    @GET
    @Path("restconfig")
    @Produces({APPLICATION_JSON})
    public Response retrieveRestConfig() {
        RestApiConfiguration conf = new RestApiConfiguration();
        PublicApiServiceConfigurationKeys[] keys = PublicApiServiceConfigurationKeys.values();
        if (!isUserInRole("ops")) {
            return ResponseFactory.accessDenied();
        }
        String jsonResponse = retrieveConfig(conf, keys);
        return Response.status(200).entity(jsonResponse).build();
    }

    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveLoadBalancersByStatus(@QueryParam("status") String status, @QueryParam("changes-since") String changedSince) {
        Calendar changedCal = null;
        try {
            if (changedSince != null) {
                changedCal = isoTocal(changedSince);
            }

            LoadBalancerAudits loadBalancerAudits = new LoadBalancerAudits();
            List<LoadBalancerAudit> loadBalancerAuditList = new ArrayList<LoadBalancerAudit>();
            LoadBalancerAudit loadBalancerAudit;
            //TODO: dozer map it...
            if (status != null) {
                List<LoadBalancer> loadBalancers = loadBalancerService.getLoadBalancersForAudit(status, changedCal);
                for (LoadBalancer lb : loadBalancers) {
                    loadBalancerAudit = new LoadBalancerAudit();
                    org.openstack.atlas.docs.loadbalancers.api.management.v1.Alerts alerts = null;
                    for (Alert domainCl : alertService.getByLoadBalancerId(lb.getId())) {
                        alerts = new org.openstack.atlas.docs.loadbalancers.api.management.v1.Alerts();
                        alerts.getAlerts().add(getDozerMapper().map(domainCl, org.openstack.atlas.docs.loadbalancers.api.management.v1.Alert.class, "SIMPLE_ALERT"));
                    }
                    loadBalancerAudit.setId(lb.getId());
                    loadBalancerAudit.setCreated(lb.getCreated());
                    loadBalancerAudit.setUpdated(lb.getUpdated());
                    loadBalancerAudit.setStatus(lb.getStatus().toString());
                    if (alerts == null) {
                        alerts = new Alerts();
                    }
                    loadBalancerAudit.getAlertAudits().add(alerts);
                    loadBalancerAudits.getLoadBalancerAudits().add(loadBalancerAudit);
                }
                return Response.status(200).entity(loadBalancerAudits).build();
            } else {
                BadRequestException badRequestException = new BadRequestException("Must supply a status to query against.");
                return ResponseFactory.getErrorResponse(badRequestException, null, null);
            }
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    private String retrieveConfig(ApacheCommonsConfiguration conf, ConfigurationKey[] keys) {
        int i;
        String resp = "{";
        for (i = 0; i < keys.length; i++) {
            ConfigurationKey key = keys[i];
            String keyStr = key.toString();
            String valueStr;
            try {
                valueStr = conf.getString(key);
            } catch (Exception ex) {
                valueStr = "????";
            }
            if (!keyStr.contains("auth") && !keyStr.contains("pass")) {
                resp += String.format("\n\t\"%s\": \"%s\",", keyStr, valueStr);
            }
        }
        if (resp.endsWith(",")) {
            resp = resp.substring(0, resp.length() - 1);
        }
        resp += "\n}";
        return resp;
    }

    @GET
    @Path("sslexpireaudit")
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Response runExpiredCertCheck() {
        DateTime now = StaticDateTimeUtils.nowDateTime(true);
        SslTermInfos sslTermElement = new SslTermInfos();
        List<SslTermInfoDb> sslTermsFromDb = loadBalancerRepository.getSslTermInfo();
        sslTermElement.setReportDate(StaticDateTimeUtils.toCal(now));
        for (SslTermInfoDb dbSslTermInfo : sslTermsFromDb) {
            SslTermInfo apiTermInfo = new SslTermInfo();
            apiTermInfo.setLoadbalancerId(dbSslTermInfo.getAccountId());
            apiTermInfo.setLoadbalancerId(dbSslTermInfo.getLoadbalancerId());
            apiTermInfo.setId(dbSslTermInfo.getSslId());
            String key = dbSslTermInfo.getPrivatekey();
            String crt = dbSslTermInfo.getCertificate();
            String imds = dbSslTermInfo.getCertificate();
            ZeusCrtFile zcf = zu.buildZeusCrtFileLbassValidation(key, crt, imds);
            CertInfo certInfo = CertInfoUtils.parseCertInfo(crt);
            apiTermInfo.setCertificate(certInfo);
            apiTermInfo.setIssuer(certInfo.getIssuerName());
            apiTermInfo.setSubject(certInfo.getSubjectName());
            apiTermInfo.setNotBefore(certInfo.getNotBefore());
            apiTermInfo.setNotAfter(certInfo.getNotAfter());
            apiTermInfo.setExpiresInDays(certInfo.getDaysTillExpires());
            if (zcf.hasFatalErrors()) {
                apiTermInfo.setApiValid(Boolean.FALSE);
            } else {
                apiTermInfo.setApiValid(Boolean.TRUE);
            }
            for (String imd : CertInfoUtils.splitImds(imds)) {
                apiTermInfo.getIntermediates().add(CertInfoUtils.parseCertInfo(imd));
            }
            sslTermElement.getSslTerms().add(apiTermInfo);
        }
        Collections.sort(sslTermElement.getSslTerms(), sslTermCmp);
        return Response.status(200).entity(sslTermElement).build();
    }
}
