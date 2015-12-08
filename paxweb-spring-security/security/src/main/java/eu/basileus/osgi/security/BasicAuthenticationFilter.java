package eu.basileus.osgi.security;

import java.io.IOException;
import java.util.Base64;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.HttpContext;

/**
 * @author Vassili Jakovlev
 */
public class BasicAuthenticationFilter implements Filter {
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (!isAuthenticated((HttpServletRequest) request)) {
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.addHeader("WWW-Authenticate", "Basic realm=\"FooBarBaz\"");
			httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
		chain.doFilter(request, response);
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
