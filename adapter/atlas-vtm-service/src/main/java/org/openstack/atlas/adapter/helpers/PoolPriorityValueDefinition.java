package org.openstack.atlas.adapter.helpers;

import java.io.Serializable;

public class PoolPriorityValueDefinition implements Serializable {
    private String node;
    private int priority;
    private Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public PoolPriorityValueDefinition() {
    }

    public PoolPriorityValueDefinition(String node, int priority) {
        this.node = node;
        this.priority = priority;
    }

    public String getNode() {
        return this.node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof PoolPriorityValueDefinition)) {
            return false;
        } else {
            PoolPriorityValueDefinition other = (PoolPriorityValueDefinition)obj;
            if (obj == null) {
                return false;
            } else if (this == obj) {
                return true;
            } else if (this.__equalsCalc != null) {
                return this.__equalsCalc == obj;
            } else {
                this.__equalsCalc = obj;
                boolean _equals = (this.node == null && other.getNode() == null || this.node != null && this.node.equals(other.getNode())) && this.priority == other.getPriority();
                this.__equalsCalc = null;
                return _equals;
            }
        }
    }

    public synchronized int hashCode() {
        if (this.__hashCodeCalc) {
            return 0;
        } else {
            this.__hashCodeCalc = true;
            int _hashCode = 1;
            if (this.getNode() != null) {
                _hashCode += this.getNode().hashCode();
            }

            _hashCode += this.getPriority();
            this.__hashCodeCalc = false;
            return _hashCode;
        }
    }
}