package eu.basileus.osgi.publicapi;

import eu.basileus.osgi.common.HttpServiceTracker;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @author Vassili Jakovlev
 */
public class Activator implements BundleActivator {

  private static final String API_URL = "/api/v1/public";
  private HttpServiceTracker httpTracker;

  public void start(BundleContext context) throws Exception {
    httpTracker = new HttpServiceTracker(context, new ServletContainer(), API_URL);
    httpTracker.setServletParams(getJerseyServletParams());
    httpTracker.open();
  }

  public void stop(BundleContext context) throws Exception {
    httpTracker.close();
  }

  @SuppressWarnings("UseOfObsoleteCollectionType")
  private Dictionary<String, String> getJerseyServletParams() {
    Dictionary<String, String> jerseyServletParams = new Hashtable<>();
    jerseyServletParams.put("javax.ws.rs.Application", JerseyApplication.class.getName());
    return jerseyServletParams;
  }

}