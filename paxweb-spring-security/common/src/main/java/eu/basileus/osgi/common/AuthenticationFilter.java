package eu.basileus.osgi.common;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Vassili Jakovlev
 */
public class AuthenticationFilter implements Filter {
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    ((HttpServletResponse) response).addHeader("Authenticated", "true");
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {

  }
}
