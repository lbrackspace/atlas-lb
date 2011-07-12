package org.openstack.atlas.api.filters.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;

public class XmlValidationExceptionHandler implements ValidationEventHandler {
    List<String> errList;
    static final Pattern cvsRe = Pattern.compile("^([^:]*):(.*)$");

    public XmlValidationExceptionHandler() {
        errList = new ArrayList<String>();
    }

    @Override
    public boolean handleEvent(ValidationEvent ve) {
        String errFormat = "Message is %s Column is %d at line number %d\n";
        ValidationEventLocator l;
        int lineNum;
        int colNum;
        String msg;
        if (ve.getSeverity() == ve.FATAL_ERROR
                || ve.getSeverity() == ve.ERROR) {
            l = ve.getLocator();
            lineNum = l.getLineNumber();
            colNum = l.getColumnNumber();
            msg = ve.getMessage();
            Matcher cvsMatcher = cvsRe.matcher(msg);
            if(cvsMatcher.find()) {
                msg = cvsMatcher.group(2).trim();
            }

            errList.add(msg);
        }
        return true;
    }

    public List<String> getErrList() {
        return errList;
    }

    public void setErrList(List<String> errList) {
        this.errList = errList;
    }

}
