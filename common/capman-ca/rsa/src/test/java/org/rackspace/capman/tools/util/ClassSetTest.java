package org.rackspace.capman.tools.util;

import ClassSetClasses.ChildOfObjectOnly;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import ClassSetClasses.SomeBase;
import ClassSetClasses.SomeOtherSubOfBase;
import ClassSetClasses.SubSomeBase;
import ClassSetClasses.SubSubSomeBase;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import org.rackspace.capman.tools.ca.exceptions.X509PathBuildException;

public class ClassSetTest {

    private ClassSet MyClasses;

    public ClassSetTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        MyClasses = new ClassSet(ChildOfObjectOnly.class,
                SomeBase.class, SomeOtherSubOfBase.class, SubSomeBase.class,
                SubSubSomeBase.class);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void shouldFindSuperClasses() {
        ClassSet expClasses;
        Class targetClass;
        targetClass = SubSubSomeBase.class;
        expClasses = new ClassSet(SomeBase.class, SubSomeBase.class);
        Assert.assertTrue(superMatches(MyClasses, targetClass, expClasses));
        expClasses.add(Object.class);
        // Object should not be in the super list so this should fail
        Assert.assertFalse(superMatches(MyClasses, targetClass, expClasses));
        // Add Object to MyClasses and see if we pass now
        MyClasses.add(Object.class);
        Assert.assertTrue(superMatches(MyClasses, targetClass, expClasses));
    }

    @Test
    public void testIsSuperOfStaticMethod() {
        failFalse(ClassSet.isSuperOf(SomeBase.class, SubSubSomeBase.class));
        failFalse(ClassSet.isSuperOf(Object.class, SubSubSomeBase.class));
        failFalse(ClassSet.isSuperOf(SubSomeBase.class, SubSubSomeBase.class));
        failTrue(ClassSet.isSuperOf(SomeBase.class, Object.class));
        failTrue(ClassSet.isSuperOf(SubSubSomeBase.class, SomeBase.class));
    }

    private boolean superMatches(ClassSet main, Class target,
            ClassSet expected) {
        String mainStr = main.toString();
        String targetStr = target.getCanonicalName();
        String expectedStr = expected.toString();
        boolean val;
        ClassSet supers = main.getSupersOf(target);
        String supersStr = supers.toString();

        val = supers.equals(expected);
        return val;
    }

    private static void failTrue(String msg, boolean condition) {
        if (condition) {
            Assert.fail(msg);
        }
    }

    private static void failTrue(boolean condition) {
        if (condition) {
            Assert.fail();
        }
    }

    private static void failFalse(String msg, boolean condition) {
        failTrue(msg, !condition);
    }

    private static void failFalse(boolean condition) {
        failTrue(!condition);
    }
}
