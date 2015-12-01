package eu.basileus.osgi.security;

import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.function.Supplier;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mitre.jwt.signer.service.impl.JWKSetCacheService;
import org.mitre.openid.connect.client.service.ServerConfigurationService;
import org.mitre.openid.connect.client.service.impl.DynamicServerConfigurationService;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.osgi.service.http.HttpContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.SerializeException;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.http.ServletUtils;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.UserInfoSuccessResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;

import javaslang.Lazy;

/**
 * @author Vassili Jakovlev
 */
public class AuthenticationFilter implements Filter {
	
	private Supplier<ServerConfigurationService> serverConfigurationService = Lazy.of(() -> new DynamicServerConfigurationService());
	private Supplier<JWKSetCacheService> jwkSetCacheService = Lazy.of(() -> new JWKSetCacheService());
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			if (isOpenidAuth(request, response)) {
				System.out.println("yupi");
			} else {
				return;
			}
		} catch (ParseException | java.text.ParseException | SerializeException e) {
			handleError(request, response, e.getLocalizedMessage());
			return;
		}
		if (false && !isAuthenticated((HttpServletRequest) request)) {
			// FIXME: Basic auth is for proof of concept only
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.addHeader("WWW-Authenticate", "Basic realm=\"FooBarBaz\"");
			httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
		chain.doFilter(request, response);
	}
	
	private void handleError(ServletRequest request, ServletResponse response, String error) throws IOException {
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, error);
	}

	private boolean isOpenidAuth(ServletRequest request, ServletResponse response) throws IOException, ParseException, java.text.ParseException, SerializeException {
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		String result = "nada";
		BearerAccessToken accessToken;
		accessToken = BearerAccessToken.parse(ServletUtils.createHTTPRequest((HttpServletRequest) request));
		JWT jwt = JWTParser.parse(accessToken.getValue());
		IdTokenValidator idTokenValidator = new IdTokenValidator(serverConfigurationService.get(), jwkSetCacheService.get()::getValidator, 1);
		String error = idTokenValidator.validate(jwt);
		if (error != null ) {
			handleError(request, response, error);
			return false;
		}
		UserInfoRequest userInfoRequest = new UserInfoRequest(URI.create(jwt.getJWTClaimsSet().getIssuer() + "/userinfo"), accessToken);
		HTTPResponse userInfoHttpResponse = userInfoRequest.toHTTPRequest().send();
		UserInfoResponse userInfoResponse = UserInfoResponse.parse(userInfoHttpResponse);
		if (userInfoResponse.indicatesSuccess()){
			UserInfoSuccessResponse userInfoSuccessResponse = (UserInfoSuccessResponse)userInfoResponse;
			httpResponse.addHeader("X-USERINFO-JWT", "" + userInfoSuccessResponse.getUserInfoJWT());
			httpResponse.addHeader("X-USERINFO-CLAIMS", "" + userInfoSuccessResponse.getUserInfo().toJSONObject().toJSONString());
			UserInfo userInfo = userInfoSuccessResponse.getUserInfo();
			SecurityContextHolder.getContext().setAuthentication(new OIDCAuthenticationToken(userInfo.getSubject().getValue(), jwt.getJWTClaimsSet().getIssuer(), null, null, jwt, accessToken.getValue(), null));
		}
		httpResponse.addHeader("X-BLU", jwt.getJWTClaimsSet().toJSONObject().toString());
		result = accessToken.toJSONString();
		httpResponse.addHeader("X-BLA", result);
		return true;
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
