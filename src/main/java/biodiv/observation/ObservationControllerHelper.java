package biodiv.observation;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import biodiv.common.CommonMethod;
import biodiv.maps.MapAndBoolQuery;
import biodiv.maps.MapAndMatchPhraseQuery;
import biodiv.maps.MapAndRangeQuery;
import biodiv.maps.MapExistQuery;
import biodiv.maps.MapOrBoolQuery;
import biodiv.maps.MapOrMatchPhraseQuery;
import biodiv.maps.MapOrRangeQuery;
import biodiv.maps.MapSearchParams;
import biodiv.maps.MapSearchQuery;

public class ObservationControllerHelper {

	
	public static final String custom_fields="custom_fields";
	
	public static MapSearchQuery getMapSearchQuery(
			String sGroup,
			String taxon,
			String user,
			String userGroupList,
			String webaddress,
			String speciesName,
			String mediaFilter,
			String months,
			String isFlagged,
			String minDate,
			String maxDate,
			String validate,
			Map<String, List<String>> traitParams,
			Map<String, List<String>> customParams,
			String classificationid,
			MapSearchParams mapSearchParams,
			String maxvotedrecoid,
			String createdOnMaxDate,
			String createdOnMinDate,
			String status,
			String taxonId,
			String recoName
) {
		List<MapAndBoolQuery> boolAndLists = new ArrayList<MapAndBoolQuery>();

		List<MapOrBoolQuery> boolOrLists = new ArrayList<MapOrBoolQuery>();

		List<MapOrRangeQuery> rangeOrLists = new ArrayList<MapOrRangeQuery>();
		List<MapAndRangeQuery> rangeAndLists = new ArrayList<MapAndRangeQuery>();

		List<MapExistQuery> andMapExistQueries = new ArrayList<MapExistQuery>();
		List<MapAndMatchPhraseQuery> andMatchPhraseQueries =new ArrayList<MapAndMatchPhraseQuery>();

		List<MapOrMatchPhraseQuery> orMatchPhraseQueriesnew =new ArrayList<MapOrMatchPhraseQuery>();
		
		

	
		if(classificationid == null) {
			classificationid = "265799";
		}
		
		CommonMethod commonMethod = new CommonMethod();

		Set<Object> groupId = commonMethod.cSTSOT(sGroup);
		if (!groupId.isEmpty()) {
			boolAndLists.add(new MapAndBoolQuery("speciesgroupid", groupId));
		}
		
		Set<Object> taxonIds = commonMethod.cSTSOT(taxon);
		if (!taxonIds.isEmpty()) {
			boolAndLists.add(new MapAndBoolQuery("path", taxonIds));
		}

		Set<Object> authorId = commonMethod.cSTSOT(user);

		if (!authorId.isEmpty()) {
			boolAndLists.add(new MapAndBoolQuery("authorid", authorId));
		}

		Set<Object> userGroupId = commonMethod.cSTSOT(userGroupList);
		if (!userGroupId.isEmpty()) {
			boolAndLists.add(new MapAndBoolQuery("usergroupid", userGroupId));
		}

		Set<Object> userGroupName = commonMethod.cSTSOT(webaddress);
		if (!userGroupName.isEmpty()) {
			boolAndLists.add(new MapAndBoolQuery("usergroupname", userGroupName));

		}

		Set<Object> month = commonMethod.cSTSOT(months);
		if (!month.isEmpty()) {
			boolAndLists.add(new MapAndBoolQuery("frommonth", month));

		}
		Set<Object> maxvotedrecoids=commonMethod.cSTSOT(maxvotedrecoid);
		if(!maxvotedrecoids.isEmpty()){
			boolAndLists.add(new MapAndBoolQuery("maxvotedrecoid", maxvotedrecoids));
		}
		
		Set<String> speciesNames = commonMethod.cSTSOT(speciesName);
		if (!speciesNames.isEmpty()) {
			if (speciesNames.size() < 2) {
				String first = (String) speciesNames.toArray()[0];
				if (first.equalsIgnoreCase("UNIDENTIFED")) {
					andMapExistQueries.add(new MapExistQuery("name", false, null));
				}
				if (first.equalsIgnoreCase("IDENTIFED")) {
					andMapExistQueries.add(new MapExistQuery("name", true,null));
				}
			}

		}
		Set<String> validates = commonMethod.cSTSOT(validate);
		if (!validates.isEmpty()) {
			if (validates.size() < 2) {
				String first = (String) validates.toArray()[0];
				if (first.equalsIgnoreCase("invalidate")) {
					Set<Object> data = new HashSet<>();
					data.add("false");
					boolAndLists.add(new MapAndBoolQuery("islocked", data));
				}
				if (first.equalsIgnoreCase("validate")) {
					Set<Object> data = new HashSet<>();
					data.add("true");
					boolAndLists.add(new MapAndBoolQuery("islocked", data));
				}
			}

		}
		
		Set<Object> taxonStatus=commonMethod.cSTSOT(status);
		if(!taxonStatus.isEmpty()){
			boolAndLists.add(new MapAndBoolQuery("status", taxonStatus));
		}
		
		Set<Object> taxonIdsArray=commonMethod.cSTSOT(taxonId);
		if(!taxonIdsArray.isEmpty()){
				if (taxonIdsArray.size() < 2) {
					String first = (String) taxonIdsArray.toArray()[0];
					
					if (first.equalsIgnoreCase("0")) {
						andMapExistQueries.add(new MapExistQuery("status", false, null));
					}
					if (first.equalsIgnoreCase("1")) {
						andMapExistQueries.add(new MapExistQuery("status", true,null));
					}
				}

		}
		/**
		 * Query for recoName
		 */
		if(recoName!=null){
			andMatchPhraseQueries.add(new MapAndMatchPhraseQuery("name",recoName.toLowerCase()));
		}
		

		/**
		 * Date Filter
		 */

		String minDateValue = null;
		String maxDateValue = null;

		Date date = new Date();
		SimpleDateFormat out = new SimpleDateFormat("YYYY-MM-dd");
		try {
			if (minDate != null) {
				minDateValue = minDate;
			}
			if (maxDate != null) {
				maxDateValue = maxDate;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		if (minDateValue != null && maxDateValue != null) {

			rangeAndLists.add(new MapAndRangeQuery("fromdate", minDateValue, maxDateValue));
		}
		if (minDateValue != null && maxDateValue == null) {
			rangeAndLists.add(new MapAndRangeQuery("fromdate", minDateValue, out.format(date)));
		}
		if (minDateValue == null && maxDateValue != null) {
			rangeAndLists.add(new MapAndRangeQuery("fromdate", out.format(date), maxDateValue));
		}
		
		/**
		 * CretedOnfilter
		 */
		
		String createdOnMaxDateValue = null;
		String createdOnMinDateValue = null;
		try {

			if (createdOnMinDate != null) {

				createdOnMinDateValue = createdOnMinDate;
			}
			if (createdOnMaxDate != null) {
				createdOnMaxDateValue = createdOnMaxDate;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		if (createdOnMinDateValue != null && createdOnMaxDateValue != null) {

			rangeAndLists.add(new MapAndRangeQuery("createdon", createdOnMinDateValue, createdOnMaxDateValue));
		}
		if (createdOnMinDateValue != null && createdOnMaxDateValue == null) {
			rangeAndLists.add(new MapAndRangeQuery("createdon", createdOnMinDateValue, out.format(date)));
		}
		if (createdOnMinDateValue == null && createdOnMaxDateValue != null) {
			rangeAndLists.add(new MapAndRangeQuery("createdon", out.format(date), createdOnMaxDateValue));
		}
		
		
		/**
		 * General conditions
		 * 
		 * General condition
		 */

		String isDeleted = "false";
		Set<Object> isdeleted = commonMethod.cSTSOT(isDeleted);
		if (!isdeleted.isEmpty()) {
			boolAndLists.add(new MapAndBoolQuery("isdeleted", isdeleted));

		}
		String isCheckList = "false";
		Set<Object> ischecklist = commonMethod.cSTSOT(isCheckList);
		if (!ischecklist.isEmpty()) {
			boolAndLists.add(new MapAndBoolQuery("ischecklist", ischecklist));

		}
		// String isshowables = "true";
		// Set<String> isshowable = commonMethod.cSTSOT(isshowables);
		// if (!isshowable.isEmpty()) {
		// boolAndLists.add(new MapAndBoolQuery("isshowable", isshowable));
		//
		// }

		// Set<String> isDataSetId=new HashSet<String>();
		// isDataSetId.add(null);
		/**
		 * we need to get the observation only those have dataidkey set to null
		 */

		// boolAndLists.add(new MapAndBoolQuery("datasetid", null));

		/**
		 * ##########################################################################
		 * Range Querues
		 */
		
		Set<String> mediaFilters = commonMethod.cSTSOT(mediaFilter);
		if (!mediaFilters.isEmpty()) {
			// remove no media value
			for (String filter : mediaFilters) {
				rangeOrLists.add(new MapOrRangeQuery(filter, 1, Long.MAX_VALUE));
			}

		}

		Set<String> flagged = commonMethod.cSTSOT(isFlagged);
		if (!flagged.isEmpty()) {

			if (flagged.size() < 2) {
				String first = (String) flagged.toArray()[0];
				if (first.equalsIgnoreCase("1")) {
					rangeAndLists.add(new MapAndRangeQuery("flagcount", first, Long.MAX_VALUE));
				}
				if (first.equalsIgnoreCase("0")) {
					rangeAndLists.add(new MapAndRangeQuery("flagcount", first, first));
				}

			}
		}
		/**
		 * Filter to implement custom fields
		 */
		
		if(!customParams.isEmpty()){
			for(Map.Entry<String, List<String>>entry:customParams.entrySet()){
				String value=entry.getKey().split("\\.")[1];
				
				if(value.equalsIgnoreCase("string")){
					String key=entry.getKey().split("\\.")[0].split("_")[1];
					String Ids=entry.getValue().get(0);
					Set<String> listOfIds = commonMethod.cSTSOT(Ids);
					Set<Object> listOfIdsWithObject=listOfIds.stream().map(String::toLowerCase).collect(Collectors.toSet());
					
					String newKey="custom_fields."+key+".value";
					for (Object data:listOfIdsWithObject){
						orMatchPhraseQueriesnew.add(new MapOrMatchPhraseQuery(newKey,data));
					}
				}
				if(value.equalsIgnoreCase("text")){
					String key=entry.getKey().split("\\.")[0].split("_")[1];
					String Ids=entry.getValue().get(0);
					Set<String> listOfIds = commonMethod.cSTSOT(Ids);
					Set<Object> listOfIdsWithObject=listOfIds.stream().map(String::toLowerCase).collect(Collectors.toSet());
					
					String newKey="custom_fields."+key+".value";
					for (Object data:listOfIdsWithObject){
						boolAndLists.add(new MapAndBoolQuery(newKey,listOfIdsWithObject));
					}
				}
				
				if(value.equalsIgnoreCase("range")){
					String key=entry.getKey().split("\\.")[0].split("_")[1];
					String Ids=entry.getValue().get(0);
					List<Long> listOfIds = commonMethod.getListOfIds(Ids);
					Long rMin;	
					Long rMax;
					if(listOfIds.size()>=2){
						rMin=listOfIds.get(0);
						rMax=listOfIds.get(1);
						
						rangeAndLists.add(new MapAndRangeQuery("custom_fields."+key+".value",rMin,rMax));
					}
				}
				if(value.equalsIgnoreCase("para")){
					String key=entry.getKey().split("\\.")[0].split("_")[1];
					String Ids=entry.getValue().get(0);
					
					List<Long> listOfIds = commonMethod.getListOfIds(Ids);
					if(listOfIds.size()==1){
						Long yesorno=listOfIds.get(0);
						if (yesorno==0L) {
							andMapExistQueries.add(new MapExistQuery("custom_fields."+key+".value", false, null));
						}
						if (yesorno==1L) {
							andMapExistQueries.add(new MapExistQuery("custom_fields."+key+".value", true,null));
						}
					}
					
				}
				if(value.equalsIgnoreCase("date")){
					String key=entry.getKey().split("\\.")[0].split("_")[1];
					String Ids=entry.getValue().get(0);
					String [] y = Ids.split(",");
					String minCustomDate;
					String maxCustomDate;
					if(y[0]!=null &&y[1]!=null){
						minCustomDate=y[0].trim().substring(0, y[0].length()-1);
						maxCustomDate=y[1].trim().substring(0, y[1].length()-1);
						rangeAndLists.add(new MapAndRangeQuery("custom_fields."+key+".value",minCustomDate,maxCustomDate));
					}
					
					
				}
			}
		}
		
		
		/**
		 * Filter to implements the traits the traits filter is boolAndQuertype
		 * filters
		 */
		if(!traitParams.isEmpty()){
			for(Map.Entry<String, List<String>>entry:traitParams.entrySet()){
				try{
					String value=entry.getKey().split("\\.")[1];
					
					if(value.equalsIgnoreCase("string")){
						String key=entry.getKey().split("\\.")[0];
						String Ids=entry.getValue().get(0);
						System.out.println(Ids);
						Set<String> listOfIds = commonMethod.cSTSOT(Ids);
						Set<Object> listOfIdsWithObject=listOfIds.stream().map(String::toLowerCase).collect(Collectors.toSet());
						key="traits."+key;
						boolAndLists.add(new MapAndBoolQuery(key,listOfIdsWithObject));
					}
					
					
					if(value.equalsIgnoreCase("season")){
						String key=entry.getKey().split("\\.")[0];
						String Ids=entry.getValue().get(0);
						System.out.println(Ids);
						String [] y = Ids.split(",");
						
				
						try {
							Date minSeasonDate=new SimpleDateFormat("yyyy-MM-dd").parse(y[0]);
							Date maxSeasonDate=new SimpleDateFormat("yyyy-MM-dd").parse(y[1]);
							Calendar cal = Calendar.getInstance();
							cal.setTime(minSeasonDate);
							int minMonth=cal.get(Calendar.MONTH);
							int minDay=cal.get(Calendar.DATE);
							cal.setTime(maxSeasonDate);
							int maxMonth=cal.get(Calendar.MONTH);
							int maxDay=cal.get(Calendar.DATE);
							if(minMonth==0 && minDay==1 && maxMonth==11 && maxDay==31){
								
							}
							else{
							
								rangeAndLists.add(new MapAndRangeQuery("traits_season."+key,y[0].replace('Z',' '),y[1].replace('Z',' ')));
							}
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
						key="traits."+key;
						
					}
					
					
					if(value.equalsIgnoreCase("color_hsl")){
						
						String key=entry.getKey().split("\\.")[0];
						
						String Ids=entry.getValue().get(0);
						List<Long> listOfIds = commonMethod.getListOfIds(Ids);
						Long h;
						Long s;
						Long l;
						Long hMax;
						Long hMin;
						Long sMax;
						Long sMin;
						Long lMax;
						Long lMin;
						
						if(listOfIds.size()>=3){
							
							h=listOfIds.get(0);
							hMax=h+5L;
							if(h-5L<0){
								hMin=0L;
							}
							else{
								hMin=h-5L;
							}
							
							s=listOfIds.get(1);
							
							sMax=s+5L;
							if(s-5L<0){
								sMin=0L;
							}
							else{
								sMin=s-5L;
							}
							
							l=listOfIds.get(2);
							lMax=l+5L;
							if(l-5L<0){
								lMin=0L;
							}
							else{
								lMin=l-5L;
							}
						
							
							rangeAndLists.add(new MapAndRangeQuery("traits_json."+key+".h",hMin,hMax,"traits_json."+key));
							rangeAndLists.add(new MapAndRangeQuery("traits_json."+key+".s",sMin,sMax,"traits_json."+key));
							rangeAndLists.add(new MapAndRangeQuery("traits_json."+key+".l",lMin,lMax,"traits_json."+key));
						}
					}
					if(value.equalsIgnoreCase("range")){
						String key=entry.getKey().split("\\.")[0];
						String Ids=entry.getValue().get(0);
						List<Long> listOfIds = commonMethod.getListOfIds(Ids);
						Long rMin;
						Long rMax;
						if(listOfIds.size()>=2){
							rMin=listOfIds.get(0);
							rMax=listOfIds.get(1);
							rangeAndLists.add(new MapAndRangeQuery("traits."+key,rMin,rMax));
						}
						
					}
					
				}
				catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
				
			}
		}
		
		/**
		 * lft, right, top bottom
		 */

		/**
		 * combine all the queries
		 * 
		 */

		MapSearchQuery mapSearchQuery = new MapSearchQuery(boolAndLists, boolOrLists, rangeAndLists, rangeOrLists,
				andMapExistQueries,andMatchPhraseQueries,orMatchPhraseQueriesnew,mapSearchParams);

		return mapSearchQuery;
	}
}
