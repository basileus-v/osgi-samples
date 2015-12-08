package eu.basileus.osgi.security;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mitre.openid.connect.client.UserInfoFetcher;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.mitre.openid.connect.model.PendingOIDCAuthenticationToken;
import org.mitre.openid.connect.model.UserInfo;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level=AccessLevel.PRIVATE)
public class OIDCAuthenticationFilter implements Filter {
	OIDCRequestAuthenticationExtractor authenticationExtractor;
	OIDCAuthorityGranter authorityGranter;
	UserInfoFetcher userInfoFetcher = new CachingUserInfoFetcher();
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
		PendingOIDCAuthenticationToken token = authenticationExtractor.parse((HttpServletRequest) request);
		UserInfo userInfo = userInfoFetcher.loadUserInfo(token);
		List<GrantedAuthority> authorities = authorityGranter.grant(token);
		SecurityContextHolder.getContext().setAuthentication(
				new OIDCAuthenticationToken(token.getSub(), 
											token.getIssuer(), 
											userInfo, 
											authorities, 
											token.getIdToken(),
											token.getAccessTokenValue(),
											token.getRefreshTokenValue()));
		chain.doFilter(request, response);
		} catch (AuthenticationServiceException e) {
			handleError(request, response, e.getMessage());
		}
	}
	
	private void handleError(ServletRequest request, ServletResponse response, String error) throws IOException {
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, error);
	}

	@Override
	public void destroy() {
	}

}
