package loghours.log;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface EntryRepository {

    static EntryRepository inMemory() {

        return new InMemory(UserRepository.inMemory());
    }

    Entry save(Entry entry);

    Optional<Entry> findByDay(String userEmail, LocalDateTime dayOfCheckIn);

    List<Entry> findAllInWeek(String userEmail, LocalDateTime timeInWeek);


    class InMemory implements EntryRepository {

        private final UserRepository userRepository;

        private final AtomicLong primaryKey = new AtomicLong(1L);
        private final Map<Long, Entry> entriesById = new HashMap<>();
        private final Map<String, Set<Long>> emailIndex = new HashMap<>();


        public InMemory(UserRepository userRepository) {

            this.userRepository = userRepository;
        }


        private static Entry copy(Entry original, long newId, User newUser) {

            return new Entry(newId, original.getCheckIn(), original.getCheckOut(), original.getStatus(), newUser);
        }


        private static Set<Long> mergeSets(Set<Long> a, Set<Long> b) {

            return Stream.concat(a.stream(), b.stream()).collect(Collectors.toSet());
        }


        @Override
        public synchronized Entry save(Entry entry) {

            var user = entry.getUser();
            if (user == null) throw new RuntimeException("User is required");

            if (entry.getId() == 0L) {
                return saveNew(entry);
            }
            return saveChanged(entry);
        }


        private Entry saveNew(Entry entry) {

            var nextId = primaryKey.getAndIncrement();
            var userCopy = userRepository.save(entry.getUser());
            // a matter of taste, but prefer to avoid side-effecting on input
            var copy = copy(entry, nextId, userCopy);
            storeAndRefreshIndex(copy);
            return copy;
        }


        private Entry saveChanged(Entry entry) {

            var ids = emailIndex.get(entry.getUser().getEmail());
            if (ids == null || ids.isEmpty()) {
                // email was changed, new email not found
                purgeFromIndex(entry);
            } else if (!ids.contains(entry.getId())) {
                // email was changed, but there were existing entries matching new email
                throw new RuntimeException("email already exists");
            }
            var userCopy = userRepository.save(entry.getUser());

            // a matter of taste, but prefer to avoid side-effecting on input
            var copy = copy(entry, userCopy);

            storeAndRefreshIndex(copy);
            return copy;
        }


        private void purgeFromIndex(Entry entry) {

            emailIndex.forEach((k, ids) -> ids.remove(entry.getId()));
        }


        private void storeAndRefreshIndex(Entry entry) {

            entriesById.put(entry.getId(), entry);
            emailIndex.merge(
                    entry.getUser().getEmail(),
                    Set.of(entry.getId()),
                    InMemory::mergeSets
            );
        }


        @Override
        public Optional<Entry> findByDay(String userEmail, LocalDateTime dayOfCheckIn) {

            var start = dayOfCheckIn.truncatedTo(ChronoUnit.DAYS);
            var stop = start.plusDays(1);

            return findEntries(userEmail, start, stop).findFirst();
        }


        private Stream<Entry> findEntries(String userEmail, LocalDateTime start, LocalDateTime stop) {

            return findEntries(userEmail).stream()
                    .filter(entry -> entry.getCheckIn().isAfter(start) && entry.getCheckIn().isBefore(stop))
                    .map(this::refreshUser);
        }


        /*
         * NB: returns a list rather than a stream,
         * because evaluation/resolution of lazy stream needs to happen
         * inside synchronized scope
         */
        private synchronized List<Entry> findEntries(String userEmail) {

            return emailIndex.getOrDefault(userEmail, Set.of())
                    .stream()
                    .map(entriesById::get)
                    .collect(Collectors.toList());
        }


        private Entry refreshUser(Entry entry) {

            var user = userRepository.find(entry.getId()).orElseThrow();
            return copy(entry, user);
        }


        private static Entry copy(Entry entry, User newUser) {

            return new Entry(entry.getId(), entry.getCheckIn(), entry.getCheckOut(), entry.getStatus(), newUser);
        }


        @Override
        public List<Entry> findAllInWeek(String userEmail, LocalDateTime weekOfCheckIn) {

            var start = weekOfCheckIn.truncatedTo(ChronoUnit.DAYS).with(DayOfWeek.MONDAY);
            var stop = start.plusDays(7);

            return findEntries(userEmail, start, stop)
                    .collect(Collectors.toList());
        }
    }
}
