package com.github.database.rider.addon;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.shell.test.ShellTest;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

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
