package loghours.log;

import java.time.LocalDateTime;

public class Entry {

    private long id;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private String status;
    private User user;


    public Entry() {

    }


    public Entry(long id, LocalDateTime checkIn, LocalDateTime checkOut, String status, User user) {

        this.id = id;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = status;
        this.user = user;
    }


    public long getId() {

        return id;
    }


    public LocalDateTime getCheckIn() {

        return checkIn;
    }


    public void setCheckIn(LocalDateTime checkIn) {

        this.checkIn = checkIn;
    }


    public LocalDateTime getCheckOut() {

        return checkOut;
    }


    public void setCheckOut(LocalDateTime checkOut) {

        this.checkOut = checkOut;
    }


    public String getStatus() {

        return status;
    }


    public void setStatus(String status) {

        this.status = status;
    }


    public User getUser() {

        return user;
    }


    public void setUser(User user) {

        this.user = user;
    }
}
