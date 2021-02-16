package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Cluster;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.validators.ValidatorUtilities;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;
import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class ClusterValidator implements ResourceValidator<Cluster> {

    private final Validator<Cluster> validator;

    public ClusterValidator() {
        validator = build(new ValidatorBuilder<Cluster>(
                Cluster.class) {

            {

                // POST EXPECTATIONS
                result(validationTarget().getDataCenter()).must().not().beEmptyOrNull().forContext(POST).withMessage("Must provide a valid Data Center.");
                result(validationTarget().getName()).must().not().beEmptyOrNull().forContext(POST).withMessage("Must provide a unique cluster name.");
                result(validationTarget().getUsername()).must().not().beEmptyOrNull().forContext(POST).withMessage("Must provide a valid username.");
                result(validationTarget().getPassword()).must().not().beEmptyOrNull().forContext(POST).withMessage("Must provide a valid password.");
                result(validationTarget().getDescription()).must().not().beEmptyOrNull().forContext(POST).withMessage("Must provide a description.");
                result(validationTarget().getStatus()).must().not().beEmptyOrNull().forContext(POST).withMessage("Must provide a status.");

                //Shared EXPECTATIONS
                result(validationTarget().getNumberOfUniqueCustomers()).must().not().exist().withMessage("Number of unique customers can not be updated in this request.");
                result(validationTarget().getUtilization()).must().not().exist().withMessage("Utilization can not be updated in this request.");
                result(validationTarget().getNumberOfLoadBalancingConfigurations()).must().not().exist().withMessage("Number of Load Balancing Configs can not be updated in this request.");
                result(validationTarget().getNumberOfHostMachines()).must().not().exist().withMessage("Number of host Machines can not be updated in this request.");
                result(validationTarget().getId()).must().not().exist().withMessage("Must not include ID for this request.");

            }
        });
    }

    @Override
    public ValidatorResult validate(Cluster cluster, Object httpRequestType) {
        ValidatorResult result = validator.validate(cluster, httpRequestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<Cluster> getValidator() {
        return validator;
    }
}
