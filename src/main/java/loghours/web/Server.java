package loghours.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import loghours.log.EntryRepository;
import loghours.log.User;
import loghours.log.UserRepository;

import java.util.stream.Collectors;


public class Server extends AbstractVerticle {

    private UserRepository userRepository;
    private EntryRepository entryRepository;


    @Override
    public void start() {

        userRepository = UserRepository.inMemory();
        entryRepository = EntryRepository.inMemory(userRepository);

        var router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.get("/users").handler(this::handleGetUsers);
        router.post("/users").handler(this::handlePostUser);
        router.get("/users/:id").handler(this::handleGetUser);
        router.put("/users/:id").handler(this::handlePutUser);

        vertx.createHttpServer().requestHandler(router).listen(8080);
    }


    private void handleGetUsers(RoutingContext routingContext) {

        var emails = routingContext.queryParam("email");
        var response = routingContext.response();
        if (emails.size() == 1) {
            var users = userRepository.find(emails.get(0))
                    .stream()
                    .map(Server::toJson)
                    .collect(Collectors.toList());
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

        userRepository.find(user.getEmail()).ifPresentOrElse(
                u -> response.setStatusCode(409).end(),
                () -> {
                    var json = toJson(userRepository.save(user));
                    response.putHeader("content-type", "application/json")
                            .end(json.encodePrettily());
                }
        );
    }


    private void handleGetUser(RoutingContext routingContext) {

        try {
            var id = Long.parseLong(routingContext.pathParam("id"));
            var response = routingContext.response();

            userRepository.find(id).map(Server::toJson).ifPresentOrElse(
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

            userRepository.find(id).ifPresentOrElse(
                    u -> {
                        var json = updateUser(id, routingContext.getBodyAsJson());
                        response.putHeader("content-type", "application/json")
                                .end(json.encodePrettily());
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


    private JsonObject updateUser(long id, JsonObject body) {

        var delta = fromJson(body);
        delta.setId(id);
        return toJson(userRepository.save(delta));
    }
}
