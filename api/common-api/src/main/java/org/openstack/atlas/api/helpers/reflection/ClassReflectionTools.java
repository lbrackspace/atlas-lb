package org.openstack.atlas.api.helpers.reflection;

import javax.xml.bind.annotation.XmlRootElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ClassReflectionTools {

    public static Object newInstance(String byString) throws ClassReflectionToolsException {
        Class oClass;
        Object obj = null;
        {
            try {
                oClass = Class.forName(byString);
                obj = oClass.newInstance();
                return obj;
            } catch (ClassNotFoundException ex) {
                throw new ClassReflectionToolsException(ex);
            } catch (InstantiationException ex) {
                throw new ClassReflectionToolsException(ex);
            } catch (IllegalAccessException ex) {
                throw new ClassReflectionToolsException(ex);
            }
        }
    }

    public static <T> T newInstance(Class<T> oClass) throws ClassReflectionToolsException {
        T obj = null;
        {
            try {
                obj = oClass.newInstance();
                return obj;
            } catch (InstantiationException ex) {
                throw new ClassReflectionToolsException(ex);
            } catch (IllegalAccessException ex) {
                throw new ClassReflectionToolsException(ex);
            }
        }
    }

    public static Object invokeGetter(Object obj, String methodName) throws ClassReflectionToolsException {
        Object out = null;
        Class oClass;
        Method m;


        try {
            oClass = obj.getClass();
            m = oClass.getMethod(methodName, new Class[]{});
            out = m.invoke(obj);
            return out;
        } catch (IllegalAccessException ex) {
            throw new ClassReflectionToolsException(ex);
        } catch (IllegalArgumentException ex) {
            throw new ClassReflectionToolsException(ex);
        } catch (InvocationTargetException ex) {
            throw new ClassReflectionToolsException(ex);
        } catch (NoSuchMethodException ex) {
            throw new ClassReflectionToolsException(ex);
        } catch (SecurityException ex) {
            throw new ClassReflectionToolsException(ex);
        }

    }

    public static Class getReturnTypeForGetter(Object obj, String methodName) throws ClassReflectionToolsException {
        Class discoveredClass = null;
        Class objClass = obj.getClass();
        Method m;
        try {
            m = objClass.getMethod(methodName, new Class[]{});
        } catch (NoSuchMethodException ex) {
            throw new ClassReflectionToolsException(ex);
        } catch (SecurityException ex) {
            throw new ClassReflectionToolsException(ex);
        }
        discoveredClass = m.getReturnType();
        return discoveredClass;
    }

    public static String getXmlRootElementName(Object obj) {
        Class oClass = obj.getClass();
        return getXmlRootElementName(oClass);
    }

    public static String getXmlRootElementName(Class oClass) {
        String rootElementName = null;
        XmlRootElement xmlRootElementAnnotation;
        if (oClass == null) {
            return null;
        }
        xmlRootElementAnnotation = (XmlRootElement) oClass.getAnnotation(XmlRootElement.class);
        if (xmlRootElementAnnotation != null) {
            rootElementName = xmlRootElementAnnotation.name();
        }
        return rootElementName;
    }

    public static String getXmlRootElementNameSpace(Object obj) {
        Class oClass = obj.getClass();
        return getXmlRootElementNameSpace(oClass);
    }

    public static String getXmlRootElementNameSpace(Class oClass) {
        String rootElementNameSpace = null;
        XmlRootElement xmlRootElementAnnotation;
        if (oClass == null) {
            return null;
        }
        xmlRootElementAnnotation = (XmlRootElement) oClass.getAnnotation(XmlRootElement.class);
        if (xmlRootElementAnnotation != null) {
            rootElementNameSpace = xmlRootElementAnnotation.namespace();
        }
        return rootElementNameSpace;
    }
}
