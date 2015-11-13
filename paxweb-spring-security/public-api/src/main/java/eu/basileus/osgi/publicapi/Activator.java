package eu.basileus.osgi.publicapi;

import eu.basileus.osgi.common.WebContainerServiceTracker;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @author Vassili Jakovlev
 */
public class Activator implements BundleActivator {

  private static final String API_URL = "/api/v2/public";
  private WebContainerServiceTracker webContainerServiceTracker;

  public void start(BundleContext context) throws Exception {
    webContainerServiceTracker = new WebContainerServiceTracker(context, new ServletContainer(), API_URL);
    webContainerServiceTracker.setServletParams(getJerseyServletParams());
    webContainerServiceTracker.open();
  }

  public void stop(BundleContext context) throws Exception {
    webContainerServiceTracker.close();
  }

  private Dictionary<String, String> getJerseyServletParams() {
    Dictionary<String, String> jerseyServletParams = new Hashtable<>();
    jerseyServletParams.put("javax.ws.rs.Application", JerseyApplication.class.getName());
    return jerseyServletParams;
  }

}