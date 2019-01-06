package loghours.log;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public interface UserRepository {

    static UserRepository inMemory() {

        return new InMemory();
    }

    User findOrCreate(String email);

    Optional<User> find(long id);

    Optional<User> find(String email);

    User save(User user);

    class InMemory implements UserRepository {

        private final AtomicLong primaryKey = new AtomicLong(1L);
        private final Map<Long, User> usersById = new HashMap<>();
        private final Map<String, Long> emailIndex = new HashMap<>();


        @Override
        public synchronized User findOrCreate(String email) {

            var id = emailIndex.get(email);
            if (id == null) {
                var nextId = primaryKey.getAndIncrement();
                var user = new User(nextId, email, null, null);
                storeAndRefreshIndex(user);
                return user;
            }
            return usersById.get(id);
        }


        private void storeAndRefreshIndex(User user) {

            usersById.put(user.getId(), user);
            emailIndex.put(user.getEmail(), user.getId());
        }


        @Override
        public Optional<User> find(long id) {

            return Optional.ofNullable(usersById.get(id));
        }


        @Override
        public Optional<User> find(String email) {

            return Optional.ofNullable(emailIndex.get(email))
                    .map(id -> Optional.ofNullable(usersById.get(id)).orElseThrow());
        }


        @Override
        public synchronized User save(User user) {

            if (user.getEmail() == null) throw new RuntimeException("Email is required");
            if (user.getId() == 0L) {
                return saveNew(user);
            }
            return saveChanged(user);
        }


        private User saveNew(User user) {

            var nextId = primaryKey.getAndIncrement();
            var copy = copy(user, nextId);
            storeAndRefreshIndex(copy);
            return copy;
        }


        private User saveChanged(User user) {

            var id = emailIndex.get(user.getEmail());
            if (id == null) {
                // email was changed, new email not found
                purgeFromIndex(user);
            } else if (id != user.getId()) {
                // email changed to another user's existing email
                throw new RuntimeException("email already exists");
            }
            storeAndRefreshIndex(user);
            return user;
        }


        private static User copy(User original, long nextId) {

            return new User(nextId, original.getEmail(), original.getFirstName(), original.getLastName());
        }


        private void purgeFromIndex(User user) {

            emailIndex.entrySet()
                    .stream()
                    .filter(e -> user.getId() == e.getValue())
                    .findFirst()
                    .map(Map.Entry::getKey)
                    .ifPresent(emailIndex::remove);
        }
    }
}
