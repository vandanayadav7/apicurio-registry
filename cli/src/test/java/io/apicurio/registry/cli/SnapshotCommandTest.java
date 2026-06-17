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
        testHelpCommand("snapshot");
    }

    @Test
    @Order(1)
    public void testSnapshotTrigger() {
        out.getBuffer().setLength(0);
        int exitCode = cmd.execute("snapshot");
        assertThat(exitCode)
                .as(withCliOutput("Snapshot on SQL storage should fail with server error"))
                .isIn(0, 1, 3);
    }

    @Test
    @Order(2)
    public void testSnapshotJsonOutput() {
        out.getBuffer().setLength(0);
        int exitCode = cmd.execute("snapshot", "--output-type", "json");
        assertThat(exitCode)
                .as(withCliOutput("Snapshot JSON on SQL storage should fail with server error"))
                .isIn(0, 1, 3);
    }
}
