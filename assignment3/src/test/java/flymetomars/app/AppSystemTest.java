package flymetomars.app;

import net.sourceforge.jwebunit.junit.JWebUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import spark.Spark;
import spark.resource.ClassPathResource;
import spark.utils.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import static org.h2.util.IOUtils.closeSilently;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Yuan-Fang Li
 * @version $Id: $
 */
public class AppSystemTest {

    @BeforeClass
    public static void setUp() throws Exception {
        ClassPathResource resource = new ClassPathResource( "app.properties" );
        Properties properties = new Properties();
        InputStream stream = null;
        try {
            stream = resource.getInputStream();
            properties.load(stream);
            int port = Integer.parseInt(properties.getProperty("spark.port"));
            JWebUnit.setBaseUrl("http://localhost:" + port);
        } finally {
            closeSilently(stream);
        }

        App.main(null);

        try {
            Thread.sleep(500);
        } catch (Exception ignored) {
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        Spark.stop();
    }

    @Test
    public void basePageShouldContainWelcome() {
        String path = "/";
        JWebUnit.beginAt(path);
        JWebUnit.assertTextPresent("Welcome");
    }
}