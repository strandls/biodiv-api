package biodiv.observation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.jax.rs.annotations.Pac4JSecurity;

import biodiv.Transactional;
import biodiv.auth.AuthUtils;
import biodiv.maps.MapAggregationResponse;
import biodiv.maps.MapBiodivResponse;
import biodiv.maps.MapBoundParams;
import biodiv.maps.MapBounds;
import biodiv.maps.MapGeoPoint;
import biodiv.maps.MapHttpResponse;
import biodiv.maps.MapResponse;
import biodiv.maps.MapSearchParams;
import biodiv.maps.MapSearchQuery;
import biodiv.maps.MapSortType;
import biodiv.scheduler.SchedulerService;
import biodiv.scheduler.SchedulerStatus;
import biodiv.user.User;
import biodiv.user.UserService;

@Path("/naksha")
public class ObservationListController {

	@Inject
	private ObservationListService observationListService;
	@Inject
	UserService userService;
	@Inject
	SchedulerService schedulerService;
	
	@POST
	@Path("/{index}/{type}/{documentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public MapResponse create(@PathParam("index") String index, @PathParam("type") String type,
			@PathParam("documentId") String documentId, String document) {

		return observationListService.create(index, type, documentId, document);
	}

	@DELETE
	@Path("/{index}/{type}/{documentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public MapResponse delete(@PathParam("index") String index, @PathParam("type") String type,
			@PathParam("documentId") String documentId) {
		return observationListService.delete(index, type, documentId);
	}

	@PUT
	@Path("/{index}/{type}/{documentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public MapResponse update(@PathParam("index") String index, @PathParam("type") String type,
			@PathParam("documentId") String documentId, String document) {

		return observationListService.update(index, type, documentId, document);
	}

	@GET
	@Path("/search/{index}/{type}/{documentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public MapHttpResponse list(@PathParam("index") String index, @PathParam("type") String type,
			@PathParam("documentId") String documentId) {

		MapHttpResponse content = observationListService.fetch(index, type, documentId);
		return content;
	}
	
	@GET
	@Path("/aggregation/{index}/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public MapAggregationResponse aggregation(@PathParam("index") String index, @PathParam("type") String type,
			@DefaultValue("") @QueryParam("sGroup") String sGroup, @DefaultValue("") @QueryParam("taxon") String taxon,
			@DefaultValue("") @QueryParam("user") String user,
			@DefaultValue("") @QueryParam("userGroupList") String userGroupList,
			@DefaultValue("") @QueryParam("webaddress") String webaddress,
			@DefaultValue("") @QueryParam("speciesName") String speciesName,
			@DefaultValue("") @QueryParam("mediaFilter") String mediaFilter,
			@DefaultValue("") @QueryParam("months") String months,
			@DefaultValue("") @QueryParam("isFlagged") String isFlagged, @QueryParam("location") String location,
			@DefaultValue("lastrevised") @QueryParam("sort") String sortOn, @QueryParam("minDate") String minDate,
			@QueryParam("maxDate") String maxDate, @QueryParam("createdOnMaxDate") String createdOnMaxDate,
			@QueryParam("createdOnMinDate") String createdOnMinDate, @QueryParam("status") String status,
			@QueryParam("taxonId") String taxonId, @QueryParam("validate") String validate,
			@QueryParam("recoName") String recoName,
			@DefaultValue("265799") @QueryParam("classifdication") String classificationid,
			@DefaultValue("10") @QueryParam("max") Integer max, @DefaultValue("0") @QueryParam("offset") Integer offset,
			@DefaultValue("location") @QueryParam("geoAggregationField") String geoAggregationField,
			@DefaultValue("1") @QueryParam("geoAggegationPrecision") Integer geoAggegationPrecision,
			@QueryParam("left") Double left, @QueryParam("right") Double right, @QueryParam("top") Double top,
			@QueryParam("bottom") Double bottom, @QueryParam("recom") String maxvotedrecoid,
			@QueryParam("onlyFilteredAggregation") Boolean onlyFilteredAggregation,
			@QueryParam("termsAggregationField") String termsAggregationField, @Context UriInfo uriInfo,
			String allParams) {

		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		// System.out.println(queryParams.get);
		Map<String, List<String>> traitParams = queryParams.entrySet().stream()
				.filter(entry -> entry.getKey().startsWith("trait"))
				.collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

		Map<String, List<String>> customParams = queryParams.entrySet().stream()
				.filter(entry -> entry.getKey().startsWith("custom"))
				.collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

		MapSortType sortType = null;
		MapBounds bounds = null;
		if (top != null || bottom != null || left != null || right != null)
			bounds = new MapBounds(top, left, bottom, right);
		List<MapGeoPoint> polygon = new ArrayList<MapGeoPoint>();
		if (location != null) {
			double[] point = Stream.of(location.split(",")).mapToDouble(Double::parseDouble).toArray();
			for (int i = 0; i < point.length; i = i + 2) {
				String singlePoint = point[i + 1] + "," + point[i];
				polygon.add(new MapGeoPoint(singlePoint));
			}
		}

		MapBoundParams mapBoundsParams = new MapBoundParams(bounds, polygon);

		MapSearchParams mapSearchParams = new MapSearchParams(offset, max, sortOn.toLowerCase(), sortType.DESC,
				mapBoundsParams);
		
		return observationListService.mapAggregate(index, type, sGroup, taxon, user, userGroupList, webaddress,
				speciesName, mediaFilter, months, isFlagged, minDate, maxDate, validate, traitParams, customParams,
				classificationid, mapSearchParams, maxvotedrecoid, createdOnMaxDate, createdOnMinDate, status, taxonId,
				recoName,geoAggregationField);
	}
	
	@GET
	@Path("/search/{index}/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	public MapBiodivResponse list(@PathParam("index") String index, @PathParam("type") String type,
			@DefaultValue("") @QueryParam("sGroup") String sGroup, @DefaultValue("") @QueryParam("taxon") String taxon,
			@DefaultValue("") @QueryParam("user") String user,
			@DefaultValue("") @QueryParam("userGroupList") String userGroupList,
			@DefaultValue("") @QueryParam("webaddress") String webaddress,
			@DefaultValue("") @QueryParam("speciesName") String speciesName,
			@DefaultValue("") @QueryParam("mediaFilter") String mediaFilter,
			@DefaultValue("") @QueryParam("months") String months,
			@DefaultValue("") @QueryParam("isFlagged") String isFlagged,
			@QueryParam("location") String location,
			@DefaultValue("lastrevised") @QueryParam("sort") String sortOn, @QueryParam("minDate") String minDate,
			@QueryParam("maxDate") String maxDate, @QueryParam("createdOnMaxDate") String createdOnMaxDate,
			@QueryParam("createdOnMinDate") String createdOnMinDate, @QueryParam("status") String status,
			@QueryParam("taxonId") String taxonId, @QueryParam("validate") String validate,
			@QueryParam("recoName") String recoName,
			@DefaultValue("265799") @QueryParam("classifdication") String classificationid,
			@DefaultValue("10") @QueryParam("max") Integer max, @DefaultValue("0") @QueryParam("offset") Integer offset,
			@DefaultValue("location") @QueryParam("geoAggregationField") String geoAggregationField,
			@DefaultValue("1") @QueryParam("geoAggegationPrecision") Integer geoAggegationPrecision,
			@QueryParam("left") Double left, @QueryParam("right") Double right, @QueryParam("top") Double top,
			@QueryParam("bottom") Double bottom, @QueryParam("recom") String maxvotedrecoid,
			@QueryParam("onlyFilteredAggregation") Boolean onlyFilteredAggregation,
			@QueryParam("termsAggregationField") String termsAggregationField,
			@Context UriInfo uriInfo, String allParams

	) {
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		// System.out.println(queryParams.get);
		Map<String, List<String>> traitParams = queryParams.entrySet().stream()
				.filter(entry -> entry.getKey().startsWith("trait"))
				.collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

		Map<String, List<String>> customParams = queryParams.entrySet().stream()
				.filter(entry -> entry.getKey().startsWith("custom"))
				.collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

		MapSortType sortType = null;
		MapBounds bounds = null;
		if(top != null || bottom != null || left != null || right != null)
			bounds = new MapBounds(top, left, bottom, right);
		List<MapGeoPoint> polygon = new ArrayList<MapGeoPoint>();
		if(location!=null){
			double[] point=Stream.of(location.split(",")).mapToDouble(Double::parseDouble).toArray();
			for(int i=0;i<point.length;i=i+2){
				String singlePoint=point[i+1]+","+point[i];
				polygon.add(new MapGeoPoint(singlePoint));
			}
		}
		
		
		MapBoundParams mapBoundsParams = new MapBoundParams(bounds, polygon);

		MapSearchParams mapSearchParams = new MapSearchParams(offset, max, sortOn.toLowerCase(), sortType.DESC,
				mapBoundsParams);

		MapSearchQuery mapSearchQuery = ObservationControllerHelper.getMapSearchQuery(sGroup, taxon, user,
				userGroupList, webaddress, speciesName, mediaFilter, months, isFlagged, minDate, maxDate, validate,
				traitParams, customParams, classificationid, mapSearchParams, maxvotedrecoid, createdOnMaxDate,
				createdOnMinDate, status, taxonId, recoName);
	

		MapBiodivResponse mapResponse = observationListService.search(index, type, mapSearchQuery, geoAggregationField,
				geoAggegationPrecision, onlyFilteredAggregation, termsAggregationField);

		return mapResponse;
	}

	@GET
	@Path("/download/{index}/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	@Pac4JSecurity(clients = "cookieClient,headerClient", authorizers = "isAuthenticated")
	@Transactional
	public SchedulerStatus download(@PathParam("index") String index, @PathParam("type") String type,
			@DefaultValue("") @QueryParam("sGroup") String sGroup, @DefaultValue("") @QueryParam("taxon") String taxon,
			@DefaultValue("") @QueryParam("user") String user,
			@DefaultValue("") @QueryParam("userGroupList") String userGroupList,
			@DefaultValue("") @QueryParam("webaddress") String webaddress,
			@DefaultValue("") @QueryParam("speciesName") String speciesName,
			@DefaultValue("") @QueryParam("mediaFilter") String mediaFilter,
			@DefaultValue("") @QueryParam("months") String months,
			@DefaultValue("") @QueryParam("isFlagged") String isFlagged,
			@DefaultValue("lastrevised") @QueryParam("sort") String sortOn,
			@QueryParam("minDate") String minDate,
			@QueryParam("maxDate") String maxDate, 
			@QueryParam("createdOnMaxDate") String createdOnMaxDate,
			@QueryParam("createdOnMinDate") String createdOnMinDate,
			@QueryParam("status") String status,
			@QueryParam("taxonId") String taxonId, 
			@QueryParam("validate") String validate,
			@QueryParam("recoName") String recoName,
			@DefaultValue("1") @QueryParam("minDay") Integer minDay,
			@DefaultValue("31") @QueryParam("maxDay") Integer maxDay,
			@DefaultValue("265799") @QueryParam("classifdication") String classificationid,
			@DefaultValue("10") @QueryParam("max") Integer max,
			@DefaultValue("0") @QueryParam("offset") Integer offset,
			@QueryParam("notes") String notes, 
			@DefaultValue("location") @QueryParam("geoField") String geoField, 
			@QueryParam("top") Double top,
			@QueryParam("bottom") Double bottom,
			@QueryParam("left") Double left,
			@QueryParam("right") Double right,
			@QueryParam("location") String location,
			@QueryParam("recom") String maxvotedrecoid,
			@QueryParam("onlyFilteredAggregation") Boolean onlyFilteredAggregation,
			@QueryParam("termsAggregationField") String termsAggregationField,@Context HttpServletRequest request, @Context UriInfo uriInfo,
			String allParams

	) {

		CommonProfile profile = AuthUtils.currentUser(request);
		User suser = userService.findById(Long.parseLong(profile.getId()));
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		// System.out.println(queryParams.get);
		Map<String, List<String>> traitParams = queryParams.entrySet().stream()
				.filter(entry -> entry.getKey().startsWith("trait"))
				.collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
		Map<String, List<String>> customParams = queryParams.entrySet().stream()
				.filter(entry -> entry.getKey().startsWith("custom"))
				.collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

		MapSortType sortType = null;
		MapBounds bounds = null;
        if (top != null || bottom != null || left != null || right != null)
                bounds = new MapBounds(top, left, bottom, right);
        List<MapGeoPoint> polygon = new ArrayList<MapGeoPoint>();
		
		if(location!=null){
			double[] point=Stream.of(location.split(",")).mapToDouble(Double::parseDouble).toArray();
			for(int i=0;i<point.length;i=i+2){
				String singlePoint=point[i+1]+","+point[i];
				polygon.add(new MapGeoPoint(singlePoint));
			}
		}
		
		MapBoundParams mapBoundsParams = new MapBoundParams(bounds, polygon);

		MapSearchParams mapSearchParams = new MapSearchParams(0, 1000000, sortOn.toLowerCase(), sortType.DESC,
				mapBoundsParams);
		MapSearchQuery mapSearchQuery = ObservationControllerHelper.getMapSearchQuery(sGroup, taxon, user,
				userGroupList, webaddress, speciesName, mediaFilter, months, isFlagged, minDate, maxDate, validate,
				traitParams, customParams, classificationid, mapSearchParams, maxvotedrecoid, createdOnMaxDate,
				createdOnMinDate, status, taxonId, recoName);
	
		
		
		return schedulerService.scheduleNow(index, type, suser, mapSearchQuery, geoField, notes, getFullURL(request));
	}

	private static String getFullURL(HttpServletRequest request) {
		StringBuffer requestURL = request.getRequestURL();
		String queryString = request.getQueryString();

		if (queryString == null) {
			return requestURL.toString();
		} else {
			return requestURL.append('?').append(queryString).toString();
		}
	}
}
