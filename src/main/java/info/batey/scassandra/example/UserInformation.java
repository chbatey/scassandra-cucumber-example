package info.batey.scassandra.example;

public class UserInformation {
    private String firstName;
    private String lastName;

    public UserInformation(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
