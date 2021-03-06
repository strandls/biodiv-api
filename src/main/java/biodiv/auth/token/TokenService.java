package biodiv.auth.token;

import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.definition.CommonProfileDefinition;
import org.pac4j.core.profile.jwt.JwtClaims;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.profile.JwtGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biodiv.Transactional;
import biodiv.auth.AuthUtils;
import biodiv.auth.Constants;
import biodiv.auth.SimpleUsernamePasswordAuthenticator;
import biodiv.auth.token.Token.TokenType;
import biodiv.common.AbstractService;
import biodiv.user.User;
import biodiv.user.UserService;
import biodiv.util.RandomString;
import org.jvnet.hk2.annotations.Service;

@Service
public class TokenService extends AbstractService<Token> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private TokenDao tokenDao;

	@Inject
	private UserService userService;

	@Inject
	private SimpleUsernamePasswordAuthenticator usernamePasswordAuthenticator;

	@Inject
	TokenService(TokenDao tokenDao) {
		super(tokenDao);
		this.tokenDao = tokenDao;
	}

	public TokenDao getDao() {
		return tokenDao;
	}

	/**
	 * 
	 * @param username
	 *            username
	 * @param password
	 *            password
	 * @return is valid or not
	 * @throws Exception
	 *             Possible error
	 */
	@Transactional
	public CommonProfile authenticate(String username, String password) throws Exception {
		// Authenticate the user using the credentials provided
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password, "");
		usernamePasswordAuthenticator.validate(credentials, null);
		return credentials.getUserProfile();
	}


	@Transactional
	public Map<String, Object> buildTokenResponse(CommonProfile profile, long userId, boolean getNewRefreshToken) {
		return buildTokenResponse(profile, userService.findById(userId), getNewRefreshToken);
	}

	/**
	 * Builds a response for authentication. On success it returns a access
	 * token and optionally a refresh token
	 * 
	 * @param profile
	 *            dummy
	 * @param user
	 *            dummy
	 * @param getNewRefreshToken
	 *            dummy
	 * @return dummy
	 */
	@Transactional
	public Map<String, Object> buildTokenResponse(CommonProfile profile, User user, boolean getNewRefreshToken) {
		try {
			log.debug("Building token response for " + user);

			String jwtToken = generateAccessToken(profile, user);

			// tokenDao.openCurrentSessionWithTransaction();
			// Return the access_token valid for 2 hrs and a new refreshToken on
			// the response
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("access_token", jwtToken);
			result.put("token_type", "bearer");
			// result.put("pic",user.getProfilePic());

			if (getNewRefreshToken) {
				log.debug("Generating new refresh token for " + user);
				// Removing all existing refreshTokens
				/*
				 * List<Token> existingRefreshToken = tokenDao.findByUser(user);
				 * for (Token t : existingRefreshToken) { user.setToken(null);
				 * tokenDao.delete(t); } tokenDao.getCurrentSession().flush();
				 */

				// Generating a fresh refreshToken
				String refreshToken = generateRefreshToken();

				Token rToken = new Token(refreshToken, TokenType.REFRESH, user);

				tokenDao.save(rToken);

				result.put("refresh_token", refreshToken);
				
				user.setLastLoginDate(new Date());
				userService.save(user);
			}
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			// tokenDao.closeCurrentSessionWithTransaction();
		}
	}

	/**
	 * Generates access token in JWT format encrypted with JWT_SALT as secret
	 * for the profile.
	 * 
	 * @param profile
	 *            dummy
	 * @return TODO : use bcrypt encryption for token
	 */
	private String generateAccessToken(CommonProfile profile, User user) {
		log.debug("generateAccessToken .... ");
		JwtGenerator<CommonProfile> generator = new JwtGenerator<>(
				new SecretSignatureConfiguration(Constants.JWT_SALT));
		// jwt claims are added in AuthUtils.updateUserProfile
		Map jwtClaims = new HashMap<String, Object>();
		jwtClaims.put("id", profile.getId());
		jwtClaims.put(JwtClaims.SUBJECT, profile.getId() + "");
		jwtClaims.put(Pac4jConstants.USERNAME, profile.getUsername());
		jwtClaims.put(CommonProfileDefinition.EMAIL, profile.getEmail());
		jwtClaims.put(JwtClaims.EXPIRATION_TIME, AuthUtils.getAccessTokenExpiryDate());
		jwtClaims.put(JwtClaims.ISSUED_AT, new Date());
		jwtClaims.put("roles", profile.getRoles());
		log.debug("++++++++++++++++++++++++++++++++++");
		log.debug("++++++++++++++++++++++++++++++++++ {} ", profile);
		log.debug("++++++++++++++++++++++++++++++++++ {} ", profile.getPictureUrl());
		log.debug("++++++++++++++++++++++++++++++++++ {} ", user.getProfilePic());
		jwtClaims.put("pic", user.getProfilePic());
		String jwtToken = generator.generate(jwtClaims);
		return jwtToken;
	}

	/**
	 * Generates a refresh token which is a plain string used to identify user.
	 * 
	 * @return dummy
	 */
	private String generateRefreshToken() {
		// Random random = new SecureRandom();
		// String token = new BigInteger(130, random).toString(32);

		// Algorithm : To generate a random string, concatenate characters drawn
		// randomly from the set of acceptable symbols until the string reaches
		// the desired length.
		log.debug("generateRefreshToken .... ");
		String easy = RandomString.digits + "ACEFGHJKLMNPQRUVWXYabcdefhijkprstuvwx";
		RandomString tickets = new RandomString(23, new SecureRandom(), easy);

		return tickets.nextString();
	}

	/**
	 * 
	 * @param refreshToken
	 *            dummy
	 * @param userId
	 *            dummy
	 * @return dummy
	 */
	public boolean isValidRefreshToken(String refreshToken, Long userId) {
		if (refreshToken == null || userId == null)
			return false;
		log.debug("isValidRefreshToken .... " + userId);
		Token token = tokenDao.findByValueAndUser(refreshToken, userId);
		if (token == null) {
			log.warn("Refresh token is invalid.");
			return false;
		}
		if (token.getCreatedOn().before(AuthUtils.getRefreshTokenExpiryDate())) {
			log.warn("Refresh token expired.");
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param refreshToken
	 *            dummy
	 */
	@Transactional
	public void removeRefreshToken(String refreshToken) {
		if (refreshToken == null)
			return;
		log.debug("Removing refresh token " + refreshToken);
		// Removing refreshToken
		Token existingRefreshToken = tokenDao.findByValue(refreshToken);
		if (existingRefreshToken != null) {
			// User user = userService.findById(userId);
			// user.setTokens().remove(existingRefreshToken);
			tokenDao.delete(existingRefreshToken);

			log.debug("Flushing session on delete tokens");
			tokenDao.flush();
		}

	}

	@Transactional
	public void removeRefreshToken(Long userId, String refreshToken) {
		if (userId == null || refreshToken == null)
			return;
		// tokenDao.openCurrentSessionWithTransaction();
		log.debug("Removing refresh token " + refreshToken + " for user " + userId);
		// Removing refreshToken
		Token existingRefreshToken = tokenDao.findByValueAndUser(refreshToken, userId);
		if (existingRefreshToken != null) {
			// User user = userService.findById(userId);
			// user.getTokens().remove(existingRefreshToken);
			tokenDao.delete(existingRefreshToken);
		}
		/*
		 * List<Token> existingRefreshToken = tokenDao.findByUser(userId); User
		 * user = userService.findById(userId); for (Token t :
		 * existingRefreshToken) { user.setTokens(null); tokenDao.delete(t); }
		 */
		log.debug("Flushing session on delete tokens");
		tokenDao.flush();

	}

	public Token findByValue(String value) {
		Token token = tokenDao.findByValue(value);
		return token;
	}

	public CommonProfile createUserProfile(String refreshToken) {
		Token refreshTokenInstance = findByValue(refreshToken);
		if (refreshTokenInstance != null) {
			User user = refreshTokenInstance.getUser();
			return userService.createUserProfile(user);
		} else {
			return null;
		}
	}

}
