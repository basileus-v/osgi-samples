package eu.basileus.osgi.security;

import java.io.IOException;
import java.net.URI;
import java.util.Base64;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mitre.jwt.signer.service.JWTSigningAndValidationService;
import org.mitre.jwt.signer.service.impl.JWKSetCacheService;
import org.mitre.openid.connect.client.service.ServerConfigurationService;
import org.mitre.openid.connect.client.service.impl.DynamicServerConfigurationService;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.osgi.service.http.HttpContext;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.SerializeException;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.http.ServletUtils;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.UserInfoSuccessResponse;

/**
 * @author Vassili Jakovlev
 */
public class AuthenticationFilter implements Filter {
	
	private ServerConfigurationService serverConfigurationService;
	private JWKSetCacheService jwkSetCacheService;  
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}
	
	public ServerConfigurationService getServerConfigurationService() {
		if (serverConfigurationService == null){
			serverConfigurationService = new DynamicServerConfigurationService();
		}
		return serverConfigurationService;
	}
	
	public JWKSetCacheService getJwkSetCacheService() {
		if (jwkSetCacheService == null) {
			jwkSetCacheService = new JWKSetCacheService();
		}
		return jwkSetCacheService;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			if (isOpenidAuth(request, response)) {
				System.out.println("yupi");
			}
		} catch (ParseException | java.text.ParseException | SerializeException e) {
			throw new RuntimeException(e);
		}
		if (false && !isAuthenticated((HttpServletRequest) request)) {
			// FIXME: Basic auth is for proof of concept only
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.addHeader("WWW-Authenticate", "Basic realm=\"FooBarBaz\"");
			httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
		chain.doFilter(request, response);
	}

	private boolean isOpenidAuth(ServletRequest request, ServletResponse response) throws IOException, ParseException, java.text.ParseException, SerializeException {
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		String result = "nada";
		BearerAccessToken accessToken;
		accessToken = BearerAccessToken.parse(ServletUtils.createHTTPRequest((HttpServletRequest) request));
		JWT jwt = JWTParser.parse(accessToken.getValue());
		String issuer = jwt.getJWTClaimsSet().getIssuer();
		ServerConfiguration serverConfiguration = getServerConfigurationService().getServerConfiguration(issuer);
		JWTSigningAndValidationService validator = getJwkSetCacheService().getValidator(serverConfiguration.getJwksUri());
		if (jwt instanceof SignedJWT) {
			if (validator.validateSignature((SignedJWT) jwt)){
			} else {
				throw new RuntimeException("Cannot validate " + jwt.getJWTClaimsSet().toJSONObject());
			}
		} else {
			throw new RuntimeException("WILL NOT PROCESS UNSIGNED STAFF!");
		}
		UserInfoRequest userInfoRequest = new UserInfoRequest(URI.create(jwt.getJWTClaimsSet().getIssuer() + "/userinfo"), accessToken);
		HTTPResponse userInfoHttpResponse = userInfoRequest.toHTTPRequest().send();
		UserInfoResponse userInfoResponse = UserInfoResponse.parse(userInfoHttpResponse);
		if (userInfoResponse.indicatesSuccess()){
			UserInfoSuccessResponse userInfoSuccessResponse = (UserInfoSuccessResponse)userInfoResponse;
			httpResponse.addHeader("X-USERINFO-JWT", "" + userInfoSuccessResponse.getUserInfoJWT());
			httpResponse.addHeader("X-USERINFO-CLAIMS", "" + userInfoSuccessResponse.getUserInfo().toJSONObject().toJSONString());
		}
		httpResponse.addHeader("X-BLU", jwt.getJWTClaimsSet().toJSONObject().toString());
		result = accessToken.toJSONString();
		httpResponse.addHeader("X-BLA", result);
		return false;
	}

	protected boolean isAuthenticated(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null) {
			return false;
		}
		request.setAttribute(HttpContext.AUTHENTICATION_TYPE, HttpServletRequest.BASIC_AUTH);

		String usernameAndPassword = new String(Base64.getDecoder().decode(authHeader.substring(6)));

		int userNameIndex = usernameAndPassword.indexOf(":");
		String username = usernameAndPassword.substring(0, userNameIndex);
		String password = usernameAndPassword.substring(userNameIndex + 1);

		boolean success = ((username.equals("admin") && password.equals("admin")));
		if (success) {
			request.setAttribute(HttpContext.REMOTE_USER, "admin");
		}
		return success;
	}

	@Override
	public void destroy() {

	}
}
