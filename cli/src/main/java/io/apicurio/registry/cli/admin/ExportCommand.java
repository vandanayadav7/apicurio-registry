package io.apicurio.registry.cli.admin;

import io.apicurio.registry.cli.common.AbstractCommand;
import io.apicurio.registry.cli.common.CliException;
import io.apicurio.registry.cli.utils.OutputBuffer;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static io.apicurio.registry.cli.common.CliException.APPLICATION_ERROR_RETURN_CODE;

@Command(
        name = "export",
        description = "Export registry data to a ZIP file"
)
public class ExportCommand extends AbstractCommand {

    @Option(
            names = {"-f", "--file"},
            required = true,
            description = "Output file path for the exported ZIP archive."
    )
    private String file;

    @Option(
            names = {"-g", "--group"},
            description = "Export only data belonging to this group."
    )
    private String groupId;

    @Override
    public void run(final OutputBuffer output) throws Exception {
        final var registryClient = client.getRegistryClient();

        final var downloadRef = registryClient.admin().export().get(r -> {
            r.queryParameters.forBrowser = true;
            r.queryParameters.groupId = groupId;
        });

        if (downloadRef == null || downloadRef.getHref() == null) {
            throw new CliException("Server did not return a download reference.",
                    APPLICATION_ERROR_RETURN_CODE);
        }

        final var baseUrl = resolveBaseUrl();
        final var downloadUrl = baseUrl + downloadRef.getHref();

        downloadFile(downloadUrl, Path.of(file));

        output.writeStdOutChunk(out ->
                out.append("Registry data exported to '").append(file).append("'.\n"));
    }

    private String resolveBaseUrl() {
        final var configModel = config.read();
        final var contextName = configModel.getCurrentContext();
        final var context = configModel.getContext().get(contextName);
        final var registryUrl = context.getRegistryUrl();
        final var suffix = "/apis/registry/v3";
        final var idx = registryUrl.indexOf(suffix);
        if (idx >= 0) {
            return registryUrl.substring(0, idx);
        }
        return registryUrl;
    }

    private static void downloadFile(final String url, final Path outputPath) {
        try {
            final var httpClient = HttpClient.newHttpClient();
            final var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(outputPath));
            if (response.statusCode() != 200) {
                throw new CliException("Failed to download export: HTTP " + response.statusCode(),
                        APPLICATION_ERROR_RETURN_CODE);
            }
        } catch (IOException ex) {
            throw new CliException("Failed to download export file: " + ex.getMessage(), ex,
                    APPLICATION_ERROR_RETURN_CODE);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new CliException("Download interrupted.", ex, APPLICATION_ERROR_RETURN_CODE);
        }
    }
}
