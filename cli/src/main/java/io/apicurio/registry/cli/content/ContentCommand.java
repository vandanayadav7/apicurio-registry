package io.apicurio.registry.cli.content;

import io.apicurio.registry.cli.common.AbstractCommand;
import io.apicurio.registry.cli.common.CliException;
import io.apicurio.registry.cli.utils.OutputBuffer;
import io.apicurio.registry.rest.client.RegistryClient;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static io.apicurio.registry.cli.common.CliException.APPLICATION_ERROR_RETURN_CODE;
import static io.apicurio.registry.cli.common.CliException.VALIDATION_ERROR_RETURN_CODE;

@Command(
        name = "content",
        description = "Retrieve artifact content by global ID, content ID, or SHA-256 hash"
)
public class ContentCommand extends AbstractCommand {

    @Option(
            names = {"--global-id"},
            description = "Retrieve content by global ID."
    )
    private Long globalId;

    @Option(
            names = {"--content-id"},
            description = "Retrieve content by content ID."
    )
    private Long contentId;

    @Option(
            names = {"--hash"},
            description = "Retrieve content by SHA-256 hash."
    )
    private String contentHash;

    @Override
    public void run(final OutputBuffer output) throws Exception {
        final var registryClient = client.getRegistryClient();

        if (globalId == null && contentId == null && contentHash == null) {
            throw new CliException("Specify --global-id, --content-id, or --hash.",
                    VALIDATION_ERROR_RETURN_CODE);
        }
        if ((globalId != null ? 1 : 0) + (contentId != null ? 1 : 0) + (contentHash != null ? 1 : 0) > 1) {
            throw new CliException("Specify only one of --global-id, --content-id, or --hash.",
                    VALIDATION_ERROR_RETURN_CODE);
        }

        try (final InputStream content = fetchContent(registryClient)) {
            final var text = new String(content.readAllBytes(), StandardCharsets.UTF_8);
            output.writeStdOutChunk(out -> {
                out.append(text);
                if (!text.endsWith("\n")) {
                    out.append('\n');
                }
            });
        } catch (IOException ex) {
            throw new CliException("Could not read content.", ex, APPLICATION_ERROR_RETURN_CODE);
        }
    }

    private InputStream fetchContent(final RegistryClient registryClient) {
        if (globalId != null) {
            return registryClient.ids().globalIds().byGlobalId(globalId).get();
        } else if (contentId != null) {
            return registryClient.ids().contentIds().byContentId(contentId).get();
        } else {
            return registryClient.ids().contentHashes().byContentHash(contentHash).get();
        }
    }
}
