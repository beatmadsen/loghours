package loghours.log;

import java.util.Optional;
import java.util.stream.Stream;

public interface UserService {

    static UserService inMemory() {

        return new InMemory(UserRepository.inMemory());
    }

    Stream<User> findAll(String email);

    Optional<User> find(Long id);

    /**
     * Create a user with given data except id
     *
     * @param user the user data
     * @return Present new user with new id if successful, absent if user already exists.
     * Nb this use of optional as an either monad is slightly non-intuitive
     */
    Optional<User> create(User user);

    /**
     * Create a user with given data except id
     *
     * @param delta the updated data
     * @param id    the user id
     * @return Present, updated user if user with id found, absent if user didn't exists.
     * Nb this use of optional as an either monad is slightly non-intuitive
     */
    Optional<User> update(long id, User delta);

    class InMemory implements UserService {

        private final UserRepository repository;


        public InMemory(UserRepository repository) {

            this.repository = repository;
        }


        @Override
        public Stream<User> findAll(String email) {

            return repository.find(email).stream();
        }


        @Override
        public Optional<User> find(Long id) {

            return repository.find(id);
        }


        @Override
        public Optional<User> create(User user) {

            if (repository.find(user.getEmail()).isPresent()) {
                return Optional.empty();
            }
            return Optional.of(repository.save(user.withId(0L)));
        }


        @Override
        public Optional<User> update(long id, User delta) {

            return repository.find(id).map(found -> repository.save(delta.withId(id)));
        }
    }
}
