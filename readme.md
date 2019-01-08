# loghours

A demo project using Java 11 and the Vert.x evented io library for
a simple service that lets you check in and check out from work

## Design

- Vert.x web layer
- Vanilla busniness logic layer
- In-memory, dumbed down "database" using hash maps for storage and indeces. For more serious purposes there's a Vert.x async JDBC integration

## Running it

Vert.x comes with a lot of different ways of deployment
(the documentation is written for people who already know what to do),
and there's of course all kinds of enterprisey ways of deploying with OSGi, in clusters and so on.
You can read all about it [here](https://vertx.io/docs/vertx-core/java/#_osgi)

For our use the command `gradle run` will suffice, or alternatively run the main class `loghours.web.Main`

## TODOs

- [x] UserService
- [x] Multiple vertx routers
- [ ] Entry post actions
- [ ] Entry get actions
- [ ] Try out vertx's async plugin for JUnit to test Server verticle

