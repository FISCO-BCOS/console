package console.command.category;

import static console.command.category.StatusQueryCommand.SET_SYSTEM_CONFIG_BY_KEY;

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

public class ConsensusOpCommand extends BasicCategoryCommand {
    protected static final Map<String, CommandInfo> commandToCommandInfo = new HashMap<>();

    public ConsensusOpCommand() {
        super(CommandType.CONSENSUS_OP);
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
                .filter(
                        key ->
                                !isAuthOpen
                                        || !CONSENSUS_COMMANDS.contains(key)
                                        || !key.equals(SET_SYSTEM_CONFIG_BY_KEY.getCommand()))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, CommandInfo> getAllCommandInfo(boolean isWasm) {
        return commandToCommandInfo;
    }

    public static final CommandInfo GET_SEALER_LIST =
            new CommandInfo(
                    "getSealerList",
                    "Query nodeId list for sealer nodes",
                    HelpInfo::getSealerListHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getSealerList(params));

    public static final CommandInfo GET_OBSERVER_LIST =
            new CommandInfo(
                    "getObserverList",
                    "Query nodeId list for observer nodes.",
                    HelpInfo::getObserverListHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getObserverList(params));

    public static final CommandInfo GET_CANDIDATE_LIST =
            new CommandInfo(
                    "getCandidateList",
                    "Query nodeId list for candidate sealer nodes.",
                    HelpInfo::getCandidateListHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getConsoleClientFace()
                                    .getCandidateSealerList(params));
    public static final CommandInfo GET_PBFT_VIEW =
            new CommandInfo(
                    "getPbftView",
                    "Query the pbft view of node",
                    HelpInfo::getPbftViewHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getPbftView(params));

    public static final CommandInfo GET_CONSENSUS_STATUS =
            new CommandInfo(
                    "getConsensusStatus",
                    "Query consensus status",
                    HelpInfo::getConsensusStatusHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getConsoleClientFace().getConsensusStatus(params));

    public static final CommandInfo ADD_SEALER =
            new CommandInfo(
                    "addSealer",
                    "Add a sealer node",
                    HelpInfo::addSealerHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().addSealer(params),
                    2,
                    2);

    public static final CommandInfo ADD_OBSERVER =
            new CommandInfo(
                    "addObserver",
                    "Add an observer node",
                    HelpInfo::addObserverHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().addObserver(params),
                    1,
                    1);

    public static final CommandInfo REMOVE_NODE =
            new CommandInfo(
                    "removeNode",
                    "Remove a node",
                    HelpInfo::removeNodeHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().removeNode(params),
                    1,
                    1);

    public static final CommandInfo SET_CONSENSUS_WEIGHT =
            new CommandInfo(
                    "setConsensusWeight",
                    "Set consensus weight for the specified node",
                    HelpInfo::setConsensusWeightHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getPrecompiledFace().setConsensusNodeWeight(params),
                    2,
                    2);

    static {
        Field[] fields = ConsensusOpCommand.class.getDeclaredFields();
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

    public static final List<String> CONSENSUS_COMMANDS =
            new ArrayList<>(
                    Arrays.asList(
                            ConsensusOpCommand.ADD_OBSERVER.getCommand(),
                            ConsensusOpCommand.ADD_SEALER.getCommand(),
                            ConsensusOpCommand.REMOVE_NODE.getCommand(),
                            ConsensusOpCommand.SET_CONSENSUS_WEIGHT.getCommand()));
}
