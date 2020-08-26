package console;

import static org.junit.Assert.assertTrue;

import console.common.WelcomeInfo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class WelcomeInfoTest extends TestBase {

    @Rule public final SystemOutRule log = new SystemOutRule().enableLog();

    @Test
    public void welcome() {
        WelcomeInfo.welcome();
        assertTrue(!"".equals(log.getLog()));
    }
}
