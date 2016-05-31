package flymetomars.mining;


import flymetomars.dataaccess.ExpertiseDAO;
import flymetomars.dataaccess.InvitationDAO;
import flymetomars.dataaccess.MissionDAO;
import flymetomars.dataaccess.PersonDAO;
import flymetomars.model.Mission;
import flymetomars.model.Person;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * @author Yuan-Fang Li
 * @version $Id: $
 */
public class EntityMiner {
    private PersonDAO personDAO;
    private MissionDAO missionDAO;
    private ExpertiseDAO expertiseDAO;
    private InvitationDAO invitationDAO;

    public EntityMiner(PersonDAO personDAO, MissionDAO missionDAO, ExpertiseDAO expertiseDAO, InvitationDAO invitationDAO) {
        this.personDAO = personDAO;
        this.missionDAO = missionDAO;
        this.expertiseDAO = expertiseDAO;
        this.invitationDAO = invitationDAO;
    }

    /**
     * Return the most popular person by the count of invitations received.
     *
     * @return The most popular person.
     */
    public Person getMostPopularPerson() throws SQLException {
        List<Person> persons = personDAO.getAll();
        int maxInvites = 0;
        Person result = null;
        for (Person p : persons) {
            int noInvites = p.getInvitationsReceived().size();
            if (maxInvites <= noInvites) {
                maxInvites = noInvites;
                result = p;
            } 
        }
        return result;
    }


    /**
     * Get a list of persons of the given size with the most invitations received.
     *
     * @param size the size of the set to be returned.
     * @return the set of the most popular person by invitations received.
     */
    public List<Person> getCelebrarity(int size) {
        // TODO: implement it!
        return null;
    }

    /**
     * Get the list of the given size of persons with the most missions registered.
     *
     * @param person the person of the buddies to be returned.
     * @param size the size of the set to be returned.
     * @return the set of the busiest persons.
     */
    public List<Person> getBuddies(Person person, int size) {
        // TODO: implement it!
        return null;
    }

    /**
     * Given a person, return the largest group of persons (including this person)
     * such that each pair of persons are connected by some mission (i.e., they all
     * know each other through these missions).
     *
     * @param person the person of the social circle.
     * @Return the social circle of the given person.
     */
    public Set<Person> getSocialCircle(Person person) {
        // TODO: implement it!
        return null;
    }

    /**
     * Given a person, return the top-k upcoming missions that his/her friends
     * have been invited to but he/she hasn’t been (ranked by the number of
     * his/her friends who have been invited).
     *
     * @param person the person of the sour grape.
     * @param size the max number of upcoming missions that the person is not invited to.
     * @return the ranked list of the person's friends who are invited to
     * a mission that the person is not invited to.
     */
    public List<Mission> getSourGrapes(Person person, int size) {
        // TODO: implement it!
        return null;
    }
}
