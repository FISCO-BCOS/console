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

public class AccountOpCommand extends BasicCategoryCommand {
    protected static final Map<String, CommandInfo> commandToCommandInfo = new HashMap<>();

    public AccountOpCommand() {
        super(CommandType.ACCOUNT_OP);
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

    public static final CommandInfo NEW_ACCOUNT =
            new CommandInfo(
                    "newAccount",
                    "Create account",
                    HelpInfo::newAccountHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().newAccount(params),
                    0,
                    2);

    public static final CommandInfo LOAD_ACCOUNT =
            new CommandInfo(
                    "loadAccount",
                    "Load account for the transaction signature",
                    HelpInfo::loadAccountHelp,
                    (consoleInitializer, params, pwd) -> consoleInitializer.loadAccount(params),
                    1,
                    2,
                    false);

    public static final CommandInfo LIST_ACCOUNT =
            new CommandInfo(
                    "listAccount",
                    "List the current saved account list",
                    () -> {
                        System.out.println("List all the accounts");
                        System.out.println("Usage: \nlistAccount");
                    },
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().listAccount(params),
                    0,
                    0);

    public static final CommandInfo GET_CURRENT_ACCOUNT =
            new CommandInfo(
                    "getCurrentAccount",
                    "Get the current account info",
                    (consoleInitializer, params, pwd) ->
                            System.out.println(
                                    consoleInitializer
                                            .getClient()
                                            .getCryptoSuite()
                                            .getCryptoKeyPair()
                                            .getAddress()));

    static {
        Field[] fields = AccountOpCommand.class.getDeclaredFields();
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
