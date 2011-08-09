package org.openstack.atlas.core.api.validation.expectation;

public interface ConditionalExpectation extends EmptyExpectation {
    OngoingExpectation if_();
}
