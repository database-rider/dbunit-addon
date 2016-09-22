package com.github.database.rider.addon.ui;

import com.github.database.rider.addon.config.DBUnitConfiguration;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

import javax.inject.Inject;

/**
 * DBUnit: Setup command
 *
 * @author <a href="mailto:rmpestano@gmail.com">Rafael Pestano</a>
 */
public class DBUnitSetupCommand extends AbstractUICommand {

    @Inject
    private FacetFactory facetFactory;


    @Inject
    DBUnitConfiguration dbunitConfiguration;

    @Inject
    @WithAttributes(label = "Url", description = "Database url, ex: jdbc:hsqldb:mem:test")
    private UIInput<String> url;

    @Inject
    @WithAttributes(label = "User", description = "Database user")
    private UIInput<String> user;

    @Inject
    @WithAttributes(type = InputType.SECRET, label = "Password", description = "Database user password")
    private UIInput<String> password;

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(DBUnitSetupCommand.class).name("DBUnit: Setup").
                category(Categories.create("DBUnit")).description("Setup database configuration.");
    }


    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        builder.add(url).add(user).add(password);
    }


    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        try {
            dbunitConfiguration.set(url.getValue(), user.getValue(), password.getValue())
            .testConnection();
        } catch (Exception e) {
            return Results.fail(e.getMessage());
        }

        return Results.success("DBUnit setup completed successfully!");

    }

}
