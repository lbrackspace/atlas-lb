package org.openstack.atlas.api.validation.validator.builder;

import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.verifier.MustBeInArray;
import org.openstack.atlas.core.api.v1.SessionPersistence;
import org.openstack.atlas.datamodel.PersistenceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
public class SessionPersistenceValidatorBuilder extends ValidatorBuilder<SessionPersistence> {

    @Autowired
    public SessionPersistenceValidatorBuilder(PersistenceType persistenceType) {
        super(SessionPersistence.class);

        // SHARED EXPECTATIONS
        result(validationTarget().getPersistenceType()).must().exist().withMessage("Must provide a persistence type.");
        result(validationTarget().getPersistenceType()).if_().exist().then().must().adhereTo(new MustBeInArray(persistenceType.toList())).withMessage("Persistence type is invalid. Please specify a valid persistence type.");
    }
}
