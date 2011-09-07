package org.openstack.atlas.api.validation.expectation;

public interface IfExpectation extends EmptyExpectation {
    OngoingExpectation<ThenExpectation> if_();
}
