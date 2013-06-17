package org.openstack.atlas.logs.hadoop.counters;

public enum LogCounters {

    MAPPER_CALLS,
    MAPPER_WRITES,
    MAPPER_SETUP_CALLS,
    REDUCER_SETUP_CALLS,
    REDUCER_CALLS,
    REDUCER_WRITES,
    REDUCER_REDUCTIONS,
    BAD_LOG_DATE,
    BAD_LOG_STRING,
    LOG_BYTE_COUNT;
}
