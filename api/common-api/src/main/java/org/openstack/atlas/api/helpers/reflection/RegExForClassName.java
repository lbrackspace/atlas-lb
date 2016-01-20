package org.openstack.atlas.api.helpers.reflection;

import java.util.regex.Pattern;

import org.openstack.atlas.api.helpers.reflection.UriClassDiscover;

public class RegExForClassName {
    // if the request URL matches the regex then this assume "someClass" is
    // the datamodel object that will be passed into the URI.
    // this is so json objects that don't have a root element can be

    private String regExString;
    private String rootElement;
    private Pattern pattern;
    private Class someClass;

    public RegExForClassName() {
    }

    public RegExForClassName(String regEx, Class someClass) {
        this.regExString = regEx;
        this.pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        this.someClass = someClass;
        this.rootElement = UriClassDiscover.getRootElementNameForClass(someClass);
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Class getSomeClass() {
        return this.someClass;
    }

    public void setSomeClass(Class someClass) {
        this.someClass = someClass;
    }

    public String getRegExString() {
        return regExString;
    }

    public void setRegExString(String regExString) {
        this.regExString = regExString;
    }

    public String getRootElement() {
        return rootElement;
    }

    public void setRootElement(String rootElement) {
        this.rootElement = rootElement;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("{ regEx=\"").append(regExString).
                append("\", className=\"").append(someClass.getCanonicalName()).
                append("}");
        return sb.toString();
    }
}
