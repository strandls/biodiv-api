package biodiv.scheduler;

import java.util.List;

import javax.inject.Inject;

import org.apache.http.ParseException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biodiv.admin.AdminService;
import biodiv.common.ESmoduleUrlService;
import biodiv.mail.DownloadMailingService;
import biodiv.maps.MapHttpResponse;
import biodiv.maps.MapIntegrationService;
import biodiv.maps.MapSearchQuery;
import biodiv.scheduler.DownloadLog.DownloadType;
import biodiv.scheduler.DownloadLog.SourceType;
import biodiv.user.User;
public class DownloadJob implements Job {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	public static final String INDEX_KEY = "index";
	public static final String TYPE_KEY = "type";
	public static final String DATA_KEY = "data";
	public static final String USER_KEY = "user";
	public static final String SEARCH_KEY = "search";
	public static final String NOTES_KEY = "notes";
	public static final String GEO_FIELD_KEY = "geoField";

	@Inject
	MapIntegrationService mapIntegrationService;

	@Inject
	DownloadLogService downloadLogService;

	@Inject
	ESmoduleUrlService esmoduleUrlService;
	@Inject
	AdminService adminService;

	@Inject
	DownloadMailingService downloadMailingService;

	public DownloadJob() {
		// Instances of Job must have a public no-argument constructor.
	}

	public void execute(JobExecutionContext context)
			throws JobExecutionException {

		JobDataMap data = context.getMergedJobDataMap();
		
		String index = data.getString(INDEX_KEY);
		String indexType = data.getString(TYPE_KEY);
		MapSearchQuery mapSearchQuery = (MapSearchQuery) data.get(DATA_KEY);
		String geoField = data.getString(GEO_FIELD_KEY);
		
		User user = (User) data.get(USER_KEY);
		String filterUrl = data.getString(SEARCH_KEY);
		String notes = data.getString(NOTES_KEY);
		SchedulerStatus status = SchedulerStatus.Scheduled;
		DownloadType type = DownloadType.CSV;
		SourceType sourceType = SourceType.Observations;
		
		DownloadLog downloadLog = new DownloadLog(user, filterUrl, notes, status, type, sourceType, 0);
		downloadLogService.save(downloadLog);

		String url = esmoduleUrlService.getDownloadUrl(index, indexType, geoField);
		MapHttpResponse httpResponse = mapIntegrationService.postRequest(url, mapSearchQuery);
		
		String filePath = null;
		status = SchedulerStatus.Failed;

		if(httpResponse != null) {
			try {
				String jsonFilePath = (String) httpResponse.getDocument();
				status = SchedulerStatus.Success;
				filePath=adminService.downloadFile(jsonFilePath);
				
				addDownloadMail(user);
				
			} catch (ParseException e) {
				log.error("Error while reading the csv file path response from naksha", e);
			} catch (Exception e) {
				log.error("Error in download job", e);
			}
		}
		
		log.info("Download file generated with status {} at {}", status, filePath);
		downloadLog.setFilePath(filePath);
		downloadLog.setStatus(status);
		downloadLogService.update(downloadLog);
	}
	
	public void addDownloadMail(User user) throws Exception{
		
		try{
			
			List<User> allBccs = downloadMailingService.getAllBccPeople();
			for(User bcc : allBccs){
				downloadMailingService.buildDownloadMailMessage(bcc.getEmail(),user.getId(),user.getName());
			}
			
			if(user.getSendNotification()){
				downloadMailingService.buildDownloadMailMessage(user.getEmail(),user.getId(),user.getName());
			}
		
			if(!downloadMailingService.isAnyThreadActive()){
				Thread th = new Thread(downloadMailingService);
				th.start();
			}
		}catch(Exception e){
			log.error("Error while mailing download status", e);
			throw e;
		}
		
	}

}