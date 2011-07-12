package org.openstack.atlas.api.validation.expectation;

public interface FinalizedExpectation extends CompleteExpectation {

    /**
     * Accepts string interpolation with one argument '$value'
     * <p/>
     * Example: "Argument shouldBeGreaterThanThree expects a value greater than 3 but contained $value"
     *
     * @param messageTemplate
     */
    CompleteExpectation withMessage(String messageTemplate);

    FinalizedExpectation forContext(Object... contexts);

    FinalizedExpectation forContexts(Object[] contexts);

    EmptyExpectation then();
}
