package pl.allegro.tech.hermes.management.api;

import com.google.common.collect.ImmutableMap;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.allegro.tech.hermes.api.Group;
import pl.allegro.tech.hermes.management.api.auth.Roles;
import pl.allegro.tech.hermes.management.api.validator.ApiPreconditions;
import pl.allegro.tech.hermes.management.domain.group.GroupService;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Component
@Path("/groups")
@Api(value = "/groups", description = "Operations on groups")
public class GroupsEndpoint {

    public static final String PASSWORD_KEY = "groupPassword";

    private final GroupService groupService;

    private final ApiPreconditions preconditions;

    @Autowired
    public GroupsEndpoint(GroupService groupService, ApiPreconditions preconditions) {
        this.groupService = groupService;
        this.preconditions = preconditions;
    }

    @GET
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "List groups", response = List.class, httpMethod = HttpMethod.GET)
    public List<String> list() {
        return groupService.listGroups();
    }


    @GET
    @Produces(APPLICATION_JSON)
    @Path("/{groupName}")
    @ApiOperation(value = "Get group details", response = Group.class, httpMethod = HttpMethod.GET)
    public Group get(@PathParam("groupName") String groupName) {
        return groupService.getGroupDetails(groupName);
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Create group", response = String.class, httpMethod = HttpMethod.POST)
    @RolesAllowed(Roles.ADMIN)
    public Response create(Group group) {
        preconditions.checkConstraints(group);
        return passwordResponse(groupService.createGroup(group));
    }

    @PUT
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Path("/{groupName}")
    @ApiOperation(value = "Update group", response = String.class, httpMethod = HttpMethod.PUT)
    @RolesAllowed(Roles.ADMIN)
    public Response update(@PathParam("groupName") String groupName, Group group) {
        groupService.updateGroup(Group.Builder.group().applyPatch(group).withGroupName(groupName).build());
        return responseStatus(Response.Status.NO_CONTENT);
    }

    @DELETE
    @Path("/{groupName}")
    @ApiOperation(value = "Remove group", response = String.class, httpMethod = HttpMethod.DELETE)
    @RolesAllowed(Roles.ADMIN)
    public Response delete(@PathParam("groupName") String groupName) {
        groupService.removeGroup(groupName);
        return responseStatus(Response.Status.OK);
    }

    private Response passwordResponse(String password) {
        return Response.status(Response.Status.CREATED).entity(ImmutableMap.of(PASSWORD_KEY, password)).build();
    }

    private Response responseStatus(Response.Status responseStatus) {
        return Response.status(responseStatus).build();
    }
}
