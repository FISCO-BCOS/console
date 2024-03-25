package console.command;

import console.command.category.AccountOpCommand;
import console.command.category.AuthOpCommand;
import console.command.category.BfsCommand;
import console.command.category.ContractOpCommand;
import console.command.category.StatusQueryCommand;
import console.command.completer.AccountCompleter;
import console.command.completer.AccountFileFormatCompleter;
import console.command.completer.ConsoleFilesCompleter;
import console.command.completer.ContractAddressCompleter;
import console.command.completer.ContractMethodCompleter;
import console.command.completer.CurrentPathCompleter;
import console.command.completer.StringsCompleterIgnoreCase;
import console.contract.utils.ContractCompiler;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosGroupInfo;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosGroupInfoList;
import org.fisco.bcos.sdk.v3.contract.precompiled.sysconfig.SystemConfigFeature;
import org.fisco.bcos.sdk.v3.contract.precompiled.sysconfig.SystemConfigService;
import org.fisco.bcos.sdk.v3.model.EnumNodeVersion;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Attributes;
import org.jline.terminal.Attributes.ControlChar;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JlineUtils {

    private static final Logger logger = LoggerFactory.getLogger(JlineUtils.class);
    private static ContractAddressCompleter contractAddressCompleter = null;
    private static ContractMethodCompleter contractMethodCompleter = null;
    private static AccountCompleter accountCompleter = null;
    private static CurrentPathCompleter currentPathCompleter = null;

    private static LineReader lineReader = null;

    public static LineReader getLineReader() {
        return lineReader;
    }

    public static void switchGroup(Client client) throws IOException {
        if (contractAddressCompleter != null) {
            contractAddressCompleter.setClient(client);
        }
        if (contractMethodCompleter != null) {
            contractMethodCompleter.setClient(client);
        }
        if (accountCompleter != null) {
            accountCompleter.setClient(client);
        }
        if (currentPathCompleter != null) {
            currentPathCompleter.setClient(client);
        }
        List<Completer> completers = generateComplters(client);
        ((LineReaderImpl) lineReader).setCompleter(new AggregateCompleter(completers));
    }

    public static void switchPwd(String pwd) {
        currentPathCompleter.setPwd(pwd);
    }

    public static LineReader getLineReader(Client client) throws IOException {

        List<Completer> completers = generateComplters(client);
        lineReader = createLineReader(completers);
        return lineReader;
    }

    private static List<Completer> generateComplters(Client client) {
        List<Completer> completers = new ArrayList<>();

        List<String> commands =
                SupportedCommand.getAllCommand(client.isWASM(), client.isEnableCommittee());
        contractAddressCompleter = new ContractAddressCompleter(client);
        contractMethodCompleter = new ContractMethodCompleter(client);
        accountCompleter = new AccountCompleter(client);
        currentPathCompleter = new CurrentPathCompleter(client);

        for (String command : commands) {
            completers.add(
                    new ArgumentCompleter(
                            new StringsCompleterIgnoreCase(command),
                            new StringsCompleterIgnoreCase()));
        }
        if (!client.isWASM()) {
            // solidity
            commands =
                    Arrays.asList(
                            ContractOpCommand.DEPLOY.getCommand(),
                            ContractOpCommand.LIST_DEPLOY_CONTRACT_ADDRESS.getCommand(),
                            ContractOpCommand.LIST_ABI.getCommand());

            for (String command : commands) {
                completers.add(
                        new ArgumentCompleter(
                                new StringsCompleter(command),
                                new ConsoleFilesCompleter(
                                        new File(ContractCompiler.SOLIDITY_PATH), false),
                                new StringsCompleterIgnoreCase()));
                completers.add(
                        new ArgumentCompleter(
                                new StringsCompleter(command),
                                new StringsCompleter("-l"),
                                currentPathCompleter,
                                new ConsoleFilesCompleter(
                                        new File(ContractCompiler.SOLIDITY_PATH), false),
                                new StringsCompleterIgnoreCase()));
            }
            // contract address and method completer
            commands = Arrays.asList(ContractOpCommand.CALL.getCommand());
            for (String command : commands) {
                completers.add(
                        new ArgumentCompleter(
                                new StringsCompleter(command),
                                new ConsoleFilesCompleter(new File(ContractCompiler.SOLIDITY_PATH)),
                                contractAddressCompleter,
                                contractMethodCompleter,
                                new StringsCompleterIgnoreCase()));
                completers.add(
                        new ArgumentCompleter(
                                new StringsCompleter(command),
                                new CurrentPathCompleter(client),
                                contractMethodCompleter,
                                new StringsCompleterIgnoreCase()));
            }
            // completer for link
            completers.add(
                    new ArgumentCompleter(
                            new StringsCompleter(BfsCommand.LINK.getCommand()),
                            new CurrentPathCompleter(client),
                            contractAddressCompleter));
        } else {
            // liquid
            commands = Arrays.asList(ContractOpCommand.DEPLOY.getCommand());

            for (String command : commands) {
                completers.add(
                        new ArgumentCompleter(
                                new StringsCompleter(command),
                                new ConsoleFilesCompleter(
                                        new File(ContractCompiler.LIQUID_PATH), true),
                                currentPathCompleter,
                                new StringsCompleterIgnoreCase()));
                completers.add(
                        new ArgumentCompleter(
                                new StringsCompleter(command),
                                new StringsCompleter("-l"),
                                currentPathCompleter,
                                new ConsoleFilesCompleter(
                                        new File(ContractCompiler.LIQUID_PATH), true),
                                currentPathCompleter,
                                new StringsCompleterIgnoreCase()));
            }
            // contract address and method completer
            commands = Arrays.asList(ContractOpCommand.CALL.getCommand());
            for (String command : commands) {
                completers.add(
                        new ArgumentCompleter(
                                new StringsCompleter(command),
                                currentPathCompleter,
                                contractMethodCompleter,
                                new StringsCompleterIgnoreCase()));
            }
        }

        commands =
                new ArrayList<>(
                        Arrays.asList(
                                StatusQueryCommand.SET_SYSTEM_CONFIG_BY_KEY.getCommand(),
                                StatusQueryCommand.GET_SYSTEM_CONFIG_BY_KEY.getCommand()));
        if (client.isEnableCommittee()) {
            commands.add(AuthOpCommand.SET_SYS_CONFIG_PROPOSAL.getCommand());
        }
        Set<String> keys = new HashSet<>();
        keys.add(SystemConfigService.TX_COUNT_LIMIT);
        keys.add(SystemConfigService.TX_GAS_LIMIT);
        keys.add(SystemConfigService.TX_GAS_PRICE);
        keys.add(SystemConfigService.CONSENSUS_PERIOD);
        keys.add(SystemConfigService.COMPATIBILITY_VERSION);
        keys.add(SystemConfigService.AUTH_STATUS);
        for (SystemConfigFeature.Features feature : SystemConfigFeature.Features.values()) {
            if (client.getChainCompatibilityVersion()
                            .compareTo(EnumNodeVersion.convertToVersion(feature.enableVersion()))
                    >= 0) {
                keys.add(feature.toString());
            }
        }
        BcosGroupInfoList groupInfoList;
        try {
            groupInfoList = client.getGroupInfoList();
            Optional<BcosGroupInfo.GroupInfo> group =
                    groupInfoList
                            .getResult()
                            .stream()
                            .filter(groupInfo -> groupInfo.getGroupID().equals(client.getGroup()))
                            .findFirst();
            if (group.isPresent() && !group.get().getNodeList().isEmpty()) {
                group.get()
                        .getNodeList()
                        .forEach(groupNodeInfo -> keys.addAll(groupNodeInfo.getFeatureKeys()));
            }
        } catch (Exception ignored) {
            logger.info("Failed to get group info list, skip feature keys.");
        }

        for (String command : commands) {
            for (String key : keys) {
                completers.add(
                        new ArgumentCompleter(
                                new StringsCompleter(command),
                                new StringsCompleter(key),
                                new StringsCompleterIgnoreCase()));
            }
        }
        completers.add(
                new ArgumentCompleter(
                        new StringsCompleter(AccountOpCommand.LOAD_ACCOUNT.getCommand()),
                        new AccountCompleter(client),
                        new AccountFileFormatCompleter()));

        completers.add(
                new ArgumentCompleter(
                        new StringsCompleterIgnoreCase(AccountOpCommand.NEW_ACCOUNT.getCommand()),
                        new AccountFileFormatCompleter()));

        commands = BfsCommand.BFS_COMMANDS;

        for (String command : commands) {
            completers.add(
                    new ArgumentCompleter(
                            new StringsCompleter(command),
                            currentPathCompleter,
                            new StringsCompleterIgnoreCase()));
        }
        return completers;
    }

    public static LineReader createLineReader(List<Completer> completers) throws IOException {
        Terminal terminal =
                TerminalBuilder.builder()
                        .nativeSignals(true)
                        .signalHandler(Terminal.SignalHandler.SIG_IGN)
                        .build();
        Attributes termAttribs = terminal.getAttributes();
        // enable CTRL+D shortcut to exit
        // disable CTRL+C shortcut
        termAttribs.setControlChar(ControlChar.VEOF, 4);
        termAttribs.setControlChar(ControlChar.VINTR, -1);
        terminal.setAttributes(termAttribs);
        return LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(new AggregateCompleter(completers))
                .build()
                .option(LineReader.Option.HISTORY_IGNORE_SPACE, false)
                .option(LineReader.Option.HISTORY_REDUCE_BLANKS, false);
    }
}
