package console.command.category;

import console.command.model.BasicCategoryCommand;
import console.command.model.CommandInfo;
import console.command.model.CommandType;
import console.command.model.HelpInfo;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BasicCommand extends BasicCategoryCommand {
    protected static final Map<String, CommandInfo> commandToCommandInfo = new HashMap<>();

    public BasicCommand() {
        super(CommandType.BASIC_CMD);
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

    public static final CommandInfo SWITCH =
            new CommandInfo(
                    "switch",
                    "Switch to a specific group by name",
                    new ArrayList<>(Collections.singletonList("s")),
                    HelpInfo::switchEndPointHelp,
                    (consoleInitializer, params, pwd) -> consoleInitializer.switchGroup(params),
                    1,
                    1,
                    false);

    public static final CommandInfo QUIT =
            new CommandInfo(
                    "quit",
                    "Quit console",
                    new ArrayList<>(Arrays.asList("quit", "q", "exit")),
                    (consoleInitializer, params, pwd) -> System.exit(0),
                    false);

    public static final CommandInfo SET_NODE_NAME =
            new CommandInfo(
                    "setNodeName",
                    "Set default node name to send request.",
                    HelpInfo::setNodeNameHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getConsoleClientFace()
                                    .setNodeName(consoleInitializer, params),
                    1,
                    1);

    public static final CommandInfo GET_NODE_NAME =
            new CommandInfo(
                    "getNodeName",
                    "Get default node name in this client.",
                    HelpInfo::getNodeNameHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getConsoleClientFace()
                                    .getNodeName(consoleInitializer),
                    0,
                    0);

    public static final CommandInfo CLEAR_NODE_NAME =
            new CommandInfo(
                    "clearNodeName",
                    "Clear default node name to empty.",
                    HelpInfo::clearNodeNameHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getConsoleClientFace()
                                    .clearNodeName(consoleInitializer),
                    0,
                    0);

    static {
        Field[] fields = BasicCommand.class.getDeclaredFields();
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
