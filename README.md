# osgi-samples
Contains proof of concept samples of implementing secured REST services in OSGi environment.

## auth-httpservice
Example of public and private (secured) API implementation using OSGi native concepts:
* [HttpService](https://osgi.org/javadoc/r5/enterprise/org/osgi/service/http/HttpService.html) for registering servlets
* [HttpContext](https://osgi.org/javadoc/r5/enterprise/org/osgi/service/http/HttpContext.html) for handling authentication.
