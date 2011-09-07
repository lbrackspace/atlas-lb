package org.openstack.atlas.api.validation.expectation;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.verifier.Verifier;

import java.util.regex.Pattern;

public interface OngoingExpectation<T> {

    OngoingExpectation<T> not();

    T exist();

    T beEmptyOrNull();

    T match(Pattern regex);

    T adhereTo(Verifier customVerifier);

    T instanceOf(Class classType);

    T haveSizeOfAtLeast(int num);

    T haveSizeOfAtMost(int num);

    T haveSizeOfExactly(int num);

    T delegateTo(Validator validator, Object delegateContext);

    T valueEquals(Object obj);

    T beValidIpv4Address();

    T beValidIpv6Address();
}
