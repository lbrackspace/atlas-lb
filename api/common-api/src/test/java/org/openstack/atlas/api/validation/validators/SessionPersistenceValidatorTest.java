package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.v1.PersistenceType;
import org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SessionPersistenceValidatorTest {
    private SessionPersistenceValidator validator;
    private SessionPersistence sp;

    @Before
    public void setUpValidValidatorObject() {
        validator = new SessionPersistenceValidator();

        sp = new SessionPersistence();
        sp.setPersistenceType(PersistenceType.HTTP_COOKIE);

    }

    @Test
    public void shouldAcceptWhenGivenAValidSessionPersistenceObject() {
        ValidatorResult result = validator.validate(sp, HttpRequestType.POST);
        assertTrue(result.passedValidation());
    }

    @Test
    public void shoudRejectANullSessionPersistenceObject() {
        sp.setPersistenceType(null);
        ValidatorResult result = validator.validate(sp, HttpRequestType.POST);
        assertFalse(result.passedValidation());
    }

    @Test
    public void shouldRejectWhenMissingPersistenceType() {
        sp.setPersistenceType(null);
        ValidatorResult result = validator.validate(sp, HttpRequestType.POST);
        assertFalse(result.passedValidation());
    }


}
