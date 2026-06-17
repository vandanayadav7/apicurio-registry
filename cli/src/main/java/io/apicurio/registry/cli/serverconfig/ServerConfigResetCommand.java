package io.apicurio.registry.cli.serverconfig;

import io.apicurio.registry.cli.common.AbstractCommand;
import io.apicurio.registry.cli.utils.OutputBuffer;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(
        name = "reset",
        description = "Reset a server configuration property to its default value"
)
public class ServerConfigResetCommand extends AbstractCommand {

    @Parameters(
            index = "0",
            description = "The property name."
    )
    private String propertyName;

    @Override
    public void run(final OutputBuffer output) throws Exception {
        client.getRegistryClient().admin().config().properties()
                .byPropertyName(propertyName).delete();

        output.writeStdOutChunk(out ->
                out.append("Property '").append(propertyName).append("' reset to default.\n"));
    }
}
