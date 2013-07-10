package org.openstack.atlas.usagerefactor.generator;

import java.util.Random;

public class GeneratorPojo {
    private int accountId;
    private int loadbalancerId;
    private int numRecords;

    public int getAccountId() {
        return this.accountId;
    }

    public int getLoadbalancerId() {
        return this.loadbalancerId;
    }

    public int getNumRecords() {
        return this.numRecords;
    }

    public GeneratorPojo(int accountId, int loadbalancerId, int numRecords) {
        this.accountId = accountId;
        this.loadbalancerId = loadbalancerId;
        this.numRecords = numRecords;
    }

    public GeneratorPojo(int accountId, int loadbalancerId, int randNumRecordsFloor, int randNumRecordsCeiling) {
        this.accountId = accountId;
        this.loadbalancerId = loadbalancerId;
        this.numRecords = randInt(randNumRecordsFloor, randNumRecordsCeiling);
    }

    private int randInt(int floor, int ceiling) {
        Random rand = new Random();
        return rand.nextInt(ceiling) + floor;
    }
}