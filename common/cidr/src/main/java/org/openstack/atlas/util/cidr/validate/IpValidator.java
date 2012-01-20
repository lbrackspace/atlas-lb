package org.openstack.atlas.util.cidr.validate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.openstack.atlas.util.ip.IPUtils;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IpValidator {

    private ValidateType type;
    private ValidateVersion version;

    public IpValidator() {
    }

    public IpValidator(ValidateType type, ValidateVersion version) {
        this.type = type;
        this.version = version;
    }

    public String getValidateResultStringNoExcept(String ip) {
        boolean valid;
        String validStr;
        String versionStr;
        String typeStr;
        String out;
        ip = ip.trim();
        valid = validateNoExcept(ip);

        if (valid) {
            validStr = "VALID ";
        } else {
            validStr = "INVALID";
        }

        switch(version) {
            case IPV4:
                versionStr = "IPv4";
                break;
            case IPV6:
                versionStr = "IPv6";
                break;
            default:
                versionStr = "UNKNOWN IPVERSION";
        }
        
        switch (type) {
            case IP:
                typeStr = "address";
                break;
            case SUBNET:
                typeStr = "subnet";
                break;
            default:
                typeStr = "UNKNOWN SUBNET OR ADDRESS";
        }
        out = String.format("%s %s %s %s\n",ip,validStr,versionStr,typeStr);
        return out;
    }

    public boolean validateNoExcept(String ip) {
        boolean out;
        ip = ip.trim();
        try {
            out = validate(ip);
        } catch (SecurityException ex) {
            out = false;
        } catch (NoSuchMethodException ex) {
            out = false;
        } catch (IllegalArgumentException ex) {
            out = false;
        } catch (IllegalAccessException ex) {
            out = false;
        } catch (InvocationTargetException ex) {
            out = false;
        }
        return out;
    }

    public boolean validate(String ip) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method method;
        ip = ip.trim();
        Class iputils = IPUtils.class;
        StringBuilder sb = new StringBuilder(24);
        String methodName;
        sb.append("isValid");
        boolean out;
        Boolean tmp;
        Object obj;
        switch (version) {
            case IPV4:
                sb.append("Ipv4");
                break;
            case IPV6:
                sb.append("Ipv6");
                break;
            default:
                out = false;
                return false;
        }

        switch (type) {
            case IP:
                sb.append("String");
                break;
            case SUBNET:
                sb.append("Subnet");
                break;
            default:
                out = false;
                return out;
        }
        methodName = sb.toString();
        method = iputils.getMethod(methodName, String.class);
        obj = method.invoke(null, ip);
        tmp = (Boolean) obj;
        out = (boolean) tmp;
        return out;
    }

    public ValidateType getType() {
        return type;
    }

    public void setType(ValidateType type) {
        this.type = type;
    }

    public ValidateVersion getVersion() {
        return version;
    }

    public void setVersion(ValidateVersion version) {
        this.version = version;
    }
}
