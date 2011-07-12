package org.openstack.atlas.api.validation;

import org.openstack.atlas.api.validation.expectation.*;
import org.openstack.atlas.api.validation.util.CallStateRegistry;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import org.openstack.atlas.api.validation.exceptions.UnfinishedExpectationChainException;
import net.sf.cglib.proxy.Enhancer;

public class ValidatorBuilder<ObjectTypeToValidate> {

    private final List<ExpectationTarget<ObjectTypeToValidate>> validationTargetList;
    private final CallStateRegistry sharedCallState;
    private final ObjectTypeToValidate validationTarget;
    private volatile int currentExpectationId;

    public ValidatorBuilder(Class<ObjectTypeToValidate> type) {
        validationTargetList = new LinkedList<ExpectationTarget<ObjectTypeToValidate>>();
        sharedCallState = new CallStateRegistry();

        currentExpectationId = 0;

        final Enhancer eh = new Enhancer();
        eh.setSuperclass(type);
        eh.setCallback(new ValidationInterceptor(sharedCallState));

        validationTarget = (ObjectTypeToValidate) eh.create();
    }

    protected ObjectTypeToValidate validationTarget() {
        return validationTarget;
    }

    protected OngoingExpectation must() {
        final Expectation freshExpectation = new Expectation(++currentExpectationId);

        synchronized (validationTargetList) {
            //Null - No target means root
            addExpectationToExpectationTarget(null, freshExpectation);
        }

        return freshExpectation.must();
    }

    protected OngoingExpectation if_() {
        final Expectation freshExpectation = new Expectation(++currentExpectationId);

        synchronized (validationTargetList) {
            //Null - No target means root
            addExpectationToExpectationTarget(null, freshExpectation);
        }

        return freshExpectation.if_();
    }

    protected <R> ConditionalExpectation result(R methodCallResult) {
        final Method callResultBelongsTo = sharedCallState.getLastKnownCall();

        if (callResultBelongsTo == null) {
            throw new IllegalArgumentException("Method call handed to expectation builder is not trackable. Please use the object being validated.");
        } else if (callResultBelongsTo.getParameterTypes().length > 0) {
            throw new IllegalArgumentException("Methods with parameters are not eligible for validation");
        }

        final Expectation freshExpectation = new Expectation(++currentExpectationId);
        addExpectationToExpectationTarget(callResultBelongsTo, freshExpectation);

        return freshExpectation;
    }

    private synchronized void addExpectationToExpectationTarget(final Method callResultBelongsTo, final Expectation freshExpectation) {
        boolean foundExistingTarget = false;

        for (ExpectationTarget<ObjectTypeToValidate> existingTarget : validationTargetList) {
            if (existingTarget.targets(callResultBelongsTo)) {
                existingTarget.addExpectation(freshExpectation);
                foundExistingTarget = true;
                
                break;
            }
        }

        if (!foundExistingTarget) {
            final ExpectationTarget<ObjectTypeToValidate> newTarget = new ExpectationTarget<ObjectTypeToValidate>(callResultBelongsTo);
            newTarget.addExpectation(freshExpectation);
            validationTargetList.add(newTarget);
        }
    }

    private synchronized void validateSelf() {
        final StringBuilder strBuff = new StringBuilder("Broken expectations:");
        boolean validationChainOk = true;

        for (ExpectationTarget<ObjectTypeToValidate> targetToCheck : validationTargetList) {
            final SelfValidationResult selfValidationResult = targetToCheck.isSatisfied();
            
            validationChainOk = selfValidationResult.isValid();
            strBuff.append(selfValidationResult.getErrorBuffer());
        }
        
        if (!validationChainOk) {
            throw new UnfinishedExpectationChainException(strBuff.toString());
        }
    }

    public static <R, S> Validator<R> build(ValidatorBuilder<R> builder) {
        return builder.toValidator();
    }

    public Validator<ObjectTypeToValidate> toValidator() {
        validateSelf();

        return new ValidatorImpl<ObjectTypeToValidate>(validationTargetList);
    }
}
