package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Backup;
import org.openstack.atlas.api.mgmt.validation.validators.BackupValidator;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.Calendar;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class BackupValidatorTest {

    public static class WhenValidatingPostRequest {

        private BackupValidator validator;
        private Backup backup;

        @Before
        public void standUp() {
            validator = new BackupValidator();
            backup = new Backup();

            backup.setName("World's greatest backup!");
        }

        @Test
        public void shouldAcceptValidBackupObject() {
            ValidatorResult result = validator.validate(backup, POST);
            assertTrue(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenIdIsSet() {
            backup.setId(1234);
            ValidatorResult result = validator.validate(backup, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenBackupTimeIsSet() {
            backup.setBackupTime(Calendar.getInstance());
            ValidatorResult result = validator.validate(backup, POST);
            assertFalse(result.passedValidation());
        }

        @Test
        public void shouldRejectWhenHostIdIsSet() {
            backup.setHostId(69);
            ValidatorResult result = validator.validate(backup, POST);
            assertFalse(result.passedValidation());
        }
    }
}
