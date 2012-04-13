package org.openstack.atlas.atom.jobs;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientParamBean;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParamBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParamBean;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.jobs.Job;
import org.openstack.atlas.jobs.LBaaSUsage;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.pojos.AccountLoadBalancer;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.usage.repository.HostUsageRepository;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageRepository;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Required;
import ru.hh.jersey.hchttpclient.ApacheHttpClient;
import ru.hh.jersey.hchttpclient.ApacheHttpClientHandler;

import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

public class AtomHopperUsageJob extends Job implements StatefulJob {
    private final Log LOG = LogFactory.getLog(AtomHopperUsageJob.class);
    private ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter;
    private HostRepository hostRepository;
    private HostUsageRepository hostUsageRepository;
    private LoadBalancerUsageRepository loadBalancerUsageRepository;
    private LoadBalancerRepository loadBalancerRepository;
    public static final int PAGESIZE = 4096;
    public static final int FRAGSIZE = 4;


    @Required
    public void setReverseProxyLoadBalancerAdapter(ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter) {
        this.reverseProxyLoadBalancerAdapter = reverseProxyLoadBalancerAdapter;
    }

    @Required
    public void setLoadBalancerUsageRepository(LoadBalancerUsageRepository loadBalancerUsageRepository) {
        this.loadBalancerUsageRepository = loadBalancerUsageRepository;
    }

    @Required
    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }

    @Required
    public void setHostRepository(HostRepository hostRepository) {
        this.hostRepository = hostRepository;
    }

    @Required
    public void setHostUsageRepository(HostUsageRepository hostUsageRepository) {
        this.hostUsageRepository = hostUsageRepository;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        startPoller();
    }

    private void startPoller() {

        //Update job status..
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Atom hopper usage poller job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));
        jobStateService.updateJobState(JobName.ATOM_USAGE_POLLER, JobStateVal.IN_PROGRESS);

        String[] acceptableTypes = new String[]{"application/xml", "application/json", "text/plain"};


        List<Usage> lbusage = null;

        //Grab desired usage and prep for upload to AHUSL
        List<Integer> accounts = loadBalancerRepository.getAllAccountIds();

        Client client = makeHttpClient();
        for (int id : accounts) {
            List<AccountLoadBalancer> lbsForAccount = loadBalancerRepository.getAccountLoadBalancers(id);
            for (AccountLoadBalancer lb : lbsForAccount) {
                try {
                    lbusage = loadBalancerRepository.getUsageByAccountIdandLbId(id, lb.getLoadBalancerId(), startTime, startTime);
                    int nbytes;

                    for (Usage ur : lbusage) {
                        LBaaSUsage lu = new LBaaSUsage();

                        lu.setMemory(ur.getNumberOfPolls());
                        LOG.info(String.format("Contacting atomHopper service now..."));
                        ClientResponse response = client.resource("http://atom.staging.ord1.us.ci.rackspace.net/lbaas/events").
                                accept(acceptableTypes).
                                header("body", "echo").
                                type("test/plain").
                                post(ClientResponse.class, String.format("Attempt %d", lu));
                        InputStream is = response.getEntityInputStream();
                        StringBuilder sb = new StringBuilder(PAGESIZE);
                        do {
                            byte[] buff = new byte[FRAGSIZE];
                            nbytes = is.read(buff);
                            String frag = new String(buff, "UTF-8");
                            sb.append(frag);
                        } while (nbytes > 0);
                        String body = sb.toString();
                        LOG.info(String.format("Status=%s\n", response.getStatus()));
                        LOG.info(String.format("body %s\n", body));
                        response.close();
                    }

                } catch (Throwable t) {
                    System.out.printf("Exception: %s\n", getExtendedStackTrace(t));
                    LOG.error(String.format("Exception: %s\n", getExtendedStackTrace(t)));
                }

            }
        }
        client.destroy();

        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        jobStateService.updateJobState(JobName.ATOM_USAGE_POLLER, JobStateVal.FINISHED);
        LOG.info(String.format("Atom hopper usage poller job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }

    public static ApacheHttpClient makeHttpClient() {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParamBean connParams = new HttpProtocolParamBean(params);
        connParams.setVersion(HttpVersion.HTTP_1_1);
        connParams.setUseExpectContinue(false);

        ConnManagerParamBean poolParams = new ConnManagerParamBean(params);
        poolParams.setMaxTotalConnections(1);
        poolParams.setTimeout(100);

        ClientParamBean clientParams = new ClientParamBean(params);
        clientParams.setMaxRedirects(10);
        clientParams.setAllowCircularRedirects(true);
        clientParams.setRejectRelativeRedirect(false);
        clientParams.setHandleAuthentication(false);

        // ------------------
        // continue as before

        SchemeRegistry schemata = new SchemeRegistry();
        schemata.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

        ClientConnectionManager connManager = new SingleClientConnManager(params, schemata);
        //ClientConnectionManager connManager = new ThreadSafeClientConnManager(params, schemata);
        HttpClient httpClient = new DefaultHttpClient(connManager, params);

        return new ApacheHttpClient(new ApacheHttpClientHandler(httpClient));
    }

     public static String getExtendedStackTrace(Throwable ti) {
        Throwable t;
        StringBuilder sb;
        Exception currEx;
        String msg;

        sb = new StringBuilder(PAGESIZE);
        t = ti;
        while (t != null) {
            if (t instanceof Exception) {
                currEx = (Exception) t;
                msg = String.format("%s\n", getStackTrace(currEx));
                sb.append(msg);
                t = t.getCause();
            }
        }
        return sb.toString();
    }

    public static String getStackTrace(Exception ex) {
        StringBuilder sb = new StringBuilder(PAGESIZE);
        sb.append(String.format("Exception: %s:%s\n", ex.getMessage(), ex.getClass().getName()));
        for (StackTraceElement se : ex.getStackTrace()) {
            sb.append(String.format("%s\n", se.toString()));
        }
        return sb.toString();
    }
}
