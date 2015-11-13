package eu.basileus.osgi.publicapi;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("users")
public class UserResource {

  @GET
  @Path("current")
  @Produces(MediaType.TEXT_PLAIN)
  public String getStatus() {
    return "anonymous";
  }
}