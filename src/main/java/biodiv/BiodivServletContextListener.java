package biodiv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

import biodiv.activityFeed.ActivityFeedModule;
import biodiv.auth.AuthModule;
import biodiv.auth.LoginController;
import biodiv.auth.SimpleUsernamePasswordAuthenticator;
import biodiv.auth.token.Token;
import biodiv.auth.token.TokenDao;
import biodiv.auth.token.TokenService;
import biodiv.comment.CommentModule;
import biodiv.common.BiodivCommonModule;
import biodiv.customField.CustomFieldModule;
import biodiv.dataset.DatasetModule;
import biodiv.follow.FollowModule;
import biodiv.maps.MapModule;
import biodiv.observation.Observation;
import biodiv.observation.ObservationDao;
import biodiv.observation.ObservationModule;
import biodiv.observation.ObservationService;
import biodiv.taxon.TaxonModule;
import biodiv.traits.TraitModule;
import biodiv.user.User;
import biodiv.user.UserDao;
import biodiv.user.UserModule;
import biodiv.user.UserService;
import biodiv.userGroup.Newsletter;
import biodiv.userGroup.NewsletterDao;
import biodiv.userGroup.NewsletterService;
import biodiv.userGroup.UserGroup;
import biodiv.userGroup.UserGroupDao;
import biodiv.userGroup.UserGroupModule;
import biodiv.userGroup.UserGroupService;

public class BiodivServletContextListener extends GuiceServletContextListener {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	protected Injector getInjector() {
		return Guice.createInjector(
				new BiodivCommonModule(), 
				new ActivityFeedModule(), 
				new AuthModule(),
				new CommentModule(), 
				new CustomFieldModule(), new DatasetModule(), new FollowModule(), new MapModule(),
				new ObservationModule(), new TaxonModule(), new TraitModule(), new UserModule(), new UserGroupModule(),

				new ServletModule() {

					@Override
					protected void configureServlets() {
						log.debug("Configuring Servlets");

						// rest("/*").packages("biodiv");

						// INTERCEPTOR
						ResourceInterceptor interceptor = new ResourceInterceptor();
						bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), interceptor);
						bind(ResourceInterceptor.class).toInstance(interceptor);
						requestInjection(interceptor);

						// FILTERS
						//bind(BiodivResponseFilter.class).in(Singleton.class);

					}
				});
	}

}
