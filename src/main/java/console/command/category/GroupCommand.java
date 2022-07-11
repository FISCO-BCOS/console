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

public class GroupCommand extends BasicCategoryCommand {
    protected static final Map<String, CommandInfo> commandToCommandInfo = new HashMap<>();

    public GroupCommand() {
        super(CommandType.GROUP_QUERY);
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

    public static final CommandInfo GET_GROUP_PEERS =
            new CommandInfo(
                    "getGroupPeers",
                    "List all group peers",
                    HelpInfo::getGroupPeersHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getGroupPeers(params),
                    0,
                    0);

    public static final CommandInfo GET_GROUP_NODE_INFO =
            new CommandInfo(
                    "getGroupNodeInfo",
                    "Get group node info",
                    () -> {
                        System.out.println("Get group node info");
                        System.out.println("Usage: \ngetGroupNodeInfo [nodeName]");
                    },
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getGroupNodeInfo(params),
                    1,
                    1);

    public static final CommandInfo GET_GROUP_INFO =
            new CommandInfo(
                    "getGroupInfo",
                    "Query the current group information.",
                    HelpInfo::getGroupInfoHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getGroupInfo(params),
                    0,
                    0);

    public static final CommandInfo GET_GROUP_LIST =
            new CommandInfo(
                    "getGroupList",
                    "List all group list",
                    HelpInfo::getGroupListHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getGroupList(params),
                    0,
                    0);

    public static final CommandInfo GET_GROUP_INFO_LIST =
            new CommandInfo(
                    "getGroupInfoList",
                    "Get all groups info",
                    () -> {
                        System.out.println("Get all group info");
                        System.out.println("Usage: \ngetGroupInfoList");
                    },
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getGroupInfoList(params),
                    0,
                    0);

    static {
        Field[] fields = GroupCommand.class.getDeclaredFields();
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
