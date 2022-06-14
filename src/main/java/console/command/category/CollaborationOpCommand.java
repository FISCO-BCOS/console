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

public class CollaborationOpCommand extends BasicCategoryCommand {
    protected static final Map<String, CommandInfo> commandToCommandInfo = new HashMap<>();

    public CollaborationOpCommand() {
        super(CommandType.COLLABORATE_OP);
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
        if (isWasm) return new ArrayList<>(commandToCommandInfo.keySet());
        return new ArrayList<>();
    }

    @Override
    public Map<String, CommandInfo> getAllCommandInfo(boolean isWasm) {
        return commandToCommandInfo;
    }

    public static final CommandInfo INITIALIZE =
            new CommandInfo(
                    "initialize",
                    "Initialize a collaboration",
                    HelpInfo::initializeHelp,
                    (consoleInitializer, params, pwd) -> {
                        consoleInitializer.getCollaborationFace().initialize(params);
                    });

    public static final CommandInfo SIGN =
            new CommandInfo(
                    "sign",
                    "Sign a contract",
                    HelpInfo::signHelp,
                    (consoleInitializer, params, pwd) -> {
                        consoleInitializer.getCollaborationFace().sign(params);
                    });

    public static final CommandInfo EXERCISE =
            new CommandInfo(
                    "execute",
                    "Exercise an right of a contract",
                    HelpInfo::exerciseHelp,
                    (consoleInitializer, params, pwd) -> {
                        consoleInitializer.getCollaborationFace().exercise(params);
                    });

    public static final CommandInfo FETCH =
            new CommandInfo(
                    "fetch",
                    "Fetch a contract",
                    HelpInfo::fetchHelp,
                    (consoleInitializer, params, pwd) -> {
                        consoleInitializer.getCollaborationFace().fetch(params);
                    });

    static {
        Field[] fields = CollaborationOpCommand.class.getDeclaredFields();
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

    // TODO: Liquid collaboration service is not supported in FISCO BCOS 3.0.0 rc4
    public static final List<String> COLLABORATION_COMMANDS =
            new ArrayList<>(
                    Arrays.asList(
                            INITIALIZE.getCommand(),
                            SIGN.getCommand(),
                            EXERCISE.getCommand(),
                            FETCH.getCommand()));
}
