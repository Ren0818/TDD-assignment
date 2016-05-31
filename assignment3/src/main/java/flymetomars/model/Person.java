package flymetomars.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by yli on 10/03/15.
 */
@DatabaseTable(tableName = "persons")
public class Person extends SeriablizableEntity {
    @DatabaseField
    private String firstName;

    @DatabaseField(canBeNull = false)
    private String lastName;

    @DatabaseField(canBeNull = false, unique = true)
    private String email;

    @DatabaseField(canBeNull = false)
    private String password;

    @ForeignCollectionField
    private Collection<Expertise> expertise;

    private Collection<Invitation> invitationsReceived;

    public Person() {
        expertise = new HashSet<>();
        invitationsReceived = new HashSet<>();
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (null == password) {
            throw new IllegalArgumentException("Password cannot be null.");
        } else if (password.trim().equals("")) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Collection<Invitation> getInvitationsReceived() {
        return invitationsReceived;
    }

    public void setInvitationsReceived(Collection<Invitation> invitationsReceived) {
        this.invitationsReceived = invitationsReceived;
    }

    public Collection<Expertise> getExpertise() {
        return expertise;
    }

    public void setExpertise(Collection<Expertise> expertise) {
        this.expertise = expertise;
    }

    public void addExpertise(Expertise exp) {
        if (null == exp) {
            throw new IllegalArgumentException("Expertise cannot be null.");
        } else if (null == exp.getDescription()) {
            throw new IllegalArgumentException("Expertise cannot have null description.");
        } else if (exp.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Expertise cannot have empty description.");
        }
        expertise.add(exp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Person person = (Person) o;

        return email.equals(person.email);
    }

    @Override
    public int hashCode() {
        return 31 * email.hashCode();
    }
}
