package flymetomars.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by yli on 10/03/15.
 */
@DatabaseTable(tableName = "missions")
public class Mission extends SeriablizableEntity {
    @DatabaseField(canBeNull = false)
    private Date time;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Person captain;

    @DatabaseField(canBeNull = false)
    private String location;

    @DatabaseField()
    private String description;

    private Set<Invitation> invitationSet;

    public Mission() {
        invitationSet = new HashSet<Invitation>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Invitation> getInvitationSet() {
        return invitationSet;
    }

    public void setInvitationSet(Set<Invitation> invitationSet) {
        this.invitationSet = invitationSet;
    }

    public Person getCaptain() {
        return captain;
    }

    public void setCaptain(Person captain) {
        this.captain = captain;
    }
}
