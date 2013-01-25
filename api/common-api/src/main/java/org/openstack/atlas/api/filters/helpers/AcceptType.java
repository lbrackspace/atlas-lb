package org.openstack.atlas.api.filters.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class AcceptType implements Comparable<AcceptType> {

    private MediaType mediaType;
    private double q = 1.0;
    private List<String> acceptExtensions = new ArrayList<String>();
    private static final Pattern qRe = Pattern.compile("^\\s*[qQ]\\s*=\\s*([0-9]+\\.?[0-9]*)\\s*$");

    public AcceptType() {
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public static AcceptType newInstance(String strIn) {
        Double q;
        AcceptType out = null;
        MediaType mtIn;
        MediaType mtOut = new MediaType();
        List<String> acExtList = new ArrayList<String>();
        String curParameter;
        int i;

        if (strIn == null) {
            return null;
        }
        mtIn = MediaType.newInstance(strIn);
        if (mtIn == null) {
            return out;
        }
        out = new AcceptType();
        out.setMediaType(mtOut);
        out.setAcceptExtensions(acExtList);
        mtOut.setType(mtIn.getType());
        mtOut.setSubtype(mtIn.getSubtype());
        i = 0;
        for (i = 0; i < mtIn.getParameters().size(); i++) {
            curParameter = mtIn.getParameters().get(i);
            q = AcceptType.getQfromString(curParameter);
            if (q != null) {
                out.setQ(q);
                i++;
                break;
            }
            mtOut.getParameters().add(curParameter);
        }

        for (; i < mtIn.getParameters().size(); i++) {
            acExtList.add(mtIn.getParameters().get(i));
        }
        return out;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(String.format("%s/%s", this.getMediaType().getType(), this.getMediaType().getSubtype()));
            for (String parameter : this.getMediaType().getParameters()) {
                sb.append(String.format(";%s", parameter));
            }
            sb.append(String.format(";q=%.2f", this.getQ()));
            for (String ext : this.getAcceptExtensions()) {
                sb.append(String.format(";%s", ext));
            }
        } catch (NullPointerException ex) {
            return null;
        }
        return sb.toString();
    }

    public static boolean isAcceptableMedia(AcceptType requested, AcceptType offerd) {
        String rType = requested.getMediaType().getType().toLowerCase();
        String oType = offerd.getMediaType().getType().toLowerCase();
        String rSub = requested.getMediaType().getSubtype();
        String oSub = offerd.getMediaType().getSubtype();

        if (rType.equals("*")) {
            return true;
        }

        if (rType.equals(oType) && rSub.equals("*")) {
            return true;
        }

        if (rType.equals(oType) && rSub.equals(oSub)) {
            return true;
        }
        return false;
    }

    public static boolean mediaMatch(AcceptType t, AcceptType o) {
        try {
            String ttype = t.getMediaType().getType();
            String otype = o.getMediaType().getType();
            String tsub = t.getMediaType().getSubtype();
            String osub = o.getMediaType().getSubtype();
            List<String> tparams = t.getMediaType().getParameters();
            List<String> oparams = t.getMediaType().getParameters();
            int i;
            if (!ttype.equals(otype)) {
                return false;
            }
            if (!tsub.equals(osub)) {
                return false;
            }
            if (tparams.size() != oparams.size()) {
                return false;
            }
            for (i = 0; i < oparams.size(); i++) {
                if (!tparams.get(i).equals(oparams.get(i))) {
                    return false;
                }
            }
            return true;
        } catch (NullPointerException ex) {
            return false;
        }
    }

    @Override
    public int compareTo(AcceptType o) {
        // Sorts the AcceptType according to according to HttpClient preferences
        // According to Section 14.1 of the Http/1.1 protocol
        double oq = o.getQ();
        double mq = this.getQ();
        String otype = o.getMediaType().getSubtype();
        String osubtype = o.getMediaType().getSubtype();
        String mtype = this.getMediaType().getType();
        String msubtype = this.getMediaType().getSubtype();
        int mscore = 0;
        int oscore = 0;

        // If the q values are different choose the highest one
        if (mq > oq) {
            return -1;
        }
        if (mq < oq) {
            return 1;
        }

        // Try to tie break off * protocols since * should have less presedence
        // type is worth 2 points subtype is just worth 1

        mscore = ((msubtype.equals("*")) ? 0 : 1) + ((mtype.equals("*")) ? 0 : 2);
        oscore = ((osubtype.equals("*")) ? 0 : 1) + ((otype.equals("*")) ? 0 : 2);
        if (mscore != oscore) {
            return oscore - mscore;
        }

        //If you still have a tie the go by the number of parameters since they are
        //more specific as suggested by the HTTP/1.1 section 14.1

        oscore = o.getMediaType().getParameters().size();
        mscore = this.getMediaType().getParameters().size();
        return oscore - mscore;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public double getQ() {
        return q;
    }

    public void setQ(double q) {
        this.q = q;
    }

    public List<String> getAcceptExtensions() {
        return acceptExtensions;
    }

    public void setAcceptExtensions(List<String> acceptExtensions) {
        this.acceptExtensions = acceptExtensions;
    }

    public static Double getQfromString(String strIn) {
        Matcher matcher = qRe.matcher(strIn);
        if (!matcher.find()) {
            return null;
        }
        return Double.parseDouble(matcher.group(1));
    }

}
