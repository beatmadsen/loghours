package loghours.log;

import java.time.LocalDateTime;

public interface Service {

    void checkIn(String userEmail, LocalDateTime time);

    void checkOut(String userEmail, LocalDateTime time);

    void updateUser(String email, String firstName, String lastName);
}
