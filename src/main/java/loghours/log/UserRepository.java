package loghours.log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public interface UserRepository {

    static UserRepository inMemory() {

        return new InMemory();
    }

    User findOrCreate(String email);

    void save(User user);

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
        public synchronized void save(User user) {

            if (user.getEmail() == null) throw new RuntimeException("Email is required");
            if (user.getId() == 0L) {
                saveNew(user);
            } else {
                saveChanged(user);
            }
        }


        private void saveNew(User user) {

            var nextId = primaryKey.getAndIncrement();
            var copy = copy(user, nextId);
            storeAndRefreshIndex(copy);
        }


        private void saveChanged(User user) {
            var id = emailIndex.get(user.getEmail());
            if (id == null) {
                // email was changed, new email not found
                purgeFromIndex(user);
            } else if (id != user.getId()) {
                // email changed to another user's existing email
                throw new RuntimeException("email already exists");
            }
            storeAndRefreshIndex(user);
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
