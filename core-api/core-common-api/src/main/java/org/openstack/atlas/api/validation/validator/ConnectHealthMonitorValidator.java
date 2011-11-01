package org.openstack.atlas.api.validation.validator;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.builder.ConnectMonitorValidatorBuilder;
import org.openstack.atlas.core.api.v1.HealthMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

@Component
@Scope("request")
public class ConnectHealthMonitorValidator implements ResourceValidator<HealthMonitor> {
    protected Validator<HealthMonitor> validator;
    protected ConnectMonitorValidatorBuilder ruleBuilder;

    @Autowired
    public ConnectHealthMonitorValidator(ConnectMonitorValidatorBuilder ruleBuilder) {
        this.ruleBuilder = ruleBuilder;
        validator = build(ruleBuilder);
    }

    @Override
    public ValidatorResult validate(HealthMonitor healthMonitor, Object context) {
        ValidatorResult result = validator.validate(healthMonitor, context);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<HealthMonitor> getValidator() {
        return validator;
    }
}
