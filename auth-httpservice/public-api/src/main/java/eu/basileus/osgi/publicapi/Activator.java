package eu.basileus.osgi.publicapi;

import eu.basileus.osgi.common.HttpServiceTracker;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author Vassili Jakovlev
 */
public class Activator implements BundleActivator {

  private static final String API_URL = "/api/v1/public";
  private HttpServiceTracker httpTracker;

  public void start(BundleContext context) throws Exception {
    httpTracker = new HttpServiceTracker(context, new PublicApiServlet(), API_URL);
    httpTracker.open();
  }

  public void stop(BundleContext context) throws Exception {
    httpTracker.close();
  }

}