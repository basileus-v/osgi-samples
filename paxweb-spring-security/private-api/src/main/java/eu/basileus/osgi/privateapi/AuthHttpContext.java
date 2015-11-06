package eu.basileus.osgi.privateapi;

import org.osgi.service.http.HttpContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;

public class AuthHttpContext implements HttpContext {

  @Override
  public boolean handleSecurity(HttpServletRequest req, HttpServletResponse res) throws IOException {
    if (!isAuthenticated(req)) {
      // FIXME: Basic auth is for proof of concept only
      res.addHeader("WWW-Authenticate", "Basic realm=\"FooBarBaz\"");
      res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return false;
    }
    return true;
  }

  protected boolean isAuthenticated(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader == null) {
      return false;
    }
    request.setAttribute(AUTHENTICATION_TYPE, HttpServletRequest.BASIC_AUTH);

    String usernameAndPassword = new String(Base64.getDecoder().decode(authHeader.substring(6)));

    int userNameIndex = usernameAndPassword.indexOf(":");
    String username = usernameAndPassword.substring(0, userNameIndex);
    String password = usernameAndPassword.substring(userNameIndex + 1);

    boolean success = ((username.equals("admin") && password.equals("admin")));
    if (success) {
      request.setAttribute(REMOTE_USER, "admin");
    }
    return success;
  }

  @Override
  public URL getResource(String s) {
    return null;
  }

  @Override
  public String getMimeType(String s) {
    return null;
  }
}
