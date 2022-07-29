package console.command;

import console.command.category.AccountOpCommand;
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
import console.common.Common;
import console.contract.utils.ContractCompiler;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
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

    public static LineReader getLineReader() throws IOException {
        return createLineReader(new ArrayList<Completer>());
    }

    public static void switchGroup(Client client) {
        if (contractAddressCompleter != null) {
            contractAddressCompleter.setClient(client);
        }
        if (contractMethodCompleter != null) {
            contractMethodCompleter.setClient(client);
        }
        if (accountCompleter != null) {
            accountCompleter.setClient(client);
        }
    }

    public static void switchPwd(String pwd) {
        currentPathCompleter.setPwd(pwd);
    }

    public static LineReader getLineReader(Client client) throws IOException {

        List<Completer> completers = new ArrayList<>();

        List<String> commands =
                SupportedCommand.getAllCommand(client.isWASM(), client.isAuthCheck());
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
                                new ConsoleFilesCompleter(new File(ContractCompiler.SOLIDITY_PATH)),
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
                Arrays.asList(
                        StatusQueryCommand.SET_SYSTEM_CONFIG_BY_KEY.getCommand(),
                        StatusQueryCommand.GET_SYSTEM_CONFIG_BY_KEY.getCommand());

        for (String command : commands) {
            completers.add(
                    new ArgumentCompleter(
                            new StringsCompleter(command),
                            new StringsCompleter(Common.TxCountLimit),
                            new StringsCompleterIgnoreCase()));

            completers.add(
                    new ArgumentCompleter(
                            new StringsCompleter(command),
                            new StringsCompleter(Common.TxGasLimit),
                            new StringsCompleterIgnoreCase()));
            completers.add(
                    new ArgumentCompleter(
                            new StringsCompleter(command),
                            new StringsCompleter(Common.ConsensusLeaderPeriod),
                            new StringsCompleterIgnoreCase()));
            completers.add(
                    new ArgumentCompleter(
                            new StringsCompleter(command),
                            new StringsCompleter(Common.compatibilityVersion),
                            new StringsCompleterIgnoreCase()));
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
        return createLineReader(completers);
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
