package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.adapter.exceptions.VTMRollBackException;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

public class BackupResource extends ManagementDependencyProvider {
    private static Log LOG = LogFactory.getLog(BackupResource.class.getName());
    private int hostId;
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;

    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }

    public int getHostId() {
        return hostId;
    }

    @DELETE
    public Response deleteBackup() {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }

        try {
            org.openstack.atlas.service.domain.entities.Host domainHost = hostService.getById(hostId);
            org.openstack.atlas.service.domain.entities.Backup domainBackup = hostService.getBackupByHostIdAndBackupId(hostId, id);

            if (!hostService.isActiveHost(domainHost)) {
                String message = String.format("Host %d is currently immutable. Canceling delete backup request...", domainHost.getId());
                LOG.warn(message);
                throw new ImmutableEntityException(message);
            }
            try {
                LOG.debug("Deleting backup in Traffic Manager...");
                reverseProxyLoadBalancerVTMService.deleteHostBackup(domainHost, domainBackup.getName());
                LOG.info("Backup successfully deleted backend...");
            } catch (VTMRollBackException oae) {
                String message = String.format("Unable to create backup %s", oae.getMessage());
                LOG.warn(message);
                throw new BadRequestException(message);
            }

            LOG.debug("Removing the backup from the database...");
            hostService.deleteBackup(domainBackup);

            LOG.info("Delete backup operation complete.");
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @Path("restore")
    @PUT
    public Response restoreBackup() {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }

        try {
            org.openstack.atlas.service.domain.entities.Host domainHost = hostService.getById(hostId);
            org.openstack.atlas.service.domain.entities.Backup domainBackup = hostService.getBackupByHostIdAndBackupId(hostId, id);

            if (!hostService.isActiveHost(domainHost)) {
                String message = String.format("Host %d is currently immutable. Canceling delete backup request...", domainHost.getId());
                LOG.warn(message);
                throw new ImmutableEntityException(message);
            }

            try {
                LOG.info(String.format("Restoring host with backup '%s' in Traffic Manager...", domainBackup.getName()));
                reverseProxyLoadBalancerVTMService.restoreHostBackup(domainHost, domainBackup.getName());
                LOG.info(String.format("Host successfully restored with backup '%s' backend...", domainBackup.getName()));
            } catch (Exception e) {
                if (e.getMessage().contains("Backup resource not found")) {
                    String message = String.format("A backup named '%s' does not exist backend... Cannot restore host!", domainBackup.getName());
                    LOG.error(message);
                    notificationService.saveAlert(e, AlertType.ZEUS_FAILURE.name(), message);
                    throw new EntityNotFoundException(message);
                }
                String error = "Error during restore backup.";
                LOG.error(error, e);
                notificationService.saveAlert(e, AlertType.ZEUS_FAILURE.name(), error);
                throw e;
            }

            LOG.info("Restore with backup operation complete.");
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

}
