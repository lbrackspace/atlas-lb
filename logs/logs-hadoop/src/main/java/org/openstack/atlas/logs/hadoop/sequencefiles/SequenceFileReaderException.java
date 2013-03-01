package org.openstack.atlas.logs.hadoop.sequencefiles;

public class SequenceFileReaderException extends Exception {

    public SequenceFileReaderException(Throwable cause) {
        super(cause);
    }

    public SequenceFileReaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public SequenceFileReaderException(String message) {
        super(message);
    }

    public SequenceFileReaderException() {
        super();
    }
}
