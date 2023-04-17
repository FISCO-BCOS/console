package console.common;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Common {

    private Common() {}

    public static final String CONTRACT_LOG_FILE_NAME = "deploylog.txt";

    // SystemConfig key
    public static final String TX_COUNT_LIMIT = "tx_count_limit";
    public static final String TX_GAS_LIMIT = "tx_gas_limit";
    public static final String CONSENSUS_LEADER_PERIOD = "consensus_leader_period";
    public static final String COMPATIBILITY_VERSION = "compatibility_version";
    public static final String AUTH_CHECK_STATUS = "auth_check_status";

    public static final List<String> SUPPORTED_SYSTEM_KEYS =
            new ArrayList<>(
                    Arrays.asList(
                            TX_COUNT_LIMIT,
                            TX_GAS_LIMIT,
                            CONSENSUS_LEADER_PERIOD,
                            COMPATIBILITY_VERSION,
                            AUTH_CHECK_STATUS));

    public static final int INVALID_RETURN_NUMBER = -100;
    public static final long INVALID_LONG_VALUE = Long.MAX_VALUE;

    public static final int QUERY_LOG_COUNT = 20;
    public static final int LOG_MAX_COUNT = 10000;
    public static final String NON_NEGATIVE_INTEGER_RANGE = "from 0 to 2147483647";
    public static final String DEPLOY_LOG_INTEGER_RANGE = "from 1 to 100";
    public static final String TX_GAS_LIMIT_RANGE = "must be greater than 100000";
    public static final String SYS_CONFIG_RANGE = "must be greater than 1";
    public static final String COMPATIBILITY_VERSION_DESC =
            "must be in this format: 3.0.0, 3.1.0, etc. Latest version now is "
                    + ConsoleVersion.Version;
    public static final String EMPTY_CONTRACT_ADDRESS =
            "0x0000000000000000000000000000000000000000";
    // BFS common
    public static final String BFS_TYPE_DIR = "directory";
    public static final String BFS_TYPE_CON = "contract";
    public static final String BFS_TYPE_LNK = "link";
    public static final BigInteger LS_DEFAULT_COUNT = BigInteger.valueOf(500);
}
