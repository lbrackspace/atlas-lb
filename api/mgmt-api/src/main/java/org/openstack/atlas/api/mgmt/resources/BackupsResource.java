package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Backup;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Backups;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.openstack.atlas.api.faults.HttpResponseBuilder;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.repository.ValidatorRepository;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import com.zxtm.service.client.InvalidObjectName;
import com.zxtm.service.client.ObjectAlreadyExists;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;

public class BackupsResource extends ManagementDependencyProvider {
    private static Log LOG = LogFactory.getLog(BackupsResource.class.getName());
    private BackupResource backupResource;
    private int hostId;

    @GET
    public Response retrieveBackups(@QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit) {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }
        List<org.openstack.atlas.service.domain.entities.Backup> domainBackups;
        Backups apiBackups = new Backups();
        try {
            domainBackups = getHostRepository().getBackupsForHost(hostId, offset, limit);
            for (org.openstack.atlas.service.domain.entities.Backup domainBackup : domainBackups) {
                apiBackups.getBackups().add(getDozerMapper().map(domainBackup, Backup.class));
            }
            return Response.status(200).entity(apiBackups).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @POST
    public Response createBackup(Backup backup) {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }
        ValidatorResult result = ValidatorRepository.getValidatorFor(Backup.class).validate(backup, POST);

        if (!result.passedValidation()) {
            return Response.status(400).entity(HttpResponseBuilder.buildBadRequestResponse("Validation fault", result.getValidationErrorMessages())).build();
        }

        try {
            org.openstack.atlas.service.domain.entities.Backup domainBackup = getDozerMapper().map(backup, org.openstack.atlas.service.domain.entities.Backup.class);
            org.openstack.atlas.service.domain.entities.Host domainHost = hostService.getById(hostId);

            if (!hostService.isActiveHost(domainHost)) {
                String message = String.format("Host %d is currently immutable. Canceling create backup request...", domainHost.getId());
                LOG.warn(message);
                throw new ImmutableEntityException(message);
            }

            try {
                LOG.info("Creating backup in Zeus...");
                reverseProxyLoadBalancerService.createHostBackup(domainHost, backup.getName());
                LOG.info("Backup successfully created in Zeus.");
            } catch (ObjectAlreadyExists oae) {
                String message = String.format("A backup named '%s' already exists. Please try a different name.", backup.getName());
                LOG.warn(message);
                throw new BadRequestException(message);
            } catch (InvalidObjectName ion) {
                String message = String.format("Backup name is invalid. Please try a different name.");
                LOG.warn(message);
                throw new BadRequestException(message);
            }

            domainBackup = hostService.createBackup(domainHost, domainBackup);
            Backup apiBackup = dozerMapper.map(domainBackup, Backup.class);
            return Response.status(Response.Status.OK).entity(apiBackup).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @Path("{id: [1-9][0-9]*}")
    public BackupResource retrieveBackupResource(@PathParam("id") int id) {
        backupResource.setHostId(getHostId());
        backupResource.setId(id);
        return backupResource;
    }

    public void setBackupResource(BackupResource backupResource) {
        this.backupResource = backupResource;
    }

    public int getHostId() {
        return hostId;
    }

    public void setHostId(Integer hostId) {
        this.hostId = hostId;
    }
}
