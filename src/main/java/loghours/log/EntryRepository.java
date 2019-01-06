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

    void save(Entry entry);

    Optional<Entry> findCheckedIn(String userEmail, LocalDateTime dayOfCheckIn);

    List<Entry> findAllInWeek(String userEmail, LocalDateTime timeInWeek);


    class InMemory implements EntryRepository {

        private final UserRepository userRepository;

        private final AtomicLong primaryKey = new AtomicLong(1L);
        private final Map<Long, Entry> entriesById = new HashMap<>();
        private final Map<String, Set<Long>> emailIndex = new HashMap<>();


        public InMemory(UserRepository userRepository) {

            this.userRepository = userRepository;
        }


        private static Entry copy(Entry original, long newId) {

            return new Entry(newId, original.getCheckIn(), original.getCheckOut(), original.getStatus(), original.getUser());
        }


        private static Set<Long> mergeSets(Set<Long> a, Set<Long> b) {

            return Stream.concat(a.stream(), b.stream()).collect(Collectors.toSet());
        }


        @Override
        public synchronized void save(Entry entry) {

            var user = entry.getUser();
            if (user == null) throw new RuntimeException("User is required");
            userRepository.save(user);
            if (entry.getId() == 0L) {
                saveNew(entry);
            } else {
                saveChanged(entry);
            }
        }


        private void saveNew(Entry entry) {

            var nextId = primaryKey.getAndIncrement();
            var copy = copy(entry, nextId);
            storeAndRefreshIndex(copy);
        }


        private void saveChanged(Entry entry) {

            var ids = emailIndex.get(entry.getUser().getEmail());
            if (ids == null || ids.isEmpty()) {
                // email was changed, new email not found
                purgeFromIndex(entry);
            } else if (!ids.contains(entry.getId())) {
                // email was changed, but there were existing entries matching new email
                throw new RuntimeException("email already exists");
            }
            storeAndRefreshIndex(entry);
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
        public Optional<Entry> findCheckedIn(String userEmail, LocalDateTime dayOfCheckIn) {

            var start = dayOfCheckIn.truncatedTo(ChronoUnit.DAYS);
            var stop = start.plusDays(1);

            return findEntries(userEmail).stream()
                    .filter(entry -> entry.getCheckIn().isAfter(start) && entry.getCheckIn().isBefore(stop))
                    .findFirst();
        }


        private synchronized List<Entry> findEntries(String userEmail) {

            return emailIndex.getOrDefault(userEmail, Set.of())
                    .stream()
                    .map(entriesById::get)
                    .collect(Collectors.toList());
        }


        @Override
        public List<Entry> findAllInWeek(String userEmail, LocalDateTime weekOfCheckIn) {

            var start = weekOfCheckIn.truncatedTo(ChronoUnit.DAYS).with(DayOfWeek.MONDAY);
            var stop = start.plusDays(7);

            List<Entry> entries = findEntries(userEmail);

            return entries.stream()
                    .filter(entry -> entry.getCheckIn().isAfter(start) && entry.getCheckIn().isBefore(stop))
                    .collect(Collectors.toList());
        }
    }
}
