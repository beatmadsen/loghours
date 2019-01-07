package loghours.web;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import loghours.log.EntryService;
import loghours.log.User;
import loghours.log.UserService;

import java.util.stream.Collectors;

public class UserController implements Controller {


    private final Router router;
    private UserService userService;


    public UserController(UserService userService, Vertx vertx) {

        this.userService = userService;
        this.router = Router.router(vertx);
    }


    @Override
    public void setupRoutes() {

        router.get("/").handler(this::handleGetUsers);
        router.post("/").blockingHandler(this::handlePostUser);
        router.get("/:id").handler(this::handleGetUser);
        router.put("/:id").blockingHandler(this::handlePutUser);
    }


    private void handleGetUsers(RoutingContext routingContext) {

        var emails = routingContext.queryParam("email");
        var response = routingContext.response();
        if (emails.size() == 1) {
            var users = userService.findAll(emails.get(0)).map(UserController::toJson).collect(Collectors.toList());
            // index type REST action expects array output
            var json = new JsonArray(users);
            response.putHeader("content-type", "application/json").end(json.encodePrettily());
        } else {
            response.setStatusCode(400).end();
        }
    }


    private void handlePostUser(RoutingContext routingContext) {

        var user = fromJson(routingContext.getBodyAsJson());
        var response = routingContext.response();

        userService.create(user).ifPresentOrElse(
                saved -> {
                    var jsonOut = toJson(saved).encodePrettily();
                    response.putHeader("content-type", "application/json").end(jsonOut);
                },
                () -> response.setStatusCode(409).end()
        );
    }


    private void handleGetUser(RoutingContext routingContext) {

        try {
            var id = Long.parseLong(routingContext.pathParam("id"));
            var response = routingContext.response();

            userService.find(id).map(UserController::toJson).ifPresentOrElse(
                    json -> response
                            .putHeader("content-type", "application/json")
                            .end(json.encodePrettily()),
                    () -> response.setStatusCode(404).end()
            );
        } catch (NumberFormatException e) {
            routingContext.response().setStatusCode(400).end();
        }
    }


    private void handlePutUser(RoutingContext routingContext) {

        try {
            var id = Long.parseLong(routingContext.pathParam("id"));
            var response = routingContext.response();
            var delta = fromJson(routingContext.getBodyAsJson());

            userService.update(id, delta).ifPresentOrElse(
                    updated -> {
                        var jsonOut = toJson(updated).encodePrettily();
                        response.putHeader("content-type", "application/json")
                                .end(jsonOut);
                    },
                    () -> response.setStatusCode(404).end()
            );
        } catch (NumberFormatException e) {
            routingContext.response().setStatusCode(400).end();
        }
    }


    private static JsonObject toJson(User user) {

        return new JsonObject()
                .put("id", user.getId())
                .put("email", user.getEmail())
                .put("first_name", user.getFirstName())
                .put("last_name", user.getLastName())
                ;
    }


    private static User fromJson(JsonObject json) {

        var user = new User();
        user.setEmail(json.getString("email"));
        user.setLastName(json.getString("last_name"));
        user.setFirstName(json.getString("first_name"));
        return user;
    }


    @Override
    public Router getRouter() {

        return router;
    }


    @Override
    public String getMountPoint() {

        return "/users";
    }
}
