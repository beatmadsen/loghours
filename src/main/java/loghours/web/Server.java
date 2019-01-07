package loghours.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import loghours.log.UserService;

import java.util.stream.Stream;


public class Server extends AbstractVerticle {


    @Override
    public void start() {

        var router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        Stream.of(
                new UserController(UserService.inMemory(), vertx),
                new EntryController(null, vertx)
        ).forEach(controller -> {
            controller.setupRoutes();
            router.mountSubRouter(controller.getMountPoint(), controller.getRouter());
        });

        vertx.createHttpServer().requestHandler(router).listen(8080);
    }
}
