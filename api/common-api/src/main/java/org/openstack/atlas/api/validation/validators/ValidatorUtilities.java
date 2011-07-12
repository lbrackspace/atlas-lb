package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.api.validation.results.ExpectationResult;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public final class ValidatorUtilities {

    public static final Pattern POS32BIT_REGEX = Pattern.compile("(\\b([1-9]|[0-9]{2,9}|1[0-9]{9}|2(0[0-9]{8}|1([0-3]" +
            "[0-9]{7}|4([0-6][0-9]{6}|7([0-3][0-9]{5}|4([0-7][0-9]{4}|8([0-2][0-9]{3}|3([0-5][0-9]{2}|6([0-3][0-9]|4[0-7])))))))))\\b)");

    public static final Pattern IPV4_REGEX = Pattern.compile("^((25[0-5]|2[0-4]\\d|1\\d\\d"
            + "|[1-9]\\d|\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]\\d|\\d)(/(3[0-2]|[0-2][0-9]|[0-9]))?$");

    public static final Pattern IPV6_REGEX = Pattern.compile("^\\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))"
            + "|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)"
            + "(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})"
            + "|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))"
            + "|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:"
            + "((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))"
            + "|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:"
            + "((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))"
            + "|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:"
            + "((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))"
            + "|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:"
            + "((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))"
            + "|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)"
            + "(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?\\s*"
            + "(/([0-1][0-2][0-8]|1[0-1][0-9]|[0-9][0-9]|[0-9]))?$");



    public static ValidatorResult removeEmptyMessages(ValidatorResult result) {
        List<ExpectationResult> filteredResults = new LinkedList<ExpectationResult>();

        for (ExpectationResult expectationResult : result.getValidationResults()) {
            if (expectationResult.getMessage() != null) {
                filteredResults.add(expectationResult);
            }
        }

        return new ValidatorResult(filteredResults);
    }
}
