package eu.basileus.osgi.security;

import org.osgi.service.http.HttpContext;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

/**
 * @author Vassili Jakovlev
 */
public class AuthenticationFilter implements Filter {
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    if (!isAuthenticated((HttpServletRequest) request)) {
      // FIXME: Basic auth is for proof of concept only
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
