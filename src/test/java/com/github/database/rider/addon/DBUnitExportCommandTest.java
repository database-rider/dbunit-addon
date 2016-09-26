package com.github.database.rider.addon;

import com.github.database.rider.addon.model.Tweet;
import com.github.database.rider.addon.model.User;
import com.github.database.rider.addon.ui.DBUnitExportCommand;
import org.h2.tools.Server;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.shell.test.ShellTest;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static com.github.database.rider.core.util.EntityManagerProvider.tx;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Created by pestano on 21/09/16.
 */
@RunWith(Arquillian.class)
public class DBUnitExportCommandTest {

    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final Path DATASET_HOME = Paths.get(System.getProperty("user.home") + "/generated-datasets");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final String TODAY = DATE_FORMAT.format(new Date());

    @Deployment
    @AddonDependencies
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Inject
    private ShellTest shellTest;

    @Inject
    private UITestHarness uiTestHarness;

    private static Server server;

    @BeforeClass
    public static void initDB() throws InterruptedException, SQLException {
        server = Server.createTcpServer();
        //we nned to start H2 server because test and forge plugin runs on different JVM
        server.start();
        Tweet t1 = new Tweet();
        t1.setContent("tweet 1");
        t1.setLikes(10);
        t1.setDate(Calendar.getInstance());

        Tweet t2 = new Tweet();
        t2.setContent("tweet 2");
        t2.setLikes(0);
        t2.setDate(Calendar.getInstance());

        User user1 = new User();
        user1.setName("rmpestano");
        user1.setTweets(Arrays.asList(t1, t2));
        tx("dbunit-pu").begin();
        em().persist(user1);

        User user2 = new User();
        user2.setName("user2");
        em().persist(user2);
        tx().commit();
    }

    
    @Test
    public void shouldNotExportDatasetsWithoutDatabaseUrl() throws TimeoutException {
    	Result result  = shellTest.execute("dbunit-export --name test",
                20, TimeUnit.SECONDS);

        assertThat(result.getMessage(),
                containsString("Use the 'setup' command to provide a valid database URL in order to use to plugin."));
    }

    @Test
    public void shouldExportYMLDataset() throws Exception {
        shellTest.clearScreen();
        Result result = shellTest.execute("dbunit-setup --url " +
                        "'jdbc:h2:tcp://localhost:9092/mem:test;DB_CLOSE_DELAY=-1;MODE=MySQL' --user sa",
                10, TimeUnit.SECONDS);
        assertThat(result, not(instanceOf(Failed.class)));
        assertThat(result.getMessage(),
                is(equalTo("DBUnit setup completed successfully!")));

        result = shellTest.execute("dbunit-export --name test",
                20, TimeUnit.SECONDS);

        assertThat(result.getMessage(),
                containsString("DataSet exported successfully at "));


        File generatedDataSet = new File(DATASET_HOME.toAbsolutePath().toString() + "/test.yml");
        assertThat(generatedDataSet.exists(), is(true));
        assertThat(asUTF8String(new FileInputStream(generatedDataSet)),
                containsString("tweet:" + NEW_LINE +
                        "  - id: \"2\"" + NEW_LINE +
                        "    content: \"tweet 1\"" + NEW_LINE +
                        "    date: \"" + TODAY + "\"" + NEW_LINE +
                        "    likes: 10" + NEW_LINE +
                        "    user_id: " + NEW_LINE +
                        "  - id: \"3\"" + NEW_LINE +
                        "    content: \"tweet 2\"" + NEW_LINE +
                        "    date: \"" + "" + TODAY + "\"" + NEW_LINE +
                        "    likes: 0"));

        assertThat(asUTF8String(new FileInputStream(generatedDataSet)),
                containsString("user:" + NEW_LINE +
                        "  - id: 1" + NEW_LINE +
                        "    name: \"rmpestano\"" + NEW_LINE +
                        "  - id: 4" + NEW_LINE +
                        "    name: \"user2\"" + NEW_LINE));
    }

    @Test
    public void shouldExportJSONDataset() throws Exception {
        shellTest.clearScreen();
        Result result = shellTest.execute("dbunit-setup --url " +
                        "'jdbc:h2:tcp://localhost:9092/mem:test;DB_CLOSE_DELAY=-1;MODE=MySQL' --user sa",
                10, TimeUnit.SECONDS);
        assertThat(result, not(instanceOf(Failed.class)));
        assertThat(result.getMessage(),
                is(equalTo("DBUnit setup completed successfully!")));

        result = shellTest.execute("dbunit-export --name test --format JSON",
                20, TimeUnit.SECONDS);

        assertThat(result.getMessage(),
                containsString("DataSet exported successfully at "));


        File generatedDataSet = new File(DATASET_HOME.toAbsolutePath().toString() + "/test.json");
        assertThat(generatedDataSet.exists(), is(true));
        assertThat(asUTF8String(new FileInputStream(generatedDataSet)),
                containsString("\"tweet\": [" + NEW_LINE +
                        "    {" + NEW_LINE +
                        "      \"id\": \"2\"," + NEW_LINE +
                        "      \"content\": \"tweet 1\"," + NEW_LINE +
                        "      \"date\": \"" + TODAY + "\"," + NEW_LINE +
                        "      \"likes\": 10," + NEW_LINE +
                        "    }," + NEW_LINE +
                        "    {" + NEW_LINE +
                        "      \"id\": \"3\"," + NEW_LINE +
                        "      \"content\": \"tweet 2\"," + NEW_LINE +
                        "      \"date\": \"" + TODAY + "\"," + NEW_LINE +
                        "      \"likes\": 0," + NEW_LINE +
                        "    }" + NEW_LINE +
                        "  ]"));

        assertThat(asUTF8String(new FileInputStream(generatedDataSet)),
                containsString("\"user\": [" + NEW_LINE +
                        "    {" + NEW_LINE +
                        "      \"id\": 1," + NEW_LINE +
                        "      \"name\": \"rmpestano\"" + NEW_LINE +
                        "    }," + NEW_LINE +
                        "    {" + NEW_LINE +
                        "      \"id\": 4," + NEW_LINE +
                        "      \"name\": \"user2\"" + NEW_LINE +
                        "    }" + NEW_LINE +
                        "  ]"));
    }


    @Test
    public void shouldExportOnlyUserTableUsingIncludes() throws Exception {
        shellTest.clearScreen();
        Result result = shellTest.execute("dbunit-setup --url " +
                        "'jdbc:h2:tcp://localhost:9092/mem:test;DB_CLOSE_DELAY=-1;MODE=MySQL' --user sa",
                10, TimeUnit.SECONDS);
        assertThat(result, not(instanceOf(Failed.class)));
        assertThat(result.getMessage(),
                is(equalTo("DBUnit setup completed successfully!")));

        try (CommandController controller = uiTestHarness.createCommandController(DBUnitExportCommand.class)) {
            controller.initialize();
            controller.setValueFor("name", "userDataset").
                    setValueFor("dependentTables", false).
                    setValueFor("includeTables", "user");
            result = controller.execute();
            assertThat(result, not(instanceOf(Failed.class)));
            File generatedDataSet = new File(DATASET_HOME.toAbsolutePath().toString() + "/userDataset.yml");
            assertThat(generatedDataSet.exists(), is(true));
            assertThat(asUTF8String(new FileInputStream(generatedDataSet)),
                    //should not contain tweet table
                    not(containsString("tweet:")));

            assertThat(asUTF8String(new FileInputStream(generatedDataSet)),
                    containsString("user:" + NEW_LINE +
                            "  - id: 1" + NEW_LINE +
                            "    name: \"rmpestano\"" + NEW_LINE +
                            "  - id: 4" + NEW_LINE +
                            "    name: \"user2\"" + NEW_LINE));
        }


    }

    @Test
    public void shouldExportOnlyTweetTableUsingQueryFilters() throws Exception {
        shellTest.clearScreen();
        Result result = shellTest.execute("dbunit-setup --url " +
                        "'jdbc:h2:tcp://localhost:9092/mem:test;DB_CLOSE_DELAY=-1;MODE=MySQL' --user sa",
                10, TimeUnit.SECONDS);
        assertThat(result, not(instanceOf(Failed.class)));
        assertThat(result.getMessage(),
                is(equalTo("DBUnit setup completed successfully!")));

        try (CommandController controller = uiTestHarness.createCommandController(DBUnitExportCommand.class)) {
            controller.initialize();
            controller.setValueFor("name", "tweetDataset").
                    setValueFor("dependentTables", false).
                    setValueFor("queryList", "select * from tweet t where t.id = 2");
            result = controller.execute();
            assertThat(result, not(instanceOf(Failed.class)));

            File generatedDataSet = new File(DATASET_HOME.toAbsolutePath().toString() + "/tweetDataset.yml");
            assertThat(generatedDataSet.exists(), is(true));
            assertThat(asUTF8String(new FileInputStream(generatedDataSet)),
                    //should not contain tweet table
                    is(equalTo("tweet:" + NEW_LINE +
                            "  - id: \"2\"" + NEW_LINE +
                            "    content: \"tweet 1\"" + NEW_LINE +
                            "    date: \""  + TODAY + "\"" + NEW_LINE +
                            "    likes: 10" + NEW_LINE +
                            "    user_id: " + NEW_LINE +
                            "" + NEW_LINE)));
        }
    }

    @Test
    public void shouldExportTweetAndUserTablesUsingQueryFiltersWithIncludes() throws Exception {
        shellTest.clearScreen();
        Result result = shellTest.execute("dbunit-setup --url " +
                        "'jdbc:h2:tcp://localhost:9092/mem:test;DB_CLOSE_DELAY=-1;MODE=MySQL' --user sa",
                10, TimeUnit.SECONDS);
        assertThat(result, not(instanceOf(Failed.class)));
        assertThat(result.getMessage(),
                is(equalTo("DBUnit setup completed successfully!")));

        try (CommandController controller = uiTestHarness.createCommandController(DBUnitExportCommand.class)) {
            controller.initialize();
            controller.setValueFor("name", "tweetAndUserDataset").
                    setValueFor("dependentTables", false).
                    setValueFor("includeTables", "user").
                    setValueFor("queryList", "select * from tweet t where t.id = 2");
            result = controller.execute();
            assertThat(result, not(instanceOf(Failed.class)));

            File generatedDataSet = new File(DATASET_HOME.toAbsolutePath().toString() + "/tweetAndUserDataset.yml");
            assertThat(generatedDataSet.exists(), is(true));
            assertThat(asUTF8String(new FileInputStream(generatedDataSet)),
                    is(equalTo("user:"+NEW_LINE +
                            "  - id: 1"+NEW_LINE +
                            "    name: \"rmpestano\""+NEW_LINE +
                            "  - id: 4"+NEW_LINE +
                            "    name: \"user2\""+NEW_LINE +
                            ""+NEW_LINE +
                            "tweet:"+NEW_LINE +
                            "  - id: \"2\""+NEW_LINE +
                            "    content: \"tweet 1\""+NEW_LINE +
                            "    date: \""+TODAY+"\""+NEW_LINE +
                            "    likes: 10"+NEW_LINE +
                            "    user_id: "+NEW_LINE+NEW_LINE)));
        }


    }

    @AfterClass
    public static void after() throws InterruptedException {
        if (server != null) {
            server.stop();
        }
    }


    public static String asUTF8String(InputStream in) {
        StringBuilder buffer = new StringBuilder();

        try {
            BufferedReader ioe = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            String line;
            while ((line = ioe.readLine()) != null) {
                buffer.append(line).append(NEW_LINE);
            }
        } catch (IOException var11) {
            throw new RuntimeException("Error in obtaining string from " + in, var11);
        } finally {
            try {
                in.close();
            } catch (IOException var10) {

            }

        }
        return buffer.toString();
    }
}
