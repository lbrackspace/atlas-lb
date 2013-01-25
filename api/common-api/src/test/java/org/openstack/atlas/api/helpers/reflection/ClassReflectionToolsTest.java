package org.openstack.atlas.api.helpers.reflection;

import org.junit.Ignore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.junit.Assert;

import javax.xml.bind.annotation.XmlRootElement;

public class ClassReflectionToolsTest {

    private ReturnTypeTest rt;
    private List<PropMap> propMaps;

    @Before
    public void setUp() {
        rt = new ReturnTypeTest();

        propMaps = new ArrayList<PropMap>();

        propMaps.add(new PropMap(Integer.class, "getxInteger", rt.getxInteger()));
        propMaps.add(new PropMap(String.class, "getxString", rt.getxString()));
        propMaps.add(new PropMap(Double.class, "getPi", rt.getPi()));
        propMaps.add(new PropMap(int.class, "getXint", rt.getXint()));
        propMaps.add(new PropMap(double.class, "getXdouble", rt.getXdouble()));
        propMaps.add(new PropMap(List.class, "getxStrList", rt.getxStrList()));
    }

    @Test
    public void shouldBeAbleToCreateNewInstanceWithNullConstructorArgsByClass() throws ClassReflectionToolsException {
        Object stringObj;
        Object strbuffObj;

        stringObj = ClassReflectionTools.newInstance(java.lang.String.class);
        strbuffObj = ClassReflectionTools.newInstance(java.lang.StringBuilder.class);

        Assert.assertTrue(stringObj instanceof java.lang.String);
        Assert.assertTrue(strbuffObj instanceof java.lang.StringBuilder);
    }

    @Test
    public void shouldBeAbleToCreateNewInstanceWithNullConstructorClassName() throws ClassReflectionToolsException {
        Object stringObj;
        Object strbuffObj;

        stringObj = ClassReflectionTools.newInstance("java.lang.String");
        strbuffObj = ClassReflectionTools.newInstance("java.lang.StringBuilder");

        Assert.assertTrue(stringObj instanceof java.lang.String);
        Assert.assertTrue(strbuffObj instanceof java.lang.StringBuilder);
    }

    @Test
    public void shouldMapPropertyClassesCorrectly() throws ClassReflectionToolsException {

        for (PropMap propMap : propMaps) {
            Class eClass = propMap.getPropClass();
            String mName = propMap.getPropName();
            Class gClass = ClassReflectionTools.getReturnTypeForGetter(rt, mName);
            Assert.assertEquals(eClass, gClass);
        }
        nop();
    }

    @Test
    public void shouldMapPropertyValuesCorrectly() throws ClassReflectionToolsException {
        Class c;
        Collection sList;
        for (PropMap propMap : propMaps) {
            Class eClass = propMap.getPropClass();
            String mName = propMap.getPropName();
            Object eVal = propMap.getPropVal();

            Class gClass = ClassReflectionTools.getReturnTypeForGetter(rt, mName);
            Object gVal = ClassReflectionTools.invokeGetter(rt, mName);
            Assert.assertEquals(eVal, gVal);
            nop();
        }
        nop();
    }

    private void nop() {
    }

    @Test
    public void shouldMapRootElementCorrectly() {
        Integer x = new Integer(-1);
        Assert.assertEquals(ClassReflectionTools.getXmlRootElementName(rt), "testRoot");
        Assert.assertEquals(ClassReflectionTools.getXmlRootElementName(x), null);
    }

    @Test
    public void shouldMapRootElementNameSpaceCorrectly() {
        Integer x = new Integer(-1);
        Assert.assertEquals(ClassReflectionTools.getXmlRootElementNameSpace(rt), "bsd");
        Assert.assertEquals(ClassReflectionTools.getXmlRootElementNameSpace(x), null);
    }

    @Ignore
    @XmlRootElement(name = "testRoot", namespace = "bsd")
    public class ReturnTypeTest {

        private Integer xInteger;
        private String xString;
        private int xint;
        private Double pi;
        private double xdouble;
        private List<String> xStrList;

        public ReturnTypeTest() {
            xInteger = 100;
            xString = "TestString";
            xint = -1;
            xdouble = 1.0;
            pi = new Double("3.1415928");
            xStrList = new ArrayList<String>();
            xStrList.add("Str1");
            xStrList.add("Str2");
        }

        public Integer getxInteger() {
            return xInteger;
        }

        public String getxString() {
            return xString;
        }

        public int getXint() {
            return xint;
        }

        public Double getPi() {
            return pi;
        }

        public double getXdouble() {
            return xdouble;
        }

        public List<String> getxStrList() {
            return xStrList;
        }
    }

    @Ignore
    public class PropMap {

        private Class propClass;
        private String propName;
        private Object propVal;

        public PropMap() {
        }

        public PropMap(Class propClass, String propName, Object val) {
            this.propClass = propClass;
            this.propName = propName;
            this.propVal = val;
        }

        public Class getPropClass() {
            return propClass;
        }

        public String getPropName() {
            return propName;
        }

        public Object getPropVal() {
            return propVal;
        }
    }
}
