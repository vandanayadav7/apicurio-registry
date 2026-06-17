package io.apicurio.registry.cli.admin;

import io.apicurio.registry.cli.common.AbstractCommand;
import io.apicurio.registry.cli.common.CliException;
import io.apicurio.registry.cli.common.OutputTypeMixin;
import io.apicurio.registry.cli.utils.OutputBuffer;
import io.apicurio.registry.cli.utils.TableBuilder;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import static io.apicurio.registry.cli.common.CliException.APPLICATION_ERROR_RETURN_CODE;
import static io.apicurio.registry.cli.utils.Columns.FIELD;
import static io.apicurio.registry.cli.utils.Columns.SNAPSHOT_ID;
import static io.apicurio.registry.cli.utils.Columns.VALUE;
import static io.apicurio.registry.cli.utils.Mapper.MAPPER;

@Command(
        name = "snapshot",
        description = "Trigger a storage snapshot (KafkaSQL only)"
)
public class SnapshotCommand extends AbstractCommand {

    @Mixin
    private OutputTypeMixin outputType;

    @Override
    public void run(final OutputBuffer output) throws Exception {
        final var snapshot = client.getRegistryClient().admin().snapshots().post();
        if (snapshot == null || snapshot.getSnapshotId() == null) {
            throw new CliException("Snapshot request did not return a snapshot ID. "
                    + "This operation may only be supported with KafkaSQL storage.",
                    APPLICATION_ERROR_RETURN_CODE);
        }

        output.writeStdOutChunkWithException(out -> {
            switch (outputType.getOutputType()) {
                case json -> {
                    out.append(MAPPER.writeValueAsString(snapshot));
                    out.append('\n');
                }
                case table -> {
                    final var table = new TableBuilder();
                    table.addColumns(FIELD, VALUE);
                    table.addRow(SNAPSHOT_ID, snapshot.getSnapshotId());
                    table.print(out);
                    out.append("Snapshot triggered successfully.\n");
                }
            }
        });
    }
}
