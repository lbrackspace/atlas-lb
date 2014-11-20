package org.openstack.atlas.logs.hadoop.util;

public class DeleteDirectoryResponse {

    private String directory = null;
    private Boolean status = null;
    private Throwable exception = null;

    public DeleteDirectoryResponse(String dir, Boolean status, Throwable ex) {
        this.directory = dir;
        this.status = status;
        this.exception = ex;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ dir = ").append((directory == null) ? "null" : directory).
                append(" status=").append((status == null) ? "null" : status).
                append(" exception=").append((exception == null) ? null : exception.toString()).
                append("}");
        return sb.toString();
    }
}
