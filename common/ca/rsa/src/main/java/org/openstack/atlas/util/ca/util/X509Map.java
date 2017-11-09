package org.openstack.atlas.util.ca.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bouncycastle.cert.X509CertificateHolder;

import org.openstack.atlas.util.ca.primitives.RsaConst;

public class X509Map {

    private Set<X509MapValue> mapValSet;
    private Map<String, Set<X509MapValue>> fileMap;
    private Map<X509CertificateHolder, Set<X509MapValue>> x509Map;

    static {
        RsaConst.init();
    }

    public X509Map() {
        mapValSet = new HashSet<X509MapValue>();
        fileMap = new HashMap<String, Set<X509MapValue>>();
        x509Map = new HashMap<X509CertificateHolder, Set<X509MapValue>>();
    }

    public X509Map(Collection<X509MapValue> map) {
        mapValSet = new HashSet<X509MapValue>();
        fileMap = new HashMap<String, Set<X509MapValue>>();
        x509Map = new HashMap<X509CertificateHolder, Set<X509MapValue>>();
        for (X509MapValue mapVal : map) {
            if (!fileMap.containsKey(mapVal.getFileName())) {
                fileMap.put(mapVal.getFileName(), new HashSet<X509MapValue>());
            }
            if (!x509Map.containsKey(mapVal.getX509CertificateHolder())) {
                x509Map.put(mapVal.getX509CertificateHolder(), new HashSet<X509MapValue>());
            }
            mapValSet.add(mapVal);
            fileMap.get(mapVal.getFileName()).add(mapVal);
            x509Map.get(mapVal.getX509CertificateHolder()).add(mapVal);
        }
    }

    public void clear() {
        mapValSet = new HashSet<X509MapValue>();
        fileMap = new HashMap<String, Set<X509MapValue>>();
        x509Map = new HashMap<X509CertificateHolder, Set<X509MapValue>>();
    }

    public void putAll(Collection<X509MapValue> mapVals) {
        for (X509MapValue mapVal : mapVals) {
            put(mapVal);
        }
    }

    public void put(X509MapValue mapVal) {
        mapValSet.add(mapVal);
        if (!fileMap.containsKey(mapVal.getFileName())) {
            fileMap.put(mapVal.getFileName(), new HashSet<X509MapValue>());
        }
        if (!x509Map.containsKey(mapVal.getX509CertificateHolder())) {
            x509Map.put(mapVal.getX509CertificateHolder(), new HashSet<X509MapValue>());
        }
        fileMap.get(mapVal.getFileName()).add(mapVal);
        x509Map.get(mapVal.getX509CertificateHolder()).add(mapVal);
    }

    public Set<String> fileKeys() {
        return fileMap.keySet();
    }

    public Set<X509CertificateHolder> x509CertificateObjectKeys() {
        return x509Map.keySet();
    }

    public Set<X509MapValue> getFile(String fileName) {
        Set<X509MapValue> resultSet = fileMap.get(fileName);
        if (resultSet == null) {
            return new HashSet<X509MapValue>(); // Its impolite to return null when an Empty list is better
        }
        return resultSet;
    }

    public Set<X509MapValue> getX509CertificateHolder(X509CertificateHolder x509Holder) {
        Set<X509MapValue> resultSet = x509Map.get(x509Holder);
        if (resultSet == null) {
            return new HashSet<X509MapValue>(); // See above comment
        }
        return resultSet;
    }

    public Set<X509MapValue> values() {
        return new HashSet<X509MapValue>(mapValSet);
    }
    
    public Set<X509CertificateHolder> valuesAsX509Certificates(){
        Set<X509CertificateHolder> values = new HashSet<X509CertificateHolder>();
        for(X509MapValue mapVal : mapValSet){
            X509CertificateHolder x509 = mapVal.getX509CertificateHolder();
            values.add(x509);
        }
        return values;
    }
}
