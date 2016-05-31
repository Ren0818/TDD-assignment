package flymetomars.mining;

import flymetomars.dataaccess.ExpertiseDAO;
import flymetomars.dataaccess.InvitationDAO;
import flymetomars.dataaccess.MissionDAO;
import flymetomars.dataaccess.PersonDAO;
import flymetomars.model.Person;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Yuan-Fang Li
 * @version $Id: $
 */
public class EntityMinerTest {
    private EntityMiner miner;
    private PersonDAO pDao;
    private MissionDAO mDao;
    private ExpertiseDAO eDao;
    private InvitationDAO iDao;

    @Before
    public void setUp() {
        pDao = mock(PersonDAO.class);
        mDao = mock(MissionDAO.class);
        eDao = mock(ExpertiseDAO.class);
        iDao = mock(InvitationDAO.class);

        miner = new EntityMiner(pDao, mDao, eDao, iDao);
    }

    @Test
    public void onePersonIsAlsoMostPopularPerson() throws SQLException {
        // create a single person
        Person p = new Person();
        p.setEmail("abc@abc.net.au");
        ArrayList<Person> list = new ArrayList<>();
        list.add(p);

        // mock the behaviour of pDao
        when(pDao.getAll()).thenReturn(list);

        Person mostPopularPerson = miner.getMostPopularPerson();
        assertEquals("One person is also most popular", p, mostPopularPerson);
    }
}