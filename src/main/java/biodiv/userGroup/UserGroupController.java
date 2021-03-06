package biodiv.userGroup;

import biodiv.Transactional;
import biodiv.auth.AuthUtils;
import biodiv.user.User;
import biodiv.util.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.jax.rs.annotations.Pac4JSecurity;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.ListIterator;

@Path("/userGroup")
public class UserGroupController {
	
	@Inject
	UserGroupService userGroupService;

	@Context
	private ResourceContext resourceContext;

	@GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public List<UserGroup> list(@QueryParam("max") int max, @QueryParam("offset") int offset) {
        return getUserList(max, offset);
    }

    @GET
    @Path("/minimalList")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public String minimalList(@QueryParam("max") int max, @QueryParam("offset") int offset) {
        List<UserGroup> usrGrp = null;
        JSONArray mUserGroup = new JSONArray();
        ListIterator<UserGroup> it = getUserList(max, offset).listIterator();
        while (it.hasNext()) {
            UserGroup t = it.next();
            if (!t.isIsDeleted()) {
                JSONObject jo = new JSONObject();
                jo.put("id", t.getId());
                jo.put("name", t.getName());
                jo.put("domainName", t.getDomainName());
                jo.put("webaddress", t.getWebaddress());
                jo.put("icon", t.getIcon());
                mUserGroup.put(jo);
            }
        }
        return mUserGroup.toString();
    }

    public List<UserGroup> getUserList(int max, int offset) {
        if (max == 0 && offset == 0) {
            return userGroupService.findAll();
        } else if (max != 0 && offset == 0) {
            return userGroupService.findAll(max, 0);
        }
        return userGroupService.findAll(max, offset);
    }

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public UserGroup show(@PathParam("id") long id) {
		UserGroup usrGrp = userGroupService.findById(Long.valueOf(id));
		return usrGrp;
	}

	@GET
	@Path("/find/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public UserGroup show(@PathParam("name") String name) {
		UserGroup usrGrp = userGroupService.findByName(name);
		return usrGrp;
	}

	@GET
	@Path("/{groupId}/users/{roleId}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<User> listofUsers(@PathParam("groupId") long groupId, @PathParam("roleId") long roleId) {
		List<User> users = userGroupService.userList(groupId, roleId);
		return users;
	}

	@POST
	@Path("/bulkPost")
	@Produces(MediaType.APPLICATION_JSON)
	@Pac4JSecurity(clients = "cookieClient,headerClient", authorizers = "isAuthenticated")
	@Transactional
	public Response bulkPost(@QueryParam("pullType") String pullType, @QueryParam("selectionType") String selectionType,
			@QueryParam("objectType") String objectType, @QueryParam("objectIds") String objectIds,
			@QueryParam("submitType") String submitType, @QueryParam("userGroups") String userGroups,
			@QueryParam("filterUrl") String filterUrl, @Context HttpServletRequest request)
			throws NumberFormatException, Exception {
		CommonProfile profile = AuthUtils.currentUser(request);
		String msg = userGroupService.posttoGroups(objectType, pullType, submitType, objectIds, userGroups,
				Long.parseLong(profile.getId()), filterUrl);
		return Utils.toJSONResponse(msg);
	}

	@Path("/{groupName}/{x}")
	@Produces(MediaType.APPLICATION_JSON)
	public Object mapfunc(@PathParam("x") String x) {
		Mapping m = new Mapping(x);

		return m.getObject();
	}

}
