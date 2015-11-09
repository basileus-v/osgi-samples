package eu.basileus.osgi.common;

import org.ops4j.pax.web.service.WebContainer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * @author Vassili Jakovlev
 */
public class WebContainerServiceTracker extends ServiceTracker<WebContainer, WebContainer> {

  private final String apiUrl;
  private final HttpServlet httpServlet;
  private Filter filter;

  public WebContainerServiceTracker(BundleContext bundleContext, HttpServlet httpServlet, String apiUrl) {
    super(bundleContext, WebContainer.class.getName(), null);
    this.httpServlet = httpServlet;
    this.apiUrl = apiUrl;
  }

  public WebContainer addingService(ServiceReference<WebContainer> reference) {
    WebContainer webContainer = context.getService(reference);
    try {
      webContainer.registerServlet(apiUrl, httpServlet, null, null);
    } catch (ServletException | NamespaceException e) {
      throw new RuntimeException("Failed to register " + httpServlet.getServletName() + " at " + apiUrl);
    }
    if (filter != null) {
      webContainer.registerFilter(filter, new String[]{apiUrl}, null, null, null);
    }
    return webContainer;
  }

  public void removedService(ServiceReference<WebContainer> reference, WebContainer service) {
    service.unregister(apiUrl);
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }
}
