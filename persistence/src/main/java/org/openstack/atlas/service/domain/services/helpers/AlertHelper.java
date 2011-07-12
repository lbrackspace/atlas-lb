package org.openstack.atlas.service.domain.services.helpers;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.events.entities.AlertStatus;

import java.io.PrintWriter;
import java.io.StringWriter;

public class AlertHelper {

    public static Alert createAlert(LoadBalancer lb, Exception e, String alertType, String messageName) {
        Alert alert = createAlert(lb.getAccountId(), lb.getId(), e, alertType, messageName);
        return alert;
    }

    public static Alert createAlert(Integer accountId, Integer lbId, Exception e, String alertType, String messageName) {
        Alert alert = createAlert(e, alertType, messageName);
        alert.setAccountId(accountId);
        alert.setLoadbalancerId(lbId);
        return alert;
    }

    public static Alert createAlert(Exception e, String alertType, String messageName) {
        Alert alert = new Alert();
        alert.setMessageName(messageName);
        String message = convertExceptionToString(e);
        alert.setMessage(message);
        alert.setStatus(AlertStatus.UNACKNOWLEDGED);
        alert.setAlertType(alertType);
        return alert;
    }


    private static String convertExceptionToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Throwable l = e.getCause();
        e.printStackTrace(pw);
        if (l != null) {
            pw.println("\n Caused by \n");
            l.printStackTrace(pw);
        }

        String error = sw.toString();
        return error;
    }

}
