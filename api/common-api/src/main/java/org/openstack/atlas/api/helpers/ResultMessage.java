package org.openstack.atlas.api.helpers;

import java.util.List;
import org.openstack.atlas.api.validation.results.ExpectationResult;
import org.openstack.atlas.api.validation.results.ValidatorResult;

public class ResultMessage {
    public static String resultMessage(ValidatorResult result) {
        StringBuilder sb;
        sb = new StringBuilder();

        if (!result.passedValidation()) {
            List<ExpectationResult> ers = result.getValidationResults();
            sb.append(String.format("result.withMessage(["));
            for (ExpectationResult er : ers) {
                sb.append(String.format("%s", er.getMessage()));
                sb.append("])");
            }
        } else {
            sb.append(String.format("No Expectations Failed\n"));
        }
        return sb.toString();
    }

    public static String resultMessage(ValidatorResult result, Enum ctx) {
        StringBuilder sb;
        sb = new StringBuilder();
        if (!result.passedValidation()) {
            List<ExpectationResult> ers = result.getValidationResults();
            sb.append(String.format("ON %s result.withMessage([", ctx.toString()));
            for (ExpectationResult er : ers) {
                sb.append(String.format("%s", er.getMessage()));
                sb.append("])");
            }
        } else {
            sb.append(String.format("On %s All Expectations PASSED\n", ctx.toString()));
        }
        return sb.toString();
    }
}
