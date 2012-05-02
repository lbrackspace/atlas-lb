package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.ContentCaching;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class ContentCachingValidator implements ResourceValidator<ContentCaching> {
    private Validator<ContentCaching> validator;
    private final Boolean bool = true;

    public ContentCachingValidator() {
        validator = build(new ValidatorBuilder<ContentCaching>(ContentCaching.class) {
            {

                result(validationTarget().isEnabled()).must().exist().withMessage("Must specify whether content caching is enabled or not.");
            }
        });
    }

    @Override
    public ValidatorResult validate(ContentCaching contentCaching, Object context) {
        ValidatorResult result = validator.validate(contentCaching, context);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<ContentCaching> getValidator() {
        return validator;
    }
}