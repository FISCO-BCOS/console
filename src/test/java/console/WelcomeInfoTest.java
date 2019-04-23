package console;

import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

import console.common.WelcomeInfo;

public class WelcomeInfoTest extends TestBase{

  @Rule public final SystemOutRule log = new SystemOutRule().enableLog();

  @Test
  public void welcome() {
  	WelcomeInfo.welcome();
  	assertTrue(!"".equals(log.getLog()));
  }
  
  @Test
  public void help() {
    String[] params1 = {};
    WelcomeInfo.help(params1);
    assertTrue(!"".equals(log.getLog()));
    log.clearLog();

    String[] params2 = {"-h"};
    WelcomeInfo.help(params2);
    assertTrue(!"".equals(log.getLog()));
    log.clearLog();

    String[] params3 = {"--help"};
    WelcomeInfo.help(params3);
    assertTrue(!"".equals(log.getLog()));
    log.clearLog();

    String[] params4 = {"k"};
    WelcomeInfo.help(params4);
    assertTrue(!"".equals(log.getLog()));
  }
}
