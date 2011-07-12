package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Backup;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.validators.ValidatorUtilities;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class BackupValidator implements ResourceValidator<Backup> {

    private final Validator<Backup> validator;

    public BackupValidator() {
        validator = build(new ValidatorBuilder<Backup>(Backup.class) {
            {
                // POST EXPECTATIONS
                result(validationTarget().getName()).must().exist().forContext(POST).withMessage("Must provide a name for the backup.");
                result(validationTarget().getId()).must().not().exist().forContext(POST).withMessage("Backup id field cannot be modified.");
                result(validationTarget().getBackupTime()).must().not().exist().forContext(POST).withMessage("Backup time cannot be modified.");
                result(validationTarget().getHostId()).must().not().exist().withMessage("HostId field cannot be modified");
            }
        });
    }

    @Override
    public ValidatorResult validate(Backup backup, Object httpRequestType) {
        ValidatorResult result = validator.validate(backup, httpRequestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<Backup> getValidator() {
        return validator;
    }

}
