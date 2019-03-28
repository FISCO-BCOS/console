package console;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class Web3jTest extends TestBase{

  @Rule public final SystemOutRule log = new SystemOutRule().enableLog();

  @Test
  public void getBlockNumberTest() throws IOException {
    String[] params1 = {};
    web3jFace.getBlockNumber(params1);
    assertTrue(!"".equals(log.getLog()));
    log.clearLog();

    String[] params2 = {"-h"};
    web3jFace.getBlockNumber(params2);
    assertTrue(!"".equals(log.getLog()));
    log.clearLog();

    String[] params3 = {"--help"};
    web3jFace.getBlockNumber(params3);
    assertTrue(!"".equals(log.getLog()));
    log.clearLog();

    String[] params4 = {"k"};
    web3jFace.getBlockNumber(params4);

    assertTrue(!"".equals(log.getLog()));
  }
}
