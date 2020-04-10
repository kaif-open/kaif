package io.kaif.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import io.kaif.config.SpringProfile;

@Profile(SpringProfile.DEV)
@Controller
public class DevProxyController {

  //TODO#dart2 $requireDigestsPath CORS issue
  @RequestMapping(value = { "/packages/**", "main.ddc.js", "main.ddc.js.errors", "/dwds/**",
      "$requireDigestsPath" })
  public RedirectView dartDev(HttpServletRequest request) {
    String redirect = getURL(request).replace("http://localhost:5980", "http://localhost:15980");
    return new RedirectView(redirect);
  }

  private static String getURL(HttpServletRequest req) {

    String scheme = req.getScheme();             // http
    String serverName = req.getServerName();     // hostname.com
    int serverPort = req.getServerPort();        // 80
    String contextPath = req.getContextPath();   // /mywebapp
    String servletPath = req.getServletPath();   // /servlet/MyServlet
    String pathInfo = req.getPathInfo();         // /a/b;c=123
    String queryString = req.getQueryString();          // d=789

    // Reconstruct original requesting URL
    StringBuilder url = new StringBuilder();
    url.append(scheme).append("://").append(serverName);

    if (serverPort != 80 && serverPort != 443) {
      url.append(":").append(serverPort);
    }

    url.append(contextPath).append(servletPath);

    if (pathInfo != null) {
      url.append(pathInfo);
    }
    if (queryString != null) {
      url.append("?").append(queryString);
    }
    return url.toString();
  }
}