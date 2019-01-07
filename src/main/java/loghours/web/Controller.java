package loghours.web;

import io.vertx.ext.web.Router;

public interface Controller {

    void setupRoutes();

    Router getRouter();

    String getMountPoint();
}
