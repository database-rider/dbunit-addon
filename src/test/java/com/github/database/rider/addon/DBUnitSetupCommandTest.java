package com.github.database.rider.addon;

import com.github.database.rider.addon.model.Tweet;
import com.github.database.rider.addon.model.User;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.shell.test.ShellTest;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Created by pestano on 21/09/16.
 */
@RunWith(Arquillian.class)
public class DBUnitSetupCommandTest {


    @Deployment
    @AddonDependencies
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Inject
    private ShellTest shellTest;


    @BeforeClass
    public static void initDB() throws InterruptedException {
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
        em("dbunit-pu").persist(user1);

        User user2 = new User();
        user2.setName("user2");
        em().persist(user2);
    }


    @Test
    public void shouldSetupDatabase() throws Exception {
        shellTest.clearScreen();
        Result result = shellTest.execute("dbunit-setup --url 'jdbc:hsqldb:mem:flyway;DB_CLOSE_DELAY=-1' --user sa",
                10, TimeUnit.SECONDS);
        assertThat(result, not(instanceOf(Failed.class)));
        assertThat(result.getMessage(),
                is(equalTo("DBUnit setup completed successfully!")));
    }

    @Test
    public void shouldNotSetupDatabaseWithInvalidConnection() throws Exception {
        shellTest.clearScreen();
        Result result = shellTest.execute("dbunit-setup --url 'jdbc:xyz:mem:sd;DB_CLOSE_DELAY=-1' --user sa",
                10, TimeUnit.SECONDS);
        assertThat(result, is(instanceOf(Failed.class)));
        assertThat(result.getMessage(),
                is(equalTo("Could not acquire jdbc connection for current configuration: Url: jdbc:xyz:mem:sd;DB_CLOSE_DELAY=-1, User: sa, Driver class: . No suitable driver found for jdbc:xyz:mem:sd;DB_CLOSE_DELAY=-1")));
    }
}
