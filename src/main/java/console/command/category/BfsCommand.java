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

public class BfsCommand extends BasicCategoryCommand {
    protected static final Map<String, CommandInfo> commandToCommandInfo = new HashMap<>();

    public BfsCommand() {
        super(CommandType.BFS_OP);
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

    public static final CommandInfo CHANGE_DIR =
            new CommandInfo(
                    "cd",
                    "Change dir to given path.",
                    HelpInfo::changeDirHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().changeDir(params),
                    0,
                    1);
    public static final CommandInfo MAKE_DIR =
            new CommandInfo(
                    "mkdir",
                    "Create dir in given path.",
                    HelpInfo::makeDirHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().makeDir(params),
                    1,
                    1);
    public static final CommandInfo LIST_DIR =
            new CommandInfo(
                    "ls",
                    "List resources in given path.",
                    HelpInfo::listDirHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().listDir(params),
                    0,
                    1);

    public static final CommandInfo TREE =
            new CommandInfo(
                    "tree",
                    "List contents of directories in a tree-like format.",
                    HelpInfo::treeHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().tree(params),
                    1,
                    2);

    public static final CommandInfo LINK =
            new CommandInfo(
                    "ln",
                    "Create a link to access contract.",
                    HelpInfo::linkHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().link(params),
                    2,
                    2);

    public static final CommandInfo FIX_BFS =
            new CommandInfo(
                    "fixBFS",
                    "Fix the bfs bug of the specified version.",
                    () -> System.out.println("Fix the bfs bug of the specified version."),
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().fixBFS(params),
                    0,
                    0);

    public static final CommandInfo PWD =
            new CommandInfo(
                    "pwd",
                    "Show absolute path of working directory name",
                    HelpInfo::pwdHelp,
                    (consoleInitializer, params, pwd) ->
                            System.out.println(consoleInitializer.getPrecompiledFace().getPwd()),
                    0,
                    0);

    static {
        Field[] fields = BfsCommand.class.getDeclaredFields();
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

    public static final List<String> BFS_COMMANDS =
            new ArrayList<>(
                    Arrays.asList(
                            BfsCommand.LIST_DIR.getCommand(),
                            BfsCommand.CHANGE_DIR.getCommand(),
                            BfsCommand.MAKE_DIR.getCommand(),
                            BfsCommand.TREE.getCommand(),
                            BfsCommand.LINK.getCommand()));
}
