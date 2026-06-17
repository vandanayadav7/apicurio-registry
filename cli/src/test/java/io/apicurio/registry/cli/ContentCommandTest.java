package io.apicurio.registry.cli;

import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Locale;

import static io.apicurio.registry.cli.utils.Mapper.MAPPER;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
public class ContentCommandTest extends AbstractCLITest {

    private static final String TEST_GROUP = "content-test-group";
    private static final String TEST_ARTIFACT = "content-test-artifact";
    private static final String TEST_CONTENT = "{\"type\": \"string\"}";

    // -- Help --

    @Test
    @Order(0)
    public void testContentHelp() {
        testHelpCommand("content");
    }

    // -- Setup --

    @Test
    @Order(1)
    public void testSetup() throws Exception {
        executeAndAssertSuccess("group", "create", TEST_GROUP);
        final Path tempFile = Files.createTempFile("content-test", ".json");
        Files.writeString(tempFile, TEST_CONTENT);
        try {
            executeAndAssertSuccess("artifact", "create", "-g", TEST_GROUP,
                    "--type", "JSON", "--file", tempFile.toString(), TEST_ARTIFACT);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    // -- Retrieval --

    @Test
    @Order(2)
    public void testGetByGlobalId() throws Exception {
        final long globalId = getVersionField("globalId");

        out.getBuffer().setLength(0);
        executeAndAssertSuccess("content", "--global-id", String.valueOf(globalId));
        assertThat(out.toString().trim())
                .as(withCliOutput("Should return artifact content"))
                .contains("string");
    }

    @Test
    @Order(3)
    public void testGetByContentId() throws Exception {
        final long contentId = getVersionField("contentId");

        out.getBuffer().setLength(0);
        executeAndAssertSuccess("content", "--content-id", String.valueOf(contentId));
        assertThat(out.toString().trim())
                .as(withCliOutput("Should return artifact content"))
                .contains("string");
    }

    @Test
    @Order(4)
    public void testGetByHash() throws Exception {
        final String hash = computeContentHash();

        out.getBuffer().setLength(0);
        executeAndAssertSuccess("content", "--hash", hash);
        assertThat(out.toString().trim())
                .as(withCliOutput("Should return artifact content"))
                .contains("string");
    }

    // -- Error cases --

    @Test
    @Order(5)
    public void testContentNoArgs() {
        executeAndAssertFailure("content");
    }

    @Test
    @Order(6)
    public void testContentMultipleArgs() {
        executeAndAssertFailure("content", "--global-id", "1", "--content-id", "1");
    }

    @Test
    @Order(7)
    public void testGetByInvalidGlobalId() {
        executeAndAssertFailure("content", "--global-id", "999999999");
    }

    @Test
    @Order(8)
    public void testGetByInvalidContentId() {
        executeAndAssertFailure("content", "--content-id", "999999999");
    }

    @Test
    @Order(9)
    public void testGetByInvalidHash() {
        executeAndAssertFailure("content", "--hash", "invalidhashvalue");
    }

    // -- Helpers --

    private long getVersionField(final String fieldName) throws Exception {
        out.getBuffer().setLength(0);
        executeAndAssertSuccess("artifact", "version", "get", "-g", TEST_GROUP, "-a", TEST_ARTIFACT,
                "--output-type", "json", "1");
        final JsonNode json = MAPPER.readTree(out.toString());
        return json.get(fieldName).asLong();
    }

    private String computeContentHash() throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        final byte[] hash = digest.digest(TEST_CONTENT.getBytes(StandardCharsets.UTF_8));
        final StringBuilder sb = new StringBuilder();
        for (final byte b : hash) {
            sb.append(String.format(Locale.ROOT, "%02x", b));
        }
        return sb.toString();
    }
}
