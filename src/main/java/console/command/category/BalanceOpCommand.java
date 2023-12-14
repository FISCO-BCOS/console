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

public class BalanceOpCommand extends BasicCategoryCommand {
    public static final CommandInfo GET_BALANCE =
            new CommandInfo(
                    "getBalance",
                    "Get balance of the account",
                    HelpInfo::getBalanceHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().getBalance(params),
                    1,
                    1);
    public static final CommandInfo ADD_BALANCE =
            new CommandInfo(
                    "addBalance",
                    "Add balance to the account",
                    HelpInfo::addBalanceHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().addBalance(params),
                    2,
                    2);
    public static final CommandInfo SUB_BALANCE =
            new CommandInfo(
                    "subBalance",
                    "Sub balance from the account",
                    HelpInfo::subBalanceHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().subBalance(params),
                    2,
                    2);
    public static final CommandInfo REGISTER_CALLER =
            new CommandInfo(
                    "registerCaller",
                    "Register caller to the account",
                    HelpInfo::registerCallerHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().registerCaller(params),
                    1,
                    1);
    public static final CommandInfo UNREGISTER_CALLER =
            new CommandInfo(
                    "unregisterCaller",
                    "Unregister caller from the account",
                    HelpInfo::unregisterCallerHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().unregisterCaller(params),
                    1,
                    1);
    protected static final Map<String, CommandInfo> commandToCommandInfo = new HashMap<>();

    static {
        Field[] fields = BalanceOpCommand.class.getDeclaredFields();
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

    public BalanceOpCommand() {
        super(CommandType.BALANCE_PRECOMPILED_OP);
    }

    public CommandInfo getCommandInfo(String command) {
        if (commandToCommandInfo.containsKey(command)) {
            return commandToCommandInfo.get(command);
        }
        return null;
    }

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

    public Map<String, CommandInfo> getAllCommandInfo(boolean isWasm) {
        return commandToCommandInfo;
    }
}
