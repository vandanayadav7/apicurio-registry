package io.apicurio.registry.cli;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
public class ImportExportCommandTest extends AbstractCLITest {

    private static Path exportFile;

    // -- Help --

    @Test
    @Order(0)
    public void testExportHelp() {
        testHelpCommand("export");
    }

    @Test
    @Order(1)
    public void testImportHelp() {
        testHelpCommand("import");
    }

    // -- Export --

    @Test
    @Order(2)
    public void testSetupArtifact() throws Exception {
        Path tempFile = Files.createTempFile("export-test-schema", ".json");
        Files.writeString(tempFile, """
                {"type": "string"}
                """);
        try {
            executeAndAssertSuccess("artifact", "create",
                    "--group", "default", "--type", "JSON",
                    "--file", tempFile.toString(), "export-test-artifact");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    @Order(3)
    public void testExport() throws Exception {
        exportFile = Files.createTempFile("registry-export-", ".zip");
        Files.deleteIfExists(exportFile);

        out.getBuffer().setLength(0);
        executeAndAssertSuccess("export", "--file", exportFile.toString());
        assertThat(out.toString())
                .as(withCliOutput("Should confirm export"))
                .contains("exported to");
        assertThat(Files.exists(exportFile))
                .as("Export file should exist")
                .isTrue();
        assertThat(Files.size(exportFile))
                .as("Export file should not be empty")
                .isGreaterThan(0);
    }

    @Test
    @Order(4)
    public void testExportWithGroup() throws Exception {
        executeAndAssertSuccess("group", "create", "export-test-group");

        Path tempFile = Files.createTempFile("export-group-schema", ".json");
        Files.writeString(tempFile, """
                {"type": "integer"}
                """);
        try {
            executeAndAssertSuccess("artifact", "create",
                    "--group", "export-test-group", "--type", "JSON",
                    "--file", tempFile.toString(), "group-artifact");
        } finally {
            Files.deleteIfExists(tempFile);
        }

        Path groupExport = Files.createTempFile("registry-group-export-", ".zip");
        Files.deleteIfExists(groupExport);

        try {
            out.getBuffer().setLength(0);
            executeAndAssertSuccess("export", "--file", groupExport.toString(),
                    "--group", "export-test-group");
            assertThat(out.toString())
                    .as(withCliOutput("Should confirm export"))
                    .contains("exported to");
            assertThat(Files.exists(groupExport))
                    .as("Group export file should exist")
                    .isTrue();
        } finally {
            Files.deleteIfExists(groupExport);
        }
    }

    // -- Import --

    @Test
    @Order(5)
    public void testImportRequiresEmptyByDefault() {
        out.getBuffer().setLength(0);
        executeAndAssertFailure("import", "--file", exportFile.toString());
    }

    @Test
    @Order(6)
    public void testImportNoRequireEmpty() {
        out.getBuffer().setLength(0);
        executeAndAssertSuccess("import", "--file", exportFile.toString(),
                "--no-require-empty");
        assertThat(out.toString())
                .as(withCliOutput("Should confirm import"))
                .contains("imported from");
    }

    @Test
    @Order(7)
    public void testImportWithPreserveIds() {
        out.getBuffer().setLength(0);
        executeAndAssertSuccess("import", "--file", exportFile.toString(),
                "--no-require-empty", "--preserve-ids");
        assertThat(out.toString())
                .as(withCliOutput("Should confirm import with preserved IDs"))
                .contains("imported from");
    }

    @Test
    @Order(8)
    public void testCleanup() throws Exception {
        if (exportFile != null) {
            Files.deleteIfExists(exportFile);
        }
    }

    // -- Error cases --

    @Test
    @Order(9)
    public void testImportNonExistentFile() {
        executeAndAssertFailure("import", "--file", "/tmp/nonexistent-file.zip");
    }

    @Test
    @Order(10)
    public void testExportMissingFile() {
        executeAndAssertFailure("export");
    }

    @Test
    @Order(11)
    public void testImportMissingFile() {
        executeAndAssertFailure("import");
    }
}
