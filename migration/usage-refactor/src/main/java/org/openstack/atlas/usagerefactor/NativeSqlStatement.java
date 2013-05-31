package org.openstack.atlas.usagerefactor;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NativeSqlStatement {

    private final String sql;
    private List<String> queryParameters = new ArrayList<String>();
    private final List<Map<Integer, String>> queryValues = new ArrayList<Map<Integer, String>>();
    private int currentParametersIndex = 0;

    public NativeSqlStatement(String sql) {
        this.sql = sql;
        this.queryValues.add(new HashMap<Integer, String>());
    }

    public void setString(int paramNumber, String paramValue) {
        queryValues.get(currentParametersIndex).put(paramNumber, paramValue);
    }

    public void addBatch() {
        queryValues.add(new HashMap<Integer, String>());

        StringBuilder parameters = new StringBuilder("(");

        for (int i = 0; i < queryValues.get(currentParametersIndex).size(); i++) {
            parameters.append("?,");
        }

        if (parameters.toString().endsWith(",")) {
            parameters.deleteCharAt(parameters.lastIndexOf(","));
        }

        parameters.append(")");

        queryParameters.add(parameters.toString());

        currentParametersIndex++;
    }

    public String generateSql() {
        validateParameters(sql, queryValues.get(0));
        StringBuilder fullQuery = new StringBuilder(replace(sql, queryValues.get(0)));

        int i = 1;
        for (String parameters : queryParameters) {
            if (validValues(i)) {
                fullQuery.append(",");
                validateParameters(parameters, queryValues.get(i));
                fullQuery.append(replace(parameters, queryValues.get(i++)));
            }
        }

        return fullQuery.toString();
    }

    private boolean validValues(int index) {
        return (index < queryValues.size()) && (queryValues.get(index).size() > 0);
    }

    private void validateParameters(String sqlParameters, Map<Integer, String> values) {
        if (StringUtils.countMatches(sqlParameters, "?") != values.size()) {
            throw new IllegalStateException("Number of '?' does not mach number of query values set.");
        }
    }

    private String replace(String sqlParameters, Map<Integer, String> values) {
        StringBuffer myStringBuffer = new StringBuffer();
        Pattern myPattern = Pattern.compile("\\?");
        Matcher myMatcher = myPattern.matcher(sqlParameters);

        int i = 1;
        while (myMatcher.find()) {
            myMatcher.appendReplacement(myStringBuffer, values.get(i++));
        }

        return myMatcher.appendTail(myStringBuffer).toString();
    }
}
