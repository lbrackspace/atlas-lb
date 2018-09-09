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
                // ARED EXPECTATIONS
                result(validationTarget().getName()).must().not().beEmptyOrNull().withMessage("Must provide a name.");

                must().adhereTo(new Verifier<Cluster>() {

                    @Override
                    public VerifierResult verify(Cluster cluster) {
                        return new VerifierResult(cluster.getNumberOfHostMachines() == null && cluster.getNumberOfLoadBalancingConfigurations() == null && cluster.getNumberOfUniqueCustomers() == null && cluster.getUtilization() == null);
                    }
                }).withMessage("Only the proper properties can be made into a request, please remove invalid elements.");

                // POST EXPECTATIONS
                result(validationTarget().getDataCenter()).must().not().beEmptyOrNull().forContext(POST).withMessage("Must provide a valid Data Center.");
                result(validationTarget().getUsername()).must().not().beEmptyOrNull().forContext(POST).withMessage("Must provide a valid username.");
                result(validationTarget().getPassword()).must().not().beEmptyOrNull().forContext(POST).withMessage("Must provide a valid password.");
                result(validationTarget().getId()).must().not().exist().forContext(POST,PUT).withMessage("Must not include ID for this request.");
                result(validationTarget().getDescription()).must().not().beEmptyOrNull().forContext(POST).withMessage("Must provide a description.");
                result(validationTarget().getStatus()).must().not().beEmptyOrNull().forContext(POST).withMessage("Must provide a status.");
                // PUT EXPECTATIONS
                result(validationTarget().getDataCenter()).must().not().exist().forContext(PUT).withMessage("Data Center can not be updated in this request.");

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
