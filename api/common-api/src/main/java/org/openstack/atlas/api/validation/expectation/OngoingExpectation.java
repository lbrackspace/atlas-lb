package org.openstack.atlas.api.validation.expectation;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.verifiers.Verifier;

import java.util.regex.Pattern;

public interface OngoingExpectation {

    OngoingExpectation not();

    FinalizedExpectation exist();

    FinalizedExpectation beEmptyOrNull();

    FinalizedExpectation match(Pattern regex);

    FinalizedExpectation adhereTo(Verifier customVerifier);

    FinalizedExpectation haveSizeOfAtLeast(int num);

    FinalizedExpectation cannotExceedSize(int num);

    FinalizedExpectation haveSizeOfExactly(int num);

    FinalizedExpectation delegateTo(Validator validator, Object delegateContext);

    FinalizedExpectation valueEquals(Object obj);

    FinalizedExpectation beValidIpv4Address();

    FinalizedExpectation beValidIpv6Address();
}
