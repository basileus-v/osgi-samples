package eu.basileus.osgi.common;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * @author Vassili Jakovlev
 */
public class HttpServiceTracker extends ServiceTracker<HttpService, HttpService> {

  private final String apiUrl;
  private final HttpServlet httpServlet;
  private HttpContext httpContext;

  public HttpServiceTracker(BundleContext bundleContext, HttpServlet httpServlet, String apiUrl) {
    super(bundleContext, HttpService.class.getName(), null);
    this.httpServlet = httpServlet;
    this.apiUrl = apiUrl;
  }

  public HttpService addingService(ServiceReference<HttpService> reference) {
    HttpService httpService = context.getService(reference);
    try {
      httpService.registerServlet(apiUrl, httpServlet, null, httpContext);
    } catch (ServletException | NamespaceException e) {
      throw new RuntimeException("Failed to register " + httpServlet.getServletName() + " at " + apiUrl);
    }
    return httpService;
  }

  public void removedService(ServiceReference<HttpService> reference, HttpService service) {
    try {
      service.unregister(apiUrl);
    } catch (IllegalArgumentException exception) {
      // Ignore; servlet registration probably failed earlier on...
    }
  }

  public void setHttpContext(HttpContext httpContext) {
    this.httpContext = httpContext;
  }
}
