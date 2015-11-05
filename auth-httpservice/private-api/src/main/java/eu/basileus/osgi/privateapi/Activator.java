package eu.basileus.osgi.privateapi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

import javax.servlet.ServletException;

/**
 * @author Vassili Jakovlev (vassili.jakovlev@nortal.com)
 */
public class Activator implements BundleActivator {

  private HttpServiceTracker httpTracker;

  public void start(BundleContext context) throws Exception {
    httpTracker = new HttpServiceTracker(context);
    httpTracker.open();
  }

  public void stop(BundleContext context) throws Exception {
    httpTracker.close();
  }

  private class HttpServiceTracker extends ServiceTracker<HttpService, HttpService> {

    public static final String API_URL = "/api/v1/private";

    public HttpServiceTracker(BundleContext context) {
      super(context, HttpService.class.getName(), null);
    }

    public HttpService addingService(ServiceReference<HttpService> reference) {
      HttpService httpService = context.getService(reference);
      try {
        httpService.registerServlet(API_URL, new PrivateApiServlet(), null, new AuthHttpContext());
      } catch (ServletException | NamespaceException e) {
        e.printStackTrace();
      }
      return httpService;
    }

    public void removedService(ServiceReference<HttpService> reference, HttpService service) {
      try {
        service.unregister(API_URL);
      } catch (IllegalArgumentException exception) {
        // Ignore; servlet registration probably failed earlier on...
      }
    }
  }

}