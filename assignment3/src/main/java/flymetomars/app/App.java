package flymetomars.app;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import flymetomars.dataaccess.ExpertiseDAO;
import flymetomars.dataaccess.MissionDAO;
import flymetomars.dataaccess.PersonDAO;
import flymetomars.dataaccess.ormlite.ExpertiseDAOImpl;
import flymetomars.dataaccess.ormlite.MisisonDaoImpl;
import flymetomars.dataaccess.ormlite.PersonDAOImpl;
import flymetomars.model.Expertise;
import flymetomars.model.Mission;
import flymetomars.model.Person;
import flymetomars.model.PersonMission;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Session;
import spark.resource.ClassPathResource;
import spark.template.freemarker.FreeMarkerEngine;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.h2.util.IOUtils.closeSilently;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.SparkBase.port;

/**
 * Created by yli on 10/03/15.
 */
public class App {
    private static PersonDAO personDao;
    private static MissionDAO missionDAO;
    private static ExpertiseDAO expertiseDao;
    private static Dao<PersonMission, Long> personMissionDao;

    // http://sparkjava.com/documentation.html
    // http://www.javacodegeeks.com/2014/06/building-a-simple-restful-api-with-spark.html
    // http://www.taywils.me/2013/11/05/javasparkframeworktutorial.html
    // http://www.boxuk.com/blog/creating-rest-api-quickly-using-pure-java/

    public static void main(String[] args) throws SQLException, IOException {

        Properties properties = loadProperties();

        int port = Integer.parseInt(properties.getProperty("spark.port"));
        port(port);

        // if in-memory database:
        //setupDB("jdbc:h2:mem:spark");
        String dbAddress = properties.getProperty("h2.dir");
        setupDB("jdbc:h2:./" + dbAddress);

        // "/person/email/:email"
        handleGetPersonByEmail();

        // "/person/:id"
        handleGetPersonById();

        // "/persons"
        handleGetPersons();

        // "/"
        handleGetIndex();

        // "/logout"
        handleGetLogout();

        // "/hello"
        handleGetHello();

        // "/register"
        handlePostRegister();

        // "/register"
        handleGetRegister();

        // "/login"
        handleGetLogin();

        // "/login"
        handlePostLogin();

        // "/missions"
        handleGetMissions();

        // "/mission/create"
        handleGetCreateMission();

        // "/mission/create"
        handlePostCreateMission();

        // "/mission/:id"
        get("/mission/:id", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            String id = req.params(":id");
            Mission mission = null;
            try {
                mission = missionDAO.load(Long.parseLong(id));
                if (null != mission) {
                    attributes.put("mission", mission);
                    List<PersonMission> personMissions = personMissionDao.queryForEq(PersonMission.MISSION_ID_FIELD_NAME, mission);
                    Set<Person> participants = new HashSet<>();
                    for (PersonMission pm : personMissions) {
                        Person person = personDao.load(pm.getPerson().getId());
                        participants.add(person);
                    }
                    attributes.put("participants", participants);
                } else {
                    attributes.put("errorMsg", "Mission with ID: " + id + " doesn't exist.");
                }
            } catch (SQLException e) {
                return handleException(res, attributes, e, "mission.html.ftl");
            }
            return new ModelAndView(attributes, "mission.html.ftl");
        }, new FreeMarkerEngine());
    }

    private static void handleGetMissions() {
        get("/missions", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            try {
                attributes.put("missions", missionDAO.getAll());
                return new ModelAndView(attributes, "missions.html.ftl");
            } catch (SQLException e) {
                return handleException(res, attributes, e, "missions.html.ftl");
            }
        }, new FreeMarkerEngine());
    }

    private static void handleGetLogout() {
        get("/logout", (req, res) -> {
            Person user = getLoggedInUser(req);
            Session session = req.session();
            if (null != session && null != user) {
                session.invalidate();
            }
            res.redirect("/");
            return "";
        });
    }

    private static Properties loadProperties() throws IOException {
        ClassPathResource resource = new ClassPathResource("app.properties");
        Properties properties = new Properties();
        InputStream stream = null;
        try {
            stream = resource.getInputStream();
            properties.load(stream);
            return properties;
        } finally {
            closeSilently(stream);
        }
    }

    private static void handleGetPersons() {
        get("/persons", (req, res) -> {
            Map<String, Object> attributes = new HashMap<String, Object>();
            try {
                attributes.put("users", personDao.getAll());
                return new ModelAndView(attributes, "persons.html.ftl");
            } catch (SQLException e) {
                return handleException(res, attributes, e, "persons.html.ftl");
            }
        }, new FreeMarkerEngine());
    }

    private static void handleGetPersonById() {
        get("/person/:id", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            Person user = getLoggedInUser(req);
            attributes.put("user", user);
            try {
                String id = req.params(":id");
                Person person = personDao.load(Long.parseLong(id));
                if (null != person) {
                    attributes.put("person", person);
                } else {
                    attributes.put("errorMsg", "No person with the ID " + id + ".");
                }
                List<PersonMission> personMissions = personMissionDao.queryForEq(PersonMission.PERSON_ID_FIELD_NAME, person);
                Set<Mission> missions = new HashSet<>();
                for (PersonMission pm : personMissions) {
                    Mission mission = missionDAO.load(pm.getMission().getId());
                    missions.add(mission);
                }
                attributes.put("missions", missions);
                return new ModelAndView(attributes, "person.html.ftl");
            } catch (Exception e) {
                return handleException(res, attributes, e, "person.html.ftl");
            }
        }, new FreeMarkerEngine());
    }

    private static void handlePostCreateMission() {
        post("/mission/create", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            String missionNameValue = req.queryParams("missionName");
            String timeValue = req.queryParams("time");
            String locationValue = req.queryParams("location");
            String descriptionValue = req.queryParams("description");

            Person user = getLoggedInUser(req);
            if (null == user) {
                attributes.clear();
                attributes.put("errorMsg", "You need to log in before creating a mission.");
                return new ModelAndView(attributes, "create_mission.html.ftl");
            } else {
                attributes.put("missionName", missionNameValue);
                attributes.put("time", timeValue);
                attributes.put("location", locationValue);
                attributes.put("description", descriptionValue);

                DateFormat df = new SimpleDateFormat("dd/mm/yyyy, HH a");
                Mission mission = new Mission();
                mission.setCaptain(user);
                mission.setName(missionNameValue);
                mission.setLocation(locationValue);
                mission.setDescription(descriptionValue);
                try {
                    PersonMission personMission = new PersonMission();
                    personMission.setMission(mission);
                    personMission.setPerson(user);
                    mission.setTime(df.parse(timeValue));
                    missionDAO.save(mission);
                    personMissionDao.create(personMission);
                    res.status(301);
                    req.session(true);
                    req.session().attribute("mission", mission);
                    res.redirect("/mission/" + mission.getId());
                    return new ModelAndView(attributes, "mission.html.ftl");
                } catch (Exception e) {
                    return handleException(res, attributes, e, "create_mission.html.ftl");
                }
            }
        }, new FreeMarkerEngine());
    }

    private static void handleGetCreateMission() {
        get("/mission/create", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("missionName", "");
            attributes.put("time", "");
            attributes.put("location", "");
            attributes.put("description", "");

            Session session = req.session();
            Person user = getLoggedInUser(req);
            if (null != user) {
                attributes.put("errorMsg", "");
            } else {
                attributes.clear();
                attributes.put("errorMsg", "You need to <a href=\"/login\">log in</a> to create a mission.");
            }

            return new ModelAndView(attributes, "create_mission.html.ftl");
        }, new FreeMarkerEngine());
    }

    private static void handlePostLogin() {
        post("/login", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            String user_name = req.queryParams("user_name");
            String password = req.queryParams("password");
            Person person = null;
            try {
                person = personDao.getPersonByEmail(user_name);
            } catch (Exception e) {
                handleException(res, attributes, e, "login.html.ftl");
            }
            if (null != person && person.getPassword().equals(password)) {
                res.status(301);
                req.session(true);
                req.session().attribute("user", person);
                res.redirect("/hello");
                return new ModelAndView(attributes, "base_page.html.ftl");
            } else {
                attributes.put("errorMsg", "Invalid email/password combination.");
                attributes.put("user_name", user_name);
                return new ModelAndView(attributes, "login.html.ftl");
            }
        }, new FreeMarkerEngine());
    }

    private static void handleGetPersonByEmail() throws SQLException {
        get("/person/email/:email", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            String email = req.params(":email");
            try {
                Person person = personDao.getPersonByEmail(email);
                if (null != person) {
                    attributes.put("user", person);
                }
                return new ModelAndView(attributes, "base_page.html.ftl");
            } catch (SQLException e) {
                return handleException(res, attributes, e, "base_page.html.ftl");
            }
        }, new FreeMarkerEngine());
    }

    private static void handleGetLogin() {
        get("/login", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            String user_name = req.params("user_name");
            if (null == user_name || user_name.trim().isEmpty()) {
                attributes.put("user_name", "");
            } else {
                attributes.put("user_name", user_name);
            }

            return new ModelAndView(attributes, "login.html.ftl");
        }, new FreeMarkerEngine());
    }

    private static void handleGetRegister() {
        get("/register", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("email", "");
            attributes.put("firstName", "");
            attributes.put("lastName", "");
            attributes.put("expertise", "");

            return new ModelAndView(attributes, "register.html.ftl");
        }, new FreeMarkerEngine());
    }

    private static void handlePostRegister() {
        post("/register", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            String email = req.queryParams("email");
            String password = req.queryParams("password");
            String firstName = req.queryParams("firstName");
            String lastName = req.queryParams("lastName");
            String expertise = req.queryParams("expertise");

            attributes.put("email", email);
            attributes.put("firstName", firstName);
            attributes.put("lastName", lastName);
            attributes.put("expertise", expertise);

            Person p = null;
            try {
                p = new Person();
                p.setEmail(email);
                p.setPassword(password);
                p.setFirstName(firstName);
                p.setLastName(lastName);
                personDao.save(p);

                Expertise exp = new Expertise();
                exp.setHolder(p);
                exp.setDescription(expertise);
                expertiseDao.save(exp);

                res.status(301);
                req.session(true);
                req.session().attribute("user", p);
                res.redirect("/hello");
                return new ModelAndView(attributes, "base_page.html.ftl");
            } catch (Exception e) {
                return handleException(res, attributes, e, "register.html.ftl");
            }
        }, new FreeMarkerEngine());
    }

    private static ModelAndView handleException(Response res, Map<String, Object> attributes, Exception e, String templateName) {
        res.status(500);
        if (e instanceof SQLException && null != e.getCause()) {
            attributes.put("errorMsg", e.getCause().getMessage());
        } else {
            attributes.put("errorMsg", e.getMessage());
        }
        e.printStackTrace();
        return new ModelAndView(attributes, templateName);
    }

    private static void handleGetIndex() {
        get("/", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            Person user = getLoggedInUser(req);
            attributes.put("user", user);
            return new ModelAndView(attributes, "base_page.html.ftl");
            //return handleBaseHelloView(req, res, attributes);
        }, new FreeMarkerEngine());
    }

    private static void handleGetHello() {
        get("/hello", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            Person user = getLoggedInUser(req);
            if (null != user) {
                attributes.put("user", user);
            }
            return new ModelAndView(attributes, "base_page.html.ftl");
        }, new FreeMarkerEngine());
    }

    private static Person getLoggedInUser(Request req) {
        Session session = req.session();
        Person user = null;
        if (null != session) {
            user = session.<Person>attribute("user");
        }
        return user;
    }

    private static void setupDB(String dbURL) throws SQLException {
        ConnectionSource connectionSource = new JdbcConnectionSource(dbURL);

        expertiseDao = new ExpertiseDAOImpl(connectionSource);
        TableUtils.createTableIfNotExists(connectionSource, Expertise.class);

        personDao = new PersonDAOImpl(connectionSource);
        TableUtils.createTableIfNotExists(connectionSource, Person.class);

        missionDAO = new MisisonDaoImpl(connectionSource);
        TableUtils.createTableIfNotExists(connectionSource, Mission.class);

        personMissionDao = DaoManager.createDao(connectionSource, PersonMission.class);
        TableUtils.createTableIfNotExists(connectionSource, PersonMission.class);
    }
}
