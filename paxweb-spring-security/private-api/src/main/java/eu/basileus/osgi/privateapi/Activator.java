package eu.basileus.osgi.privateapi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import eu.basileus.osgi.common.WebContainerServiceTracker;
import eu.basileus.osgi.security.OIDCAuthenticationFilterFactory;

/**
 * @author Vassili Jakovlev
 */
public class Activator implements BundleActivator {

  private static final String API_URL = "/api/v2/private";
  private WebContainerServiceTracker httpTracker;

  public void start(BundleContext context) throws Exception {
    httpTracker = new WebContainerServiceTracker(context, new PrivateApiServlet(), API_URL);
    httpTracker.setFilter(OIDCAuthenticationFilterFactory.create());
    httpTracker.open();
  }

  public void stop(BundleContext context) throws Exception {
    httpTracker.close();
  }

}