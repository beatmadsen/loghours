package loghours.log;

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


    public long getId() {

        return id;
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
