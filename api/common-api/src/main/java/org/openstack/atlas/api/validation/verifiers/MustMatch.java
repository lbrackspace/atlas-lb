package org.openstack.atlas.api.validation.verifiers;

import java.util.regex.Pattern;

public class MustMatch implements Verifier<Object> {

    private final Pattern myRegex;

    public MustMatch(Pattern myRegex) {
        this.myRegex = myRegex;
    }

    public VerifierResult verify(Object obj) {
        return new VerifierResult(obj != null && myRegex.matcher(obj.toString()).matches());
    }
}
