package org.openstack.atlas.service.domain.pojos;

import java.util.ArrayList;
import java.util.List;

public class NodeStatusReport {


    private int canonicalIpErrors = 0;
    private int threadErrors = 0;
    private int ipTypeErrors = 0;
    private int nodesUpdated = 0;
    private int turnedOnline = 0;
    private int turnedOffline = 0;
    private int noDeltas = 0;
    private int notInDb = 0;
    private int unknownStatus = 0;
    private int snmpKeyMissMatch = 0;
    private List<Integer> turnOffline;
    private List<Integer> turnOnline;


    public void incSnmpKeyMissMatch() {
        snmpKeyMissMatch++;
    }

    public void incNoDeltas() {
        noDeltas++;
    }

    public void incUnknownStatus() {
        unknownStatus++;
    }

    public void incCanonicalIpErrors() {
        canonicalIpErrors++;
    }

    public void incThreadErrors() {
        threadErrors++;
    }

    public void incIpTypeError() {
        ipTypeErrors++;
    }

    public void incNodesUpdated() {
        nodesUpdated++;
    }

    public void incNotInDB() {
        notInDb++;
    }

    public void incTurnedOnline() {
        turnedOnline++;
    }

    public void incTurnedOffline() {
        turnedOffline++;
    }

    public int getCanonicalIpErrors() {
        return canonicalIpErrors;
    }

    public void setCanonicalIpErrors(int canonicalIpErrors) {
        this.canonicalIpErrors = canonicalIpErrors;
    }

    public int getThreadErrors() {
        return threadErrors;
    }

    public void setThreadErrors(int threadErrors) {
        this.threadErrors = threadErrors;
    }

    public int getIpTypeErrors() {
        return ipTypeErrors;
    }

    public void setIpTypeErrors(int ipTypeErrors) {
        this.ipTypeErrors = ipTypeErrors;
    }

    public int getNodesUpdated() {
        return nodesUpdated;
    }

    public void setNodesUpdated(int nodesUpdated) {
        this.nodesUpdated = nodesUpdated;
    }

    public int getNotInDb() {
        return notInDb;
    }

    public void setNotInDb(int notInDb) {
        this.notInDb = notInDb;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NodeStatusReport other = (NodeStatusReport) obj;
        if (this.canonicalIpErrors != other.canonicalIpErrors) {
            return false;
        }
        if (this.threadErrors != other.threadErrors) {
            return false;
        }
        if (this.ipTypeErrors != other.ipTypeErrors) {
            return false;
        }
        if (this.nodesUpdated != other.nodesUpdated) {
            return false;
        }
        if (this.notInDb != other.notInDb) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + this.canonicalIpErrors;
        hash = 79 * hash + this.threadErrors;
        hash = 79 * hash + this.ipTypeErrors;
        hash = 79 * hash + this.nodesUpdated;
        return hash;
    }

    public int getTurnedOnline() {
        return turnedOnline;
    }

    public void setTurnedOnline(int turnedOnline) {
        this.turnedOnline = turnedOnline;
    }

    public int getTurnedOffline() {
        return turnedOffline;
    }

    public void setTurnedOffline(int turnedOffline) {
        this.turnedOffline = turnedOffline;
    }

    public int getUnknownStatus() {
        return unknownStatus;
    }

    public void setUnknownStatus(int unknownStatus) {
        this.unknownStatus = unknownStatus;
    }

    public int getNoDeltas() {
        return noDeltas;
    }

    public void setNoDeltas(int noDeltas) {
        this.noDeltas = noDeltas;
    }

    public int getSnmpKeyMissMatch() {
        return snmpKeyMissMatch;
    }

    public void setSnmpKeyMissMatch(int snmpKeyMissMatch) {
        this.snmpKeyMissMatch = snmpKeyMissMatch;
    }

    @Override
    public String toString() {
        return "{" + "canonicalIpErrors=" + canonicalIpErrors
                + ",threadErrors=" + threadErrors + ",ipTypeErrors="
                + ipTypeErrors + ",nodesUpdated=" + nodesUpdated
                + ",turnedOnline=" + turnedOnline + ",turnedOffline="
                + turnedOffline + ",noDeltas=" + noDeltas + ",notInDb="
                + notInDb + ",unknownStatus=" + unknownStatus
                + ",snmpKeyMissMatch=" + snmpKeyMissMatch + '}';
    }

    public List<Integer> getTurnOffline() {
        if(turnOffline == null){
            this.turnOffline = new ArrayList<Integer>();
        }
        return turnOffline;
    }

    public void setTurnOffline(List<Integer> turnOffline) {
        this.turnOffline = turnOffline;
    }

    public List<Integer> getTurnOnline() {
        if(turnOnline == null){
            this.turnOnline = new ArrayList<Integer>();
        }
        return turnOnline;
    }

    public void setTurnOnline(List<Integer> turnOnline) {
        this.turnOnline = turnOnline;
    }
}
