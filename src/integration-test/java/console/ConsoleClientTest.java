package console;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class ConsoleClientTest extends TestBase {

    @Rule public final SystemOutRule log = new SystemOutRule().enableLog();

    @Test
    public void getBlockNumberTest() throws IOException {
        String[] params1 = {};
        consoleClientFace.getBlockNumber(params1);
        assertTrue(!"".equals(log.getLog()));
        log.clearLog();

        String[] params2 = {"-h"};
        consoleClientFace.getBlockNumber(params2);
        assertTrue(!"".equals(log.getLog()));
        log.clearLog();

        String[] params3 = {"--help"};
        consoleClientFace.getBlockNumber(params3);
        assertTrue(!"".equals(log.getLog()));
        log.clearLog();

        String[] params4 = {"k"};
        consoleClientFace.getBlockNumber(params4);

        assertTrue(!"".equals(log.getLog()));
    }
}
