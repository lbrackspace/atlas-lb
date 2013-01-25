package org.openstack.atlas.api.filters.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AcceptTypes {

    private List<AcceptType> acceptTypeList = new ArrayList<AcceptType>();

    public List<AcceptType> getAcceptTypeList() {
        return acceptTypeList;
    }

    public void setAcceptTypeList(List<AcceptType> acceptTypeList) {
        this.acceptTypeList = acceptTypeList;
    }

    public static AcceptTypes getPrefferedAcceptTypes(String strIn) {
        AcceptTypes out = new AcceptTypes();
        AcceptType acceptType;
        int i;
        String[] vals;
        if(strIn == null) {
            return out;
        }
        vals = strIn.split(",");
        for (i = 0; i < vals.length; i++) {
            acceptType = AcceptType.newInstance(vals[i].trim());
            if (acceptType != null) {
                out.getAcceptTypeList().add(acceptType);
            }
        }
        Collections.sort(out.getAcceptTypeList());
        return out;
    }

    public String mediaTypesToString() {
        StringBuilder sb = new StringBuilder();
        if (this.acceptTypeList == null) {
            return null;
        }
        for(AcceptType at:acceptTypeList) {
            sb.append(String.format("%s\n",at.toString()));
        }
        return sb.toString();
    }

    public String findSuitableMediaType(String ... mediaChoices) {
        for(AcceptType requested : acceptTypeList) {
            for(String choice : mediaChoices) {
                if(AcceptType.isAcceptableMedia(requested, AcceptType.newInstance(choice))) {
                    return choice;
                }
            }
        }
        return null;
    }
}