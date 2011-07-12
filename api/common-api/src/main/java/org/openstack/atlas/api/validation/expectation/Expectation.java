package org.openstack.atlas.api.validation.expectation;

import org.openstack.atlas.api.validation.verifiers.MustBeValidIpv4Address;
import org.openstack.atlas.api.validation.verifiers.MustBeValidIpv6Address;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.verifiers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class Expectation implements ConditionalExpectation, OngoingExpectation, FinalizedExpectation, Comparable<Expectation> {

    private final int id;
    private final List contextList;
    private Verifier verifier;
    //TODO: Extract into seperate class. Adapter class?
    private Verifier ifVerifier;
    private String messageTemplate;
    private boolean notFlag;
    private boolean ifFlag;

    public Expectation(int id) {
        this.id = id;
        contextList = new LinkedList();

        notFlag = false;
        ifFlag = false;
        verifier = null;
        ifVerifier = null;
    }

    public boolean hasVerifierSet() {
        return verifier != null;
    }

    public List<ValidationResult> validate(Object obj, Object context) {
        List<ValidationResult> validationResults = new ArrayList<ValidationResult>();

        if (ifVerifier != null) {
            VerifierResult result = ifVerifier.verify(obj);
            boolean valid = notFlag ? !result.passed() : result.passed();
            if (!valid) {
                return validationResults;
            }
        }

        VerifierResult result = verifier.verify(obj);
        boolean valid = notFlag ? !result.passed() : result.passed();

        if (!contextIsMet(context)) {
            validationResults.add(new ValidationResult(true, "Context doesn't match. Skipping..."));
            return validationResults;
        }

        if (valid) {
            validationResults.add(new ValidationResult(valid, ""));
        } else {
            validationResults.add(new ValidationResult(valid, messageTemplate));
            if (verifier instanceof MustDelegateTo) {
                validationResults.addAll(result.getResultList());
            }
        }

        return validationResults;
    }

    private boolean contextIsMet(Object context) {
        return contextList == null || contextList.isEmpty() ? true : contextList.contains(context);
    }

    @Override
    public int getExpectationId() {
        return id;
    }

    @Override
    public int compareTo(Expectation o) {
        return id - o.id;
    }

    @Override
    public Expectation withMessage(String messageTemplate) {
        this.messageTemplate = messageTemplate;

        return this;
    }

    //Default Validations
    @Override
    public OngoingExpectation must() {
        return this;
    }

    @Override
    public OngoingExpectation if_() {
        ifFlag = true;
        return this;
    }

    @Override
    public OngoingExpectation not() {
        notFlag = !notFlag;

        return this;
    }

    @Override
    public FinalizedExpectation exist() {
        setVerifier(new MustExist());

        return this;
    }

    @Override
    public FinalizedExpectation forContext(Object... contexts) {
        contextList.addAll(Arrays.asList(contexts));
        return this;
    }

    @Override
    public FinalizedExpectation forContexts(Object[] contexts) {
        contextList.addAll(Arrays.asList(contexts));

        return this;
    }

    @Override
    public FinalizedExpectation beValidIpv4Address() {
        setVerifier(new MustBeValidIpv4Address());
        return this;
    }

    @Override
    public FinalizedExpectation beValidIpv6Address() {
        setVerifier(new MustBeValidIpv6Address());
        return this;
    }

    @Override
    public FinalizedExpectation beEmptyOrNull() {
        setVerifier(new MustBeEmptyOrNull());

        return this;
    }

    @Override
    public FinalizedExpectation match(Pattern regex) {
        setVerifier(new MustMatch(regex));

        return this;
    }

    @Override
    public FinalizedExpectation haveSizeOfAtLeast(int num) {
        setVerifier(new MustHaveSizeOfAtLeast(num));

        return this;
    }

    @Override
    public FinalizedExpectation cannotExceedSize(int num) {
        setVerifier(new CannotExceedSize(num));

        return this;
    }

    @Override
    public FinalizedExpectation haveSizeOfExactly(int num) {
        setVerifier(new MustHaveSizeOfExactly(num));

        return this;
    }

    @Override
    public FinalizedExpectation delegateTo(Validator validator, Object delegateContext) {
        setVerifier(new MustDelegateTo(validator, delegateContext));

        return this;
    }

    @Override
    public FinalizedExpectation valueEquals(Object obj) {
        setVerifier(new MustEqualTo(obj));

        return this;
    }

    @Override
    public FinalizedExpectation adhereTo(Verifier customVerifier) {
        setVerifier(customVerifier);

        return this;
    }

    @Override
    public EmptyExpectation then() {
        return this;
    }

    private void setVerifier(Verifier verifier) {
        if (ifFlag) {
            ifFlag = false;
            this.ifVerifier = verifier;
        } else {
            this.verifier = verifier;
        }
    }
}
