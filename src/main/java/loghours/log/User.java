package loghours.log;

import java.util.Objects;

public class User {

    private long id;
    private String email;
    private String firstName;
    private String lastName;
    public User(long id, String email, String firstName, String lastName) {

        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }


    public User() {

    }

    public User withId(long id) {
        return new User(id, email, firstName, lastName);
    }


    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id &&
                email.equals(user.email) &&
                Objects.equals(firstName, user.firstName) &&
                Objects.equals(lastName, user.lastName);
    }


    @Override
    public int hashCode() {

        return Objects.hash(id, email, firstName, lastName);
    }


    public long getId() {

        return id;
    }


    public void setId(long id) {

        this.id = id;
    }


    public String getEmail() {

        return email;
    }


    public void setEmail(String email) {

        this.email = email;
    }


    public String getFirstName() {

        return firstName;
    }


    public void setFirstName(String firstName) {

        this.firstName = firstName;
    }


    public String getLastName() {

        return lastName;
    }


    public void setLastName(String lastName) {

        this.lastName = lastName;
    }
}
