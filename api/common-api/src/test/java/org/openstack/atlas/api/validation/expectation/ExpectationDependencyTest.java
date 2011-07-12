package org.openstack.atlas.api.validation.expectation;

import org.junit.Ignore;

//TODO: For reimplementation of this idea, I've left a few breadcrumbs but no implementation. See the return value of result(...) and must(...)

@Ignore
public class ExpectationDependencyTest {
//    private SimpleBean testObjectA;
//    private SimpleBean testObjectB;

//    @Before
//    public void setUpObjectsToValidate() {
//        testObjectA = new SimpleBean("1", null, null, 1, null, null);
//        testObjectB = new SimpleBean(null, null, null, null, null, null);
//    }
//
//    @Test
//    public void shouldMeetExpectationWithDependency() {
//        final RestValidator<SimpleBean> validator = build(new ValidatorBuilder<SimpleBean>(SimpleBean.class) {
//
//            {
//                Expectation ex1 = result(validationTarget().getIntProperty1()).must().exist().withMessage("IntProperty1 is required.");
//                result(validationTarget().getStringProperty1()).must().exist().withMessage("StringProperty1 is required.").setDependency(ex1, testObjectA);
//            }
//        });
//
//        assertTrue(validator.validate(testObjectA).passedValidation());
//    }
//
//    @Test
//    public void shouldNotReturnResultsWithDependencies() {
//        final RestValidator<SimpleBean> validator = build(new ValidatorBuilder<SimpleBean>(SimpleBean.class) {
//
//            {
//                Expectation ex1 = result(validationTarget().getIntProperty1()).must().exist().withMessage("IntProperty1 is required.");
//                result(validationTarget().getStringProperty1()).must().exist().withMessage("StringProperty1 is required.").setDependency(ex1, testObjectB.getIntProperty1());
//            }
//        });
//
//        ValidatorResult result = validator.validate(testObjectB);
//
//        assertFalse(result.passedValidation());
//        assertEquals(1, result.getValidationResults().size());
//    }
}
