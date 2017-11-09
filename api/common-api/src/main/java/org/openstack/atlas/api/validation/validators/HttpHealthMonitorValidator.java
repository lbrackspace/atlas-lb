package org.openstack.atlas.api.validation.validators;

import org.apache.commons.lang.StringUtils;
import org.openstack.atlas.api.validation.verifiers.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.validators.HealthMonitorValidator.FLOOR;
import static org.openstack.atlas.api.validation.validators.HealthMonitorValidator.CEILING;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;
import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class HttpHealthMonitorValidator implements ResourceValidator<HealthMonitor> {
    private Validator<HealthMonitor> validator;
    private static final int MAXSTR=128;
    private static final String maxStrMsg = String.format(" can not exceed %d characters.",MAXSTR);

    public HttpHealthMonitorValidator() {
        this.validator = build(new ValidatorBuilder<HealthMonitor>(HealthMonitor.class) {
            {
                // SHARED EXPECTATIONS
                result(validationTarget().getType()).must().exist().withMessage("Must provide a type for the health monitor.");
                result(validationTarget().getId()).must().not().exist().withMessage("Health monitor id field cannot be modified.");
                result(validationTarget().getPath()).if_().exist().then().must().adhereTo(new CannotExceedSize(MAXSTR)).withMessage("path" + maxStrMsg);
                result(validationTarget().getPath()).if_().exist().then().must().adhereTo(new HealthMonitorPathVerifier()).withMessage("Must provide a forward slash(/) as the beginning of the path.");
                result(validationTarget().getStatusRegex()).if_().exist().then().must().adhereTo(new CannotExceedSize(MAXSTR)).withMessage("statusRegex" + maxStrMsg);
                result(validationTarget().getBodyRegex()).if_().exist().then().must().adhereTo(new CannotExceedSize(MAXSTR)).withMessage("bodyRegex" + maxStrMsg);
                result(validationTarget().getStatusRegex()).if_().exist().then().must().adhereTo(new RegexValidatorVerifier()).withMessage("Must provide a valid status regex");
                result(validationTarget().getBodyRegex()).if_().exist().then().must().adhereTo(new RegexValidatorVerifier()).withMessage("Must provide a valid body regex");
                result(validationTarget().getHostHeader()).if_().exist().then().must().adhereTo(new HostHeaderVerifier()).withMessage("Must provide a valid host header name.");
                result(validationTarget().getHostHeader()).if_().exist().then().must().adhereTo(new CannotExceedSize(256)).withMessage("Host Header field cannot exceed 256 characters");

                // PUT EXPECTATIONS
                must().adhereTo(new Verifier<HealthMonitor>() {
                    @Override
                    public VerifierResult verify(HealthMonitor monitor) {
                        return new VerifierResult(monitor.getDelay() != null ||
                                monitor.getTimeout() != null ||
                                monitor.getAttemptsBeforeDeactivation() != null ||
                                StringUtils.isNotEmpty(monitor.getPath())||
                                StringUtils.isNotEmpty(monitor.getStatusRegex()) ||
                                StringUtils.isNotEmpty(monitor.getBodyRegex()));
                    }
                }).forContext(PUT).withMessage("The health monitor must have at least one of the following to update: delay, timeout, attempts before deactivation, path, status regex, body regex.");

                //POST EXPECTATIONS
                //CLB-279 Creating a Load Balancer with empty Health Monitor - creates the Load Balancer in Error state.
                result(validationTarget().getPath()).must().adhereTo(new MustNotBeEmpty()).forContext(POST).withMessage("Must provide a valid path for the health monitor.");
                result(validationTarget().getStatusRegex()).must().adhereTo(new MustNotBeEmpty()).forContext(POST).withMessage("Must provide a status regex for the health monitor.");
                result(validationTarget().getBodyRegex()).must().adhereTo(new MustNotBeEmpty()).forContext(POST).withMessage("Must provide a body regex for the health monitor.");
            }
        });
    }

    @Override
    public ValidatorResult validate(HealthMonitor monitor, Object monitorContext) {
        ValidatorResult result = validator.validate(monitor, monitorContext);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<HealthMonitor> getValidator() {
        return validator;
    }
}
