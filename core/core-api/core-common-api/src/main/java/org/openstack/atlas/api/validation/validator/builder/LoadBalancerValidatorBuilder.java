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
    public LoadBalancerValidatorBuilder(AlgorithmType algorithmType, ProtocolType protocolType, NodeValidatorBuilder nodeValidatorBuilder) {
        super(LoadBalancer.class);
        this.algorithmType = algorithmType;
        this.protocolType = protocolType;

        // SHARED EXPECTATIONS
        result(validationTarget().getProtocol()).if_().exist().then().must().adhereTo(new MustBeInArray(protocolType.toList())).withMessage("Load balancer protocol is invalid. Please specify a valid protocol.");
        result(validationTarget().getAlgorithm()).if_().exist().then().must().adhereTo(new MustBeInArray(algorithmType.toList())).withMessage("Load balancer algorithm is invalid. Please specify a valid algorithm.");

        result(validationTarget().getPort()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(MIN_PORT, MAX_PORT)).withMessage("Load balancer port is invalid. Please specify a valid port.");
        result(validationTarget().getId()).must().not().exist().withMessage("Load balancer id field cannot be modified.");
        result(validationTarget().getStatus()).must().not().exist().withMessage("Load balancer status field cannot be modified.");
        result(validationTarget().getCluster()).must().not().exist().withMessage("Load balancer cluster field cannot be modified.");
        result(validationTarget().getCreated()).must().not().exist().withMessage("Load balancer created field cannot be modified.");
        result(validationTarget().getUpdated()).must().not().exist().withMessage("Load balancer updated field cannot be modified.");
        result(validationTarget().getName()).if_().exist().then().must().adhereTo(new MustHaveLengthVerifier(LB_NAME_LENGTH)).withMessage("Load Balancer name must be less than or equal to " + LB_NAME_LENGTH);

        // POST EXPECTATIONS
        result(validationTarget().getName()).must().exist().forContext(POST).withMessage("Must provide a name for the load balancer.");
        result(validationTarget().getName()).must().not().beEmptyOrNull().forContext(POST).withMessage("Load balancer name is invalid. Please specify a valid name");
        result(validationTarget().getVirtualIps()).must().haveSizeOfAtMost(MAX_VIPS).forContext(POST).withMessage("Must have at most one virtual ip for the load balancer");
        result(validationTarget().getVirtualIps()).if_().exist().then().must().adhereTo(new SharedOrNewVipVerifier()).forContext(POST).withMessage("Must specify either a shared or new virtual ip.");
        result(validationTarget().getVirtualIps()).if_().exist().then().must().delegateTo(new VirtualIpValidator().getValidator(), POST).forContext(POST);
        result(validationTarget().getNodes()).must().exist().forContext(POST).withMessage("Must provide at least one node for the load balancer.");
        result(validationTarget().getNodes()).must().adhereTo(new DuplicateNodeVerifier()).forContext(POST).withMessage("Duplicate nodes detected. Please ensure that the ip address and port are unique for each node.");
        result(validationTarget().getNodes()).must().adhereTo(new ActiveNodeVerifier()).forContext(POST).withMessage("Please ensure that at least one node has an ENABLED condition.");
        result(validationTarget().getNodes()).must().haveSizeOfAtLeast(MIN_NODES).forContext(POST).withMessage("Must have at least one node.");
        result(validationTarget().getNodes()).must().haveSizeOfAtMost(MAX_NODES).forContext(POST).withMessage("Must not provide more than twenty five nodes per load balancer.");
        result(validationTarget().getNodes()).if_().exist().then().must().delegateTo(new NodeValidator(nodeValidatorBuilder).getValidator(), POST).forContext(POST);
        result(validationTarget().getHealthMonitor()).if_().exist().then().must().delegateTo(new HealthMonitorValidator().getValidator(), POST).forContext(POST);
        result(validationTarget().getConnectionThrottle()).if_().exist().then().must().delegateTo(new ConnectionThrottleValidator().getValidator(), POST).forContext(POST);


        // PUT EXPECTATIONS
        must().adhereTo(new Verifier<LoadBalancer>() {
            @Override
            public VerifierResult verify(LoadBalancer obj) {
                return new VerifierResult(obj.getName() != null || obj.getAlgorithm() != null || obj.getPort() != null || obj.getProtocol() != null);
            }
        }).forContext(PUT).withMessage("The load balancer must have at least one of the following to update: name, algorithm, protocol, port.");
        result(validationTarget().getNodes()).must().beEmptyOrNull().forContext(PUT).withMessage("Please visit {account id}/loadbalancers/{load balancer id}/nodes to configure nodes.");
        result(validationTarget().getVirtualIps()).must().beEmptyOrNull().forContext(PUT).withMessage("Please visit {account id}/loadbalancers/{load balancer id}/virtualips/{virtual ip id} to configure a virtual ip.");
        result(validationTarget().getHealthMonitor()).must().not().exist().forContext(PUT).withMessage("Please visit {account id}/loadbalancers/{load balancer id}/healthmonitor to configure your health monitor.");
        result(validationTarget().getConnectionThrottle()).must().not().exist().forContext(PUT).withMessage("Please visit {account id}/loadbalancers/{load balancer id}/throttles to configure connection throttling.");
        result(validationTarget().getConnectionLogging()).must().not().exist().forContext(PUT).withMessage("Please visit {account id}/loadbalancers/{load balancer id}/connectionLogging to configure connection throttling.");
        result(validationTarget().getSessionPersistence()).must().not().exist().forContext(PUT).withMessage("Please visit {account id}/loadbalancers/{load balancer id}/sessionPersistence to configure connection throttling.");
    }
}
