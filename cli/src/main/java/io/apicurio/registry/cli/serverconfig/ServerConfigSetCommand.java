package io.apicurio.registry.cli.serverconfig;

import io.apicurio.registry.cli.common.AbstractCommand;
import io.apicurio.registry.cli.common.OutputTypeMixin;
import io.apicurio.registry.cli.utils.OutputBuffer;
import io.apicurio.registry.rest.client.models.UpdateConfigurationProperty;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Parameters;

@Command(
        name = "set",
        description = "Set a server configuration property"
)
public class ServerConfigSetCommand extends AbstractCommand {

    @Parameters(
            index = "0",
            description = "The property name."
    )
    private String propertyName;

    @Parameters(
            index = "1",
            description = "The property value."
    )
    private String propertyValue;

    @Mixin
    private OutputTypeMixin outputType;

    @Override
    public void run(final OutputBuffer output) throws Exception {
        final var update = new UpdateConfigurationProperty();
        update.setValue(propertyValue);

        client.getRegistryClient().admin().config().properties()
                .byPropertyName(propertyName).put(update);

        output.writeStdOutChunk(out ->
                out.append("Property '").append(propertyName).append("' set to '")
                        .append(propertyValue).append("'.\n"));

        final var prop = client.getRegistryClient().admin().config().properties()
                .byPropertyName(propertyName).get();
        ServerConfigCommand.printProperty(output, prop, outputType);
    }
}
