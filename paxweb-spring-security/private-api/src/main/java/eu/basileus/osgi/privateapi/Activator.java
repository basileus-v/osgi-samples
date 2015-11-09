package eu.basileus.osgi.privateapi;

import eu.basileus.osgi.common.WebContainerServiceTracker;
import eu.basileus.osgi.security.AuthenticationFilter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.springframework.web.filter.DelegatingFilterProxy;

/**
 * @author Vassili Jakovlev
 */
public class Activator implements BundleActivator {

  private static final String API_URL = "/api/v2/private";
  private WebContainerServiceTracker httpTracker;

  public void start(BundleContext context) throws Exception {
    httpTracker = new WebContainerServiceTracker(context, new PrivateApiServlet(), API_URL);
//    httpTracker.setFilter(new AuthenticationFilter());
    httpTracker.setFilter(new DelegatingFilterProxy());
    httpTracker.open();
  }

  public void stop(BundleContext context) throws Exception {
    httpTracker.close();
  }

}