package eu.basileus.osgi.privateapi;

import eu.basileus.osgi.common.HttpServiceTracker;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author Vassili Jakovlev
 */
public class Activator implements BundleActivator {

  private static final String API_URL = "/api/v1/private";
  private HttpServiceTracker httpTracker;

  public void start(BundleContext context) throws Exception {
    httpTracker = new HttpServiceTracker(context, new PrivateApiServlet(), API_URL);
    httpTracker.setHttpContext(new AuthHttpContext());
    httpTracker.open();
  }

  public void stop(BundleContext context) throws Exception {
    httpTracker.close();
  }

}