package com.github.database.rider.addon.ui;

import com.github.database.rider.addon.config.DBUnitConfiguration;
import com.github.database.rider.core.api.dataset.DataSetFormat;
import com.github.database.rider.core.api.expoter.DataSetExportConfig;
import com.github.database.rider.core.exporter.DataSetExporter;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.input.ValueChangeListener;
import org.jboss.forge.addon.ui.input.events.ValueChangeEvent;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

import javax.inject.Inject;
import java.io.File;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * DBUnit: Export command
 *
 * @author <a href="mailto:rmpestano@gmail.com">Rafael Pestano</a>
 */
public class DBUnitExportCommand extends AbstractUICommand {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    @Inject
    private FacetFactory facetFactory;

    @Inject
    private DBUnitConfiguration dbunitConfiguration;

    @Inject
    private ResourceFactory resourceFactory;

    private DirectoryResource lastSelectedDir;

    @Inject
    @WithAttributes(label = "Format", description = "Output format of generated dataset. ", required = true, type = InputType.DROPDOWN)
    private UISelectOne<DataSetFormat> format;

    @Inject
    @WithAttributes(label = "Include tables", description = "Name of tables to be included in generated dataset. If empty, all tables will be included.")
    private UISelectMany<String> includeTables;

    @Inject
    @WithAttributes(label = "Output dir", description = "Output directory to generate datasets. Defaults to 'user.home/generated-datasets' dir")
    private UIInput<DirectoryResource> outputDir;

    @Inject
    @WithAttributes(label = "Name", description = "Name of generated dataset. Defauts to 'dataset-HH:mm:ss'")
    private UIInput<String> name;

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(DBUnitExportCommand.class).name("DBUnit: Export").
                category(Categories.create("DBUnit")).description("Export database tables as DBUnit datasets.");
    }


    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        format.setDefaultValue(DataSetFormat.YML);
        format.setValueChoices(Arrays.asList(DataSetFormat.values()));
        includeTables.setValue(new ArrayList<String>());
        includeTables.setValueChoices(dbunitConfiguration.getTableNames(dbunitConfiguration.getConnection()));

        builder.add(format).add(includeTables);

        if (lastSelectedDir != null) {
            outputDir.setDefaultValue(lastSelectedDir);
        } else{
            outputDir.setDefaultValue(resourceFactory.create(DirectoryResource.class, new File(System.getProperty("user.home") + "/generated-datasets").getAbsoluteFile()));
        }

        outputDir.addValueChangeListener(valueChangeEvent -> {
            if (valueChangeEvent.getNewValue() != null) {
                lastSelectedDir = (DirectoryResource) valueChangeEvent.getNewValue();
            }
        });
        builder.add(outputDir);

        name.setValue("dataset-"+sdf.format(new Date()).replaceAll(":","")+"."+format.getValue().toString().toLowerCase());

        format.addValueChangeListener(valueChangeEvent -> {
            String newName = name.getValue().substring(0, name.getValue().
                    contains(".") ? name.getValue().lastIndexOf(".") : name.getValue().length());

            name.setValue(newName + "." + valueChangeEvent.getNewValue().toString().toLowerCase());
        });


        builder.add(name);
    }


    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Connection connection = null;
        StringBuilder output = new StringBuilder();
        try {
            connection = dbunitConfiguration.getConnection();
            DirectoryResource selectedDir = outputDir.getValue();
            if(selectedDir != null){
                output.append(selectedDir.getFullyQualifiedName());
            }

            if(!output.toString().endsWith("/")){
                output.append("/");
            }

            String fileName = name.getValue();
            if(fileName == null || "".equals(fileName.toString().trim())){
                fileName = "dataset-"+sdf.format(new Date());
            }

            output.append(fileName);


            DataSetExportConfig dataSetExportConfig = new DataSetExportConfig().
                        dataSetFormat(format.getValue()).
                        outputFileName(output.toString());

            Iterator<String> iterator = includeTables.getValue().iterator();
            if (iterator.hasNext()){
                Set<String> includes = new HashSet<>();
                while(iterator.hasNext()){
                    includes.add(iterator.next());
                }
                dataSetExportConfig.includeTables(includes.toArray(new String[includes.size()]));
            }

            DataSetExporter.getInstance().export(connection, dataSetExportConfig);

        } catch (Exception e) {
            return Results.fail(e.getMessage());
        }finally {
            if(connection != null){
                connection.close();
            }
        }

        return Results.success("DataSet exported successfully at "+ output.toString());

    }

}
