package io.apicurio.registry.cli;

import com.fasterxml.jackson.core.type.TypeReference;
import io.apicurio.registry.rest.client.models.ArtifactReference;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.apicurio.registry.cli.utils.Mapper.MAPPER;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
public class ReferenceCommandTest extends AbstractCLITest {

    // -- Help --

    @Test
    @Order(0)
    public void testReferenceHelp() {
        testHelpCommand("reference");
        testHelpCommand("reference", "list");
    }

    // -- Setup: create artifacts with a reference --

    @Test
    @Order(1)
    public void testSetupReferencedArtifact() throws Exception {
        Path tempFile = Files.createTempFile("ref-target", ".json");
        Files.writeString(tempFile, """
                {"type": "string"}
                """);
        try {
            executeAndAssertSuccess("artifact", "create",
                    "--group", "default", "--type", "JSON",
                    "--file", tempFile.toString(), "ref-target-artifact");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    @Order(2)
    public void testSetupReferencingArtifact() throws Exception {
        Path tempFile = Files.createTempFile("ref-source", ".json");
        Files.writeString(tempFile, """
                {
                  "type": "object",
                  "properties": {
                    "name": { "$ref": "ref-target-artifact" }
                  }
                }
                """);
        try {
            executeAndAssertSuccess("artifact", "create",
                    "--group", "default", "--type", "JSON",
                    "--file", tempFile.toString(), "ref-source-artifact");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    // -- List references --

    @Test
    @Order(3)
    public void testListOutboundTable() {
        out.getBuffer().setLength(0);
        executeAndAssertSuccess("reference", "list",
                "-g", "default", "-a", "ref-source-artifact", "branch=latest");
        String output = out.toString();
        assertThat(output)
                .as(withCliOutput("Should show references or empty message"))
                .isNotBlank();
    }

    @Test
    @Order(4)
    public void testListOutboundJson() throws Exception {
        out.getBuffer().setLength(0);
        executeAndAssertSuccess("reference", "list", "-o", "json",
                "-g", "default", "-a", "ref-source-artifact", "branch=latest");
        List<ArtifactReference> refs = MAPPER.readValue(out.toString(),
                new TypeReference<>() { });
        assertThat(refs)
                .as(withCliOutput("JSON output should be a list"))
                .isNotNull();
    }

    @Test
    @Order(5)
    public void testListInbound() {
        out.getBuffer().setLength(0);
        executeAndAssertSuccess("reference", "list", "--inbound",
                "-g", "default", "-a", "ref-target-artifact", "branch=latest");
        String output = out.toString();
        assertThat(output)
                .as(withCliOutput("Should show inbound references or empty message"))
                .isNotBlank();
    }

    // -- Error cases --

    @Test
    @Order(6)
    public void testListNonExistentArtifact() {
        executeAndAssertFailure("reference", "list",
                "-g", "default", "-a", "non-existent", "branch=latest");
    }

    @Test
    @Order(7)
    public void testListMissingArtifactNoContext() {
        executeAndAssertFailure("reference", "list",
                "-g", "default", "branch=latest");
    }
}
