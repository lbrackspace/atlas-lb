package org.openstack.atlas.api.mapper.dozer.converter;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.dozer.CustomConverter;
import org.openstack.atlas.service.domain.exceptions.NoMappableConstantException;
import org.openstack.atlas.docs.loadbalancers.api.v1.Ciphers;
import org.openstack.atlas.docs.loadbalancers.api.v1.Cipher;

public class CiphersConverter implements CustomConverter {

    public static final Class apiCiphersClass;
    public static final Class dbStringClass;

    static {
        apiCiphersClass = org.openstack.atlas.docs.loadbalancers.api.v1.Ciphers.class;
        dbStringClass = String.class;
    }

    @Override
    public Object convert(Object dstValue, Object srcValue, Class dstClass, Class srcClass) {
        if (srcValue == null) {
            return null;
        }
        if (srcValue.getClass() == apiCiphersClass && dstClass == dbStringClass) {
            String dbStr = convertCiphers2CommaSeperatedList((Ciphers) srcValue);
            return dbStr;
        } else if (srcValue.getClass() == dbStringClass && dstClass == apiCiphersClass) {
            String dbString = (String) srcValue;
            List<String> cipherNames = convertCommaAndSpaceSeparatedList2ListOfStrings(dbString);
            Ciphers ciphers = new Ciphers();
            List<Cipher> cipherList = ciphers.getCiphers();
            for (String cipherName : cipherNames) {
                Cipher cipher = new Cipher();
                cipher.setName(cipherName);
                cipherList.add(cipher);
            }
            return ciphers;
        } else {
            throw new NoMappableConstantException("Cannot map source type: " + srcClass.getName());
        }
    }

    String convertCiphers2CommaSeperatedList(Ciphers ciphers) {
        int i;
        int n;
        String out;
        StringBuilder sb = new StringBuilder();
        List<String> cipherNames = new ArrayList<String>();
        for (Cipher cipher : ciphers.getCiphers()) {
            cipherNames.add(cipher.getName());
        }
        Collections.sort(cipherNames);
        n = cipherNames.size();
        for (i = 0; i < n - 1; i++) {
            sb.append(cipherNames.get(i).trim()).append(",");
        }
        sb.append(cipherNames.get(n - 1));
        out = sb.toString();
        return out;
    }

    List<String> convertCommaAndSpaceSeparatedList2ListOfStrings(String cos) {
        List<String> out = new ArrayList<String>();
        for (String value : cos.split("[\\s,]")) {
            if(!value.equals("")) {
                out.add(value.trim());
            }
        }

        Collections.sort(out);
        return out;
    }
}
