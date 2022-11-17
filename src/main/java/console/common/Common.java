package console.common;

import java.math.BigInteger;

public class Common {

    public static final String ContractLogFileName = "deploylog.txt";

    // SystemConfig key
    public static final String TxCountLimit = "tx_count_limit";
    public static final String TxGasLimit = "tx_gas_limit";
    public static final String ConsensusLeaderPeriod = "consensus_leader_period";
    public static final String compatibilityVersion = "compatibility_version";

    public static final int InvalidReturnNumber = -100;
    public static final long InvalidLongValue = Long.MAX_VALUE;

    public static final int QueryLogCount = 20;
    public static final int LogMaxCount = 10000;
    public static final String PositiveIntegerRange = "from 1 to 2147483647";
    public static final String NonNegativeIntegerRange = "from 0 to 2147483647";
    public static final String DeployLogIntegerRange = "from 1 to 100";
    public static final String TxGasLimitRange = "must be greater than 100000";
    public static final String TxCountLimitRange = "must be no smaller than 1";
    public static final String ConsensusLeaderPeriodRange = "must be no smaller than 1";
    public static final String EMPTY_CONTRACT_ADDRESS =
            "0x0000000000000000000000000000000000000000";
    public static final String EMPTY_OUTPUT = "0x";
    public static final int TxGasLimitMin = 100000;

    public static int SYS_TABLE_KEY_MAX_LENGTH = 48;

    // BFS common
    public static final String BFS_TYPE_DIR = "directory";
    public static final String BFS_TYPE_CON = "contract";
    public static final String BFS_TYPE_LNK = "link";
    public static final BigInteger LS_DEFAULT_COUNT = BigInteger.valueOf(500);
}
