package org.openstack.atlas.api.validation;

/**
 *
 * @author John Hopper
 */
public class SimpleBean {
    private String stringProperty1;
    private String stringProperty2;
    private String stringProperty3;

    private Integer intProperty1;
    private Integer intProperty2;
    private Integer intProperty3;

    public SimpleBean() {
    }

    public SimpleBean(String stringProperty1, String stringProperty2, String stringProperty3, Integer intProperty1, Integer intProperty2, Integer intProperty3) {
        this.stringProperty1 = stringProperty1;
        this.stringProperty2 = stringProperty2;
        this.stringProperty3 = stringProperty3;

        this.intProperty1 = intProperty1;
        this.intProperty2 = intProperty2;
        this.intProperty3 = intProperty3;
    }

    public String doSomething(String st, Integer a) {
        return "a";
    }

    public Integer getIntProperty3() throws Exception {
        throw new Exception("Call failed");
    }

    public Integer getIntProperty1() {
        return intProperty1;
    }

    public Integer getIntProperty2() {
        return intProperty2;
    }

    public String getStringProperty1() {
        return stringProperty1;
    }

    public String getStringProperty2() {
        return stringProperty2;
    }

    public String getStringProperty3() {
        return stringProperty3;
    }
}
