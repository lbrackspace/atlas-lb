package org.openstack.atlas.api.validation.expectation;

public interface ConditionalExpectation extends EmptyExpectation {
    OngoingExpectation if_();
}
