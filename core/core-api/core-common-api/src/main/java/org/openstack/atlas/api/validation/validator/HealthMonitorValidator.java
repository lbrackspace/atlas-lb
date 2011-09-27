package org.openstack.atlas.api.validation.validator;

import org.openstack.atlas.api.validation.validator.builder.HealthMonitorValidatorBuilder;
import org.openstack.atlas.core.api.v1.HealthMonitor;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.verifier.HealthMonitorTypeVerifier;
import org.openstack.atlas.api.validation.verifier.MustBeIntegerInRange;
import org.openstack.atlas.datamodel.CoreHealthMonitorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

@Component
@Scope("request")
public class HealthMonitorValidator implements ResourceValidator<HealthMonitor> {
    protected Validator<HealthMonitor> validator;
    protected HealthMonitorValidatorBuilder ruleBuilder;

    @Autowired
    public HealthMonitorValidator(HealthMonitorValidatorBuilder ruleBuilder) {
        this.ruleBuilder = ruleBuilder;
        validator = build(ruleBuilder);
    }

    @Override
    public ValidatorResult validate(HealthMonitor healthMonitor, Object context) {
        return validator.validate(healthMonitor, context);
    }

    @Override
    public Validator<HealthMonitor> getValidator() {
        return validator;
    }
}
