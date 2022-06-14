package console.command.category;

import console.command.model.BasicCategoryCommand;
import console.command.model.CommandInfo;
import console.command.model.CommandType;
import console.command.model.HelpInfo;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CrudCommand extends BasicCategoryCommand {
    protected static final Map<String, CommandInfo> commandToCommandInfo = new HashMap<>();

    public CrudCommand() {
        super(CommandType.CRUD_OP);
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

    public static final CommandInfo DESC =
            new CommandInfo(
                    "desc",
                    "Description table information",
                    HelpInfo::showDescHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().desc(params),
                    1,
                    1);
    public static final CommandInfo CREATE =
            new CommandInfo(
                    "create",
                    "Create table by sql",
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().createTable(params[0]));

    public static final CommandInfo ALTER =
            new CommandInfo(
                    "alter",
                    "Alter table columns by sql",
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().alterTable(params[0]));

    public static final CommandInfo SELECT =
            new CommandInfo(
                    "select",
                    "Select records by sql",
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().select(params[0]));
    public static final CommandInfo INSERT =
            new CommandInfo(
                    "insert",
                    "Insert records by sql",
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().insert(params[0]));
    public static final CommandInfo UPDATE =
            new CommandInfo(
                    "update",
                    "Update records by sql",
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().update(params[0]));
    public static final CommandInfo DELETE =
            new CommandInfo(
                    "delete",
                    "Remove records by sql",
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().remove(params[0]));

    static {
        Field[] fields = CrudCommand.class.getDeclaredFields();
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

    public static final List<String> CRUD_COMMANDS =
            new ArrayList<>(
                    Arrays.asList(
                            CrudCommand.CREATE.getCommand(),
                            CrudCommand.ALTER.getCommand(),
                            CrudCommand.INSERT.getCommand(),
                            CrudCommand.SELECT.getCommand(),
                            CrudCommand.UPDATE.getCommand(),
                            CrudCommand.DELETE.getCommand()));
}
