package console.common;

public class Common {

    public static final String ContractLogFileName = "deploylog.txt";

    // SystemConfig key
    public static final String TxCountLimit = "tx_count_limit";
    public static final String TxGasLimit = "tx_gas_limit";
    public static final String RPBFTEpochSealerNum = "rpbft_epoch_sealer_num";
    public static final String RPBFTEpochBlockNum = "rpbft_epoch_block_num";

    // in seconds
    public static final String ConsensusTimeout = "consensus_timeout";
    public static int ConsensusTimeoutMin = 3;
    public static int ConsensusTimeoutMax = Integer.MAX_VALUE / 1000;

    public static final int InvalidReturnNumber = -100;
    public static final int QueryLogCount = 20;
    public static final int LogMaxCount = 10000;
    public static final String GroupIDRange = "from 1 to 32767";
    public static final String PositiveIntegerRange = "from 1 to 2147483647";
    public static final String NonNegativeIntegerRange = "from 0 to 2147483647";
    public static final String DeployLongIntegerRange = "from 1 to 100";
    public static final String NodeIdLength = "128";
    public static final String TxGasLimitRange = "from 100000 to 2147483647";
    public static final String ConsensusTimeoutRange =
            "from "
                    + Integer.toString(ConsensusTimeoutMin)
                    + " seconds to "
                    + Integer.toString(ConsensusTimeoutMax - 1)
                    + " seconds";
    public static final String EMPTY_CONTRACT_ADDRESS =
            "0x0000000000000000000000000000000000000000";
    public static final String EMPTY_OUTPUT = "0x";
    public static final int TxGasLimitMin = 100000;

    public static int PermissionCode = 0;
    public static int TableExist = 0;
    public static final int MaxGroupID = 32767;
    public static int SYS_TABLE_KEY_MAX_LENGTH = 48;
    public static int SYS_TABLE_KEY_FIELD_NAME_MAX_LENGTH = 64;
    public static int SYS_TABLE_VALUE_FIELD_MAX_LENGTH = 1024;
    public static int USER_TABLE_KEY_VALUE_MAX_LENGTH = 255;
    public static int USER_TABLE_FIELD_NAME_MAX_LENGTH = 64;
    public static int USER_TABLE_FIELD_VALUE_MAX_LENGTH = 16 * 1024 * 1024 - 1;
}
