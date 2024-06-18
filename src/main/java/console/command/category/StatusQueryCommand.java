package console.command.category;

import console.command.model.BasicCategoryCommand;
import console.command.model.CommandInfo;
import console.command.model.CommandType;
import console.command.model.HelpInfo;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatusQueryCommand extends BasicCategoryCommand {
    protected static final Map<String, CommandInfo> commandToCommandInfo = new HashMap<>();

    public StatusQueryCommand() {
        super(CommandType.STATUS_QUERY);
    }

    @Override
    public CommandInfo getCommandInfo(String command) {
        if (commandToCommandInfo.containsKey(command)) {
            return commandToCommandInfo.get(command);
        }
        return null;
    }

    @Override
    public List<String> getAllCommand(boolean isWasm, boolean isAuthOpen) {
        return commandToCommandInfo
                .keySet()
                .stream()
                .filter(
                        key ->
                                !(isWasm && !commandToCommandInfo.get(key).isWasmSupport()
                                        || (!isAuthOpen
                                                && commandToCommandInfo.get(key).isNeedAuthOpen())))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, CommandInfo> getAllCommandInfo(boolean isWasm) {
        return commandToCommandInfo;
    }

    public static final CommandInfo GET_SYNC_STATUS =
            new CommandInfo(
                    "getSyncStatus",
                    "Query sync status",
                    HelpInfo::getSyncStatusHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getSyncStatus(params));

    public static final CommandInfo GET_PEERS =
            new CommandInfo(
                    "getPeers",
                    "Query peers currently connected to the client",
                    HelpInfo::getPeersHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getPeers(params));

    public static final CommandInfo GET_BLOCK_NUMBER =
            new CommandInfo(
                    "getBlockNumber",
                    "Query the number of most recent block",
                    HelpInfo::getBlockNumberHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getBlockNumber(params));

    public static final CommandInfo GET_LATEST_BLOCK =
            new CommandInfo(
                    "getLatestBlock",
                    "Query the latest block",
                    HelpInfo::getLatestBlockHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getLatestBlock(params),
                    0,
                    1);

    public static final CommandInfo GET_BLOCK_HASH_BY_NUMBER =
            new CommandInfo(
                    "getBlockHashByNumber",
                    "Query block hash by block number.",
                    HelpInfo::getBlockHashByNumberHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getBlockHashByNumber(params),
                    1,
                    1);

    public static final CommandInfo GET_BLOCK_BY_NUMBER =
            new CommandInfo(
                    "getBlockByNumber",
                    "Query information about a block by number",
                    HelpInfo::getBlockByNumberHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getBlockByNumber(params),
                    1,
                    2);

    public static final CommandInfo GET_BLOCK_BY_HASH =
            new CommandInfo(
                    "getBlockByHash",
                    "Query information about a block by hash",
                    HelpInfo::getBlockByHashHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getBlockByHash(params),
                    1,
                    2);

    public static final CommandInfo GET_BLOCK_HEADER_BY_NUMBER =
            new CommandInfo(
                    "getBlockHeaderByNumber",
                    "Query information about a block header by block number",
                    HelpInfo::getBlockHeaderByNumberHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getConsoleClientFace()
                                    .getBlockHeaderByNumber(params),
                    1,
                    1);

    public static final CommandInfo GET_BLOCK_HEADER_BY_HASH =
            new CommandInfo(
                    "getBlockHeaderByHash",
                    "Query information about a block header by hash",
                    HelpInfo::getBlockHeaderByHashHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getBlockHeaderByHash(params),
                    1,
                    1);

    public static final CommandInfo GET_TRANSACTION_BY_HASH =
            new CommandInfo(
                    "getTransactionByHash",
                    "Query information about a transaction requested by transaction hash",
                    HelpInfo::getTransactionByHashHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getTransactionByHash(params),
                    1,
                    2);

    public static final CommandInfo GET_TRANSACTION_BY_HASH_WITH_PROOF =
            new CommandInfo(
                    "getTransactionByHashWithProof",
                    "Query the transaction and transaction proof by transaction hash",
                    HelpInfo::getTransactionByHashWithProofHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getConsoleClientFace()
                                    .getTransactionByHashWithProof(params),
                    1,
                    2);

    public static final CommandInfo GET_TRANSACTION_RECEIPT =
            new CommandInfo(
                    "getTransactionReceipt",
                    "Query the receipt of a transaction by transaction hash",
                    HelpInfo::getTransactionReceiptHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getTransactionReceipt(params),
                    1,
                    2);

    public static final CommandInfo GET_TRANSACTION_RECEIPT_BY_HASH_WITH_PROOF =
            new CommandInfo(
                    "getTransactionReceiptByHashWithProof",
                    "Query the receipt and transaction receipt proof by transaction hash",
                    HelpInfo::getTransactionReceiptByHashWithProofHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getConsoleClientFace()
                                    .getTransactionReceiptByHashWithProof(params),
                    1,
                    2);

    public static final CommandInfo GET_PENDING_TX_SIZE =
            new CommandInfo(
                    "getPendingTxSize",
                    "Query pending transactions size",
                    HelpInfo::getPendingTxSizeHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getPendingTxSize(params));

    public static final CommandInfo GET_TOTAL_TRANSACTION_COUNT =
            new CommandInfo(
                    "getTotalTransactionCount",
                    "Query total transaction count",
                    HelpInfo::getTotalTransactionCountHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getConsoleClientFace()
                                    .getTotalTransactionCount(params));

    public static final CommandInfo SET_SYSTEM_CONFIG_BY_KEY =
            new CommandInfo(
                    "setSystemConfigByKey",
                    "Set a system config value by key",
                    HelpInfo::setSystemConfigByKeyHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getPrecompiledFace()
                                    .setSystemConfigByKey(consoleInitializer, params),
                    2,
                    3);

    public static final CommandInfo GET_SYSTEM_CONFIG_BY_KEY =
            new CommandInfo(
                    "getSystemConfigByKey",
                    "Query a system config value by key",
                    HelpInfo::getSystemConfigByKeyHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getSystemConfigByKey(params),
                    1,
                    1);

    public static final CommandInfo LIST_CONFIGS =
            new CommandInfo(
                    "listSystemConfigs",
                    "List all support system configs",
                    HelpInfo::listSystemConfigsHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().listConfigs(params),
                    0,
                    0);

    static {
        Field[] fields = StatusQueryCommand.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(CommandInfo.class)) {
                try {
                    CommandInfo constantCommandInfo = (CommandInfo) field.get(null);
                    commandToCommandInfo.put(constantCommandInfo.getCommand(), constantCommandInfo);
                    if (constantCommandInfo.getOptionCommand() != null) {
                        List<String> subCommandList = constantCommandInfo.getOptionCommand();
                        for (String s : subCommandList) {
                            commandToCommandInfo.put(s, constantCommandInfo);
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
