package loghours.web;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import loghours.log.EntryService;

public class EntryController implements Controller {

    private final Router router;
    private final EntryService entryService;


    public EntryController(EntryService entryService, Vertx vertx) {

        this.router = Router.router(vertx);
        this.entryService = entryService;
    }


    @Override
    public void setupRoutes() {

        router.get("/").handler(ctx -> ctx.fail(503));
    }


    @Override
    public Router getRouter() {

        return router;
    }


    @Override
    public String getMountPoint() {

        return "/entries";
    }
}
