package io.apicurio.registry.cli;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
public class SnapshotCommandTest extends AbstractCLITest {

    @Test
    @Order(0)
    public void testSnapshotHelp() {
        testHelpCommand("admin", "snapshot");
    }

    @Test
    @Order(1)
    public void testSnapshotOnSqlStorageFails() {
        out.getBuffer().setLength(0);
        err.getBuffer().setLength(0);
        int exitCode = cmd.execute("admin", "snapshot");
        assertThat(exitCode)
                .as(withCliOutput("Snapshot on SQL storage should fail"))
                .isNotEqualTo(0);
        assertThat(err.toString())
                .as(withCliOutput("Should show an error message"))
                .isNotBlank();
    }

    @Test
    @Order(2)
    public void testSnapshotJsonOnSqlStorageFails() {
        out.getBuffer().setLength(0);
        err.getBuffer().setLength(0);
        int exitCode = cmd.execute("admin", "snapshot", "--output-type", "json");
        assertThat(exitCode)
                .as(withCliOutput("Snapshot JSON on SQL storage should fail"))
                .isNotEqualTo(0);
        assertThat(err.toString())
                .as(withCliOutput("Should show an error message"))
                .isNotBlank();
    }
}
