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

public class ContractOpCommand extends BasicCategoryCommand {
    protected static final Map<String, CommandInfo> commandToCommandInfo = new HashMap<>();
    public static boolean isWasm = false;

    public ContractOpCommand() {
        super(CommandType.CONTRACT_OP);
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

    public static void setIsWasm(boolean wasm) {
        isWasm = wasm;
        DEPLOY.setMinParamLength(wasm ? 2 : 1);
    }

    public static final CommandInfo DEPLOY =
            new CommandInfo(
                    "deploy",
                    "Deploy a contract on blockchain",
                    () -> HelpInfo.deployHelp(isWasm),
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleContractFace().deploy(params, pwd),
                    1,
                    -1);

    public static final CommandInfo CALL =
            new CommandInfo(
                    "call",
                    "Call a contract by a function and parameters",
                    () -> HelpInfo.callHelp(isWasm),
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleContractFace().call(params, pwd),
                    2,
                    -1);

    public static final CommandInfo GET_CODE =
            new CommandInfo(
                    "getCode",
                    "Query code at a given address",
                    HelpInfo::getCodeHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getConsoleClientFace()
                                    .getCode(params, consoleInitializer.getClient().isWASM(), pwd),
                    1,
                    1);

    public static final CommandInfo LIST_ABI =
            new CommandInfo(
                    "listAbi",
                    "List functions and events info of the contract.",
                    () -> HelpInfo.listAbiHelp(isWasm),
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getConsoleContractFace()
                                    .listAbi(consoleInitializer, params, pwd),
                    1,
                    1,
                    true);

    public static final CommandInfo GET_DEPLOY_LOG =
            new CommandInfo(
                    "getDeployLog",
                    "Query the log of deployed contracts",
                    HelpInfo::getDeployLogHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleContractFace().getDeployLog(params),
                    -1,
                    -1,
                    true);

    public static final CommandInfo LIST_DEPLOY_CONTRACT_ADDRESS =
            new CommandInfo(
                    "listDeployContractAddress",
                    "List the contractAddress for the specified contract",
                    HelpInfo::listDeployContractAddressHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getConsoleContractFace()
                                    .listDeployContractAddress(consoleInitializer, params),
                    1,
                    2,
                    true);

    public static final CommandInfo TRANSFER =
            new CommandInfo(
                    "transfer",
                    "Transfer token to a specified address",
                    HelpInfo::transferHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getConsoleContractFace()
                                    .transfer(consoleInitializer, params),
                    2,
                    3,
                    true);

    static {
        Field[] fields = ContractOpCommand.class.getDeclaredFields();
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
