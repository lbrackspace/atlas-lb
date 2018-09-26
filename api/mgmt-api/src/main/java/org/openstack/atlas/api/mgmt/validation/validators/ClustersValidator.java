package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Clusters;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.validators.ValidatorUtilities;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class ClustersValidator implements ResourceValidator<Clusters> {

    private final Validator<Clusters> validator;

    public ClustersValidator() {
        validator = build(new ValidatorBuilder<Clusters>(
                Clusters.class) {

            {
                result(validationTarget().getClusters()).must().exist().withMessage("Must provide a cluster.");
                result(validationTarget().getClusters()).if_().exist().then().must().haveSizeOfAtLeast(1).withMessage("Must provide at least one cluster.");
                result(validationTarget().getClusters()).must().delegateTo(new ClusterValidator().getValidator(), POST);

            }
        });
    }

    @Override
    public ValidatorResult validate(Clusters clusters, Object httpRequestType) {
        ValidatorResult result = validator.validate(clusters, httpRequestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<Clusters> getValidator() {
        return validator;
    }
}
