package org.openstack.atlas.api.validation.validator.builder;

import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.validator.*;
import org.openstack.atlas.api.validation.verifier.*;
import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.openstack.atlas.datamodel.AlgorithmType;
import org.openstack.atlas.datamodel.ProtocolType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

@Component
@Scope("request")
public class LoadBalancerValidatorBuilder extends ValidatorBuilder<LoadBalancer> {
    protected final int MIN_PORT = 1;
    protected final int MAX_PORT = 65535;
    protected final int LB_NAME_LENGTH = 128;
    protected final int MIN_NODES = 1;
    protected final int MAX_NODES = 25;
    protected final int MAX_VIPS = 1;
    protected AlgorithmType algorithmType;
    protected ProtocolType protocolType;

    @Autowired
    public LoadBalancerValidatorBuilder(AlgorithmType algorithmType,
                                        ProtocolType protocolType,
                                        NodeValidatorBuilder nodeValidatorBuilder,
                                        VirtualIpValidatorBuilder virtualIpValidatorBuilder,
                                        HealthMonitorValidatorBuilder healthMonitorValidatorBuilder,
                                        ConnectionThrottleValidatorBuilder connectionThrottleValidatorBuilder,
                                        SessionPersistenceValidatorBuilder sessionPersistenceValidatorBuilder) {
        super(LoadBalancer.class);
        this.algorithmType = algorithmType;
        this.protocolType = protocolType;

        // SHARED EXPECTATIONS
        result(validationTarget().getId()).must().not().exist().withMessage("Load balancer id field cannot be modified.");
        result(validationTarget().getName()).if_().exist().then().must().adhereTo(new MustHaveLengthVerifier(LB_NAME_LENGTH)).withMessage("Load Balancer name must be less than or equal to " + LB_NAME_LENGTH);
        result(validationTarget().getAlgorithm()).if_().exist().then().must().adhereTo(new MustBeInArray(algorithmType.toList())).withMessage("Load balancer algorithm is invalid. Please specify a valid algorithm.");
        result(validationTarget().getStatus()).must().not().exist().withMessage("Load balancer status field cannot be modified.");
        result(validationTarget().getCreated()).must().not().exist().withMessage("Load balancer created field cannot be modified.");
        result(validationTarget().getUpdated()).must().not().exist().withMessage("Load balancer updated field cannot be modified.");
        
        // POST EXPECTATIONS
        result(validationTarget().getName()).must().exist().forContext(POST).withMessage("Must provide a name for the load balancer.");
        result(validationTarget().getName()).must().not().beEmptyOrNull().forContext(POST).withMessage("Load balancer name is invalid. Please specify a valid name");
        result(validationTarget().getProtocol()).if_().exist().then().must().adhereTo(new MustBeInArray(protocolType.toList())).forContext(POST).withMessage("Load balancer protocol is invalid. Please specify a valid protocol.");
        result(validationTarget().getPort()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(MIN_PORT, MAX_PORT)).forContext(POST).withMessage("Load balancer port is invalid. Please specify a valid port.");
        result(validationTarget().getVirtualIps()).must().haveSizeOfAtMost(MAX_VIPS).forContext(POST).withMessage(String.format("Must have at most %d virtual ip for the load balancer", MAX_VIPS));
        result(validationTarget().getVirtualIps()).if_().exist().then().must().adhereTo(new SharedOrNewVipVerifier()).forContext(POST).withMessage("Must specify either a shared or new virtual ip.");
        result(validationTarget().getVirtualIps()).if_().exist().then().must().delegateTo(new VirtualIpValidator(virtualIpValidatorBuilder).getValidator(), POST).forContext(POST);
        result(validationTarget().getNodes()).must().exist().forContext(POST).withMessage("Must provide at least 1 node for the load balancer.");
        result(validationTarget().getNodes()).must().adhereTo(new DuplicateNodeVerifier()).forContext(POST).withMessage("Duplicate nodes detected. Please ensure that the ip address and port are unique for each node.");
        result(validationTarget().getNodes()).must().adhereTo(new ActiveNodeVerifier()).forContext(POST).withMessage("Please ensure that at least 1 node has an ENABLED condition.");
        result(validationTarget().getNodes()).must().haveSizeOfAtLeast(MIN_NODES).forContext(POST).withMessage(String.format("Must have at least %d node(s).", MIN_NODES));
        result(validationTarget().getNodes()).must().haveSizeOfAtMost(MAX_NODES).forContext(POST).withMessage(String.format("Must not provide more than %d nodes per load balancer.", MAX_NODES));
        result(validationTarget().getNodes()).if_().exist().then().must().delegateTo(new NodeValidator(nodeValidatorBuilder).getValidator(), POST).forContext(POST);
        result(validationTarget().getHealthMonitor()).if_().exist().then().must().delegateTo(new HealthMonitorValidator(healthMonitorValidatorBuilder).getValidator(), PUT).forContext(POST);
        result(validationTarget().getConnectionThrottle()).if_().exist().then().must().delegateTo(new ConnectionThrottleValidator(connectionThrottleValidatorBuilder).getValidator(), PUT).forContext(POST);
        result(validationTarget().getSessionPersistence()).if_().exist().then().must().delegateTo(new SessionPersistenceValidator(sessionPersistenceValidatorBuilder).getValidator(), PUT).forContext(POST);

        // PUT EXPECTATIONS
        result(validationTarget().getProtocol()).must().not().exist().forContext(PUT).withMessage("Load balancer protocol field cannot be modified.");
        result(validationTarget().getPort()).must().not().exist().forContext(PUT).withMessage("Load balancer port field cannot be modified.");
        must().adhereTo(new Verifier<LoadBalancer>() {
            @Override
            public VerifierResult verify(LoadBalancer obj) {
                return new VerifierResult(obj.getName() != null || obj.getAlgorithm() != null || !obj.getOtherAttributes().isEmpty());
            }
        }).forContext(PUT).withMessage("The load balancer must have at least one attribute to update.");
        result(validationTarget().getNodes()).must().beEmptyOrNull().forContext(PUT).withMessage("Please visit {account id}/loadbalancers/{load balancer id}/nodes to configure nodes.");
        result(validationTarget().getVirtualIps()).must().beEmptyOrNull().forContext(PUT).withMessage("Please visit {account id}/loadbalancers/{load balancer id}/virtualips/{virtual ip id} to configure a virtual ip.");
        result(validationTarget().getHealthMonitor()).must().not().exist().forContext(PUT).withMessage("Please visit {account id}/loadbalancers/{load balancer id}/healthmonitor to configure your health monitor.");
        result(validationTarget().getConnectionThrottle()).must().not().exist().forContext(PUT).withMessage("Please visit {account id}/loadbalancers/{load balancer id}/throttles to configure connection throttling.");
        result(validationTarget().getSessionPersistence()).must().not().exist().forContext(PUT).withMessage("Please visit {account id}/loadbalancers/{load balancer id}/sessionPersistence to configure connection throttling.");
    }
}
