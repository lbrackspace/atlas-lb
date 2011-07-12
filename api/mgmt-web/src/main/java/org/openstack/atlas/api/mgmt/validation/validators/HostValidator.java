package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Host;
import org.openstack.atlas.service.domain.entities.HostStatus;
import org.openstack.atlas.api.mgmt.validation.contexts.ReassignHostContext;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.verifiers.MustBeInArray;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;

import static org.openstack.atlas.api.mgmt.validation.contexts.HostContext.*;
import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class HostValidator implements ResourceValidator<Host> {

    private final Validator<Host> validator;

    public HostValidator() {

        validator = build(new ValidatorBuilder<Host>(Host.class) {

            {
                result(validationTarget().getId()).must().not().exist().forContext(POST, PUT).withMessage("Host id can not be modified.");
                result(validationTarget().getIpv4Public()).if_().exist().then().must().beValidIpv4Address().forContext(POST, PUT).withMessage("ipv4Public was not a valid IPv4 address");
                result(validationTarget().getIpv4Servicenet()).if_().exist().then().must().beValidIpv4Address().forContext(POST, PUT).withMessage("ipv4Servicenet was not a valid IPv4 address");
                result(validationTarget().getIpv6Public()).if_().exist().then().must().beValidIpv6Address().forContext(POST, PUT).withMessage("ipv6Public was not a valid IPv6 address");
                result(validationTarget().getIpv6Servicenet()).if_().exist().then().must().beValidIpv6Address().forContext(POST, PUT).withMessage("ipv6Servicenet was not a valid IPv6 address");

                result(validationTarget().getZone()).must().not().exist().forContext(PUT).withMessage("Must not provide a zone.");
                result(validationTarget().getClusterId()).must().not().exist().forContext(PUT).withMessage("Must not provide a cluster id.");
                result(validationTarget().getNumberOfLoadBalancingConfigurations()).must().not().exist().forContext(PUT).withMessage("Must not specify the number of load balancing configurations.");
                result(validationTarget().getNumberOfUniqueCustomers()).must().not().exist().forContext(PUT).withMessage("Must not specify the number of unique customers.");
                result(validationTarget().getUtilization()).must().not().exist().forContext(PUT).withMessage("Must not specify the utilization.");
                result(validationTarget().getType()).must().not().exist().forContext(PUT).withMessage("Must not specify the type.");

                result(validationTarget().getId()).must().exist().forContext(LOADBALANCER_PUT).withMessage("Host Id must be present on Loadbalancer Host Put method");
                result(validationTarget().getName()).must().exist().forContext(POST).withMessage("Name must be present for host");
                result(validationTarget().getClusterId()).must().exist().forContext(POST).withMessage("ClusterId must be present for host");
                result(validationTarget().getCoreDeviceId()).must().exist().forContext(POST).withMessage("CoreDeviceId must be preset for host");
                result(validationTarget().getStatus()).if_().exist().then().must().adhereTo(new MustBeInArray(HostStatus.values())).forContext(POST).withMessage("Status must be a valid host status.");
                result(validationTarget().getStatus()).must().not().exist().forContext(POST).withMessage("Status must not be present for host");
                result(validationTarget().getZone()).must().exist().forContext(POST).withMessage("Zone must be present for host");
                result(validationTarget().getMaxConcurrentConnections()).must().exist().forContext(POST).withMessage("MaxConcurrentConnections must be preset for host");
                result(validationTarget().getManagementIp()).must().exist().forContext(POST).withMessage("ManagementIpAddress must be preset for host");
                result(validationTarget().getManagementSoapInterface()).must().exist().forContext(POST).withMessage("ManagementSoapInterface must be preset for host");
                result(validationTarget().getManagementIp()).must().exist().forContext(POST).withMessage("ManagementIp must be preset for host");
                result(validationTarget().getTrafficManagerName()).must().exist().forContext(POST).withMessage("TrafficManagerName must be present for host");
                result(validationTarget().getIpv4Public()).must().exist().forContext(POST).withMessage("ipv4Public must be present in host"); // JIRA:882
                result(validationTarget().getIpv4Servicenet()).must().exist().forContext(POST).withMessage("ipv4Servicenet must be present in host"); // JIRA:882
                // result(validationTarget().getIpv6Public()).must().exist().forContext(POST).withMessage("ipv6Public was must be present in host");
                // result(validationTarget().getIpv6Servicenet()).must().exist().forContext(POST).withMessage("ipv6Servicenet must be present in host");
                result(validationTarget().isSoapEndpointActive()).must().exist().forContext(POST).withMessage("SoapEndPointActive must be preset for host");
                result(validationTarget().isSoapEndpointActive()).if_().exist().then().must().adhereTo(new Verifier<Boolean>() {

                    @Override
                    public VerifierResult verify(Boolean isSoapEndpointActive) {
                        return new VerifierResult(isSoapEndpointActive != null && (isSoapEndpointActive || !isSoapEndpointActive));
                    }
                }).withMessage("Soap end point active must be of boolean value");
                result(validationTarget().getClusterId()).must().not().exist().forContext(PUT).withMessage("Cluster Id is not mutable");
                //REASSIGN_HOST context
                result(validationTarget().getId()).must().exist().forContext(ReassignHostContext.REASSIGN_HOST).withMessage("ID must be present for host.");
                result(validationTarget().getName()).must().not().exist().forContext(ReassignHostContext.REASSIGN_HOST).withMessage("Must not provide name for this request.");
                result(validationTarget().getClusterId()).must().not().exist().forContext(ReassignHostContext.REASSIGN_HOST).withMessage("Must not provide cluster id for this request.");
                result(validationTarget().getCoreDeviceId()).must().not().exist().forContext(ReassignHostContext.REASSIGN_HOST).withMessage("Must not provide core device id for this request.");
                result(validationTarget().getStatus()).must().not().exist().forContext(ReassignHostContext.REASSIGN_HOST).withMessage("Must not provide status for this request.");
                result(validationTarget().getZone()).must().not().exist().forContext(ReassignHostContext.REASSIGN_HOST).withMessage("Must not provide zone for this request.");
                result(validationTarget().getMaxConcurrentConnections()).must().not().exist().forContext(ReassignHostContext.REASSIGN_HOST).withMessage("Must not provide max concurrent connections for this request.");
                result(validationTarget().getManagementIp()).must().not().exist().forContext(ReassignHostContext.REASSIGN_HOST).withMessage("Must not provide management ip for this request.");
                result(validationTarget().getManagementSoapInterface()).must().not().exist().forContext(ReassignHostContext.REASSIGN_HOST).withMessage("Must not provide management soap interface for this request.");


            }
        });
    }

    @Override
    public ValidatorResult validate(Host host, Object ctx) {
        return validator.validate(host, ctx);
    }

    @Override
    public Validator<Host> getValidator() {
        return validator;
    }
}
