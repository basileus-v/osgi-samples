package eu.basileus.osgi.publicapi;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Vassili Jakovlev
 */
public class JerseyApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> result = new HashSet<>();
    result.add(UserResource.class);
    return result;
  }
}
