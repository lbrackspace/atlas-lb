package org.openstack.atlas.api.resource;

import org.apache.log4j.Logger;
import org.openstack.atlas.api.resource.provider.CommonDependencyProvider;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.core.api.v1.LoadBalancerUsageRecord;
import org.openstack.atlas.core.api.v1.LoadBalancerUsageRecords;
import org.openstack.atlas.service.domain.entity.UsageRecord;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_ATOM_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

@Controller
@Scope("request")
public class UsageResource extends CommonDependencyProvider {
    protected final Logger LOG = Logger.getLogger(UsageResource.class);
    protected Integer accountId;
    protected Integer loadBalancerId;

    @Autowired
    protected UsageRepository usageRepository;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response get() {
        try {
            List<UsageRecord> usageRecordList = usageRepository.getByLoadBalancerId(loadBalancerId);
            LoadBalancerUsageRecords loadBalancerUsageRecords = new LoadBalancerUsageRecords();

            for (UsageRecord usageRecord : usageRecordList) {
                LoadBalancerUsageRecord loadBalancerUsageRecord = dozerMapper.map(usageRecord, LoadBalancerUsageRecord.class);
                loadBalancerUsageRecords.getLoadBalancerUsageRecords().add(loadBalancerUsageRecord);
            }
            
            return Response.status(Response.Status.OK).entity(loadBalancerUsageRecords).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }
}
