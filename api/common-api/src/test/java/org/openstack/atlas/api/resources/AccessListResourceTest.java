package org.openstack.atlas.api.resources;

import org.openstack.atlas.docs.loadbalancers.api.v1.AccessList;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItem;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItemType;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.DeletedStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.api.integration.AsyncService;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class AccessListResourceTest {

}
