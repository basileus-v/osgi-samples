package eu.basileus.osgi.publicapi;

import eu.basileus.osgi.common.WebContainerServiceTracker;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author Vassili Jakovlev
 */
public class Activator implements BundleActivator {

  private static final String API_URL = "/api/v2/public";
  private WebContainerServiceTracker webContainerServiceTracker;

  public void start(BundleContext context) throws Exception {
    webContainerServiceTracker = new WebContainerServiceTracker(context, new PublicApiServlet(), API_URL);
    webContainerServiceTracker.open();
  }

  public void stop(BundleContext context) throws Exception {
    webContainerServiceTracker.close();
  }

}