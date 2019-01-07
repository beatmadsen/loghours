package loghours.log;

import java.time.LocalDateTime;

public interface EntryService {

    void checkIn(String userEmail, LocalDateTime time);

    void checkOut(String userEmail, LocalDateTime time);
}
