package biodiv.userGroup;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/newsletters")
public class NewsletterController {

	@Inject
	NewsletterService newsletterService;

	@GET
	@Path("/pages")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Map<String, Object>> getPages(@QueryParam("userGroupId") Long ugId, @QueryParam("max") Long max,
			@QueryParam("offset") Long offset, @QueryParam("sort") String sort, @QueryParam("order") String order,
			@QueryParam("currentLanguage") String currentLanguage, @QueryParam("date") Date date,
			@QueryParam("newsitem") String newsitem, @QueryParam("title") String title,
			@QueryParam("displayOrder") Integer displayOrder, @QueryParam("parentId") Long parentId,
			@QueryParam("showInFooter") Boolean showInFooter, @QueryParam("userId") Long userId) {
		System.out.println("getPages");
		Map<String, Object> filterParams = new HashMap<String, Object>();
		filterParams.put("date", date);
		filterParams.put("newsitem", newsitem);
		filterParams.put("title", title);
		filterParams.put("displayOrder", displayOrder);
		filterParams.put("parentId", parentId);
		// filterParams.put("showInFooter", showInFooter);
		List<Map<String, Object>> nl = newsletterService.getPages(ugId, max, offset, sort, order, currentLanguage,
				filterParams, userId, showInFooter);
		return nl;
	}
}
