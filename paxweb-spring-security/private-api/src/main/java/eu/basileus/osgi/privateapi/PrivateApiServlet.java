package eu.basileus.osgi.privateapi;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Vassili Jakovlev
 */
class PrivateApiServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	OIDCAuthenticationToken authentication = (OIDCAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    resp.getWriter().write("This API is private. " + authentication.getSub());
  }
}
