package console;

import console.command.JlineUtils;
import console.command.SupportedCommand;
import console.command.category.BasicCommand;
import console.command.category.BfsCommand;
import console.command.category.CrudCommand;
import console.command.model.CommandInfo;
import console.command.model.WelcomeInfo;
import console.common.ConsoleUtils;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;
import org.fisco.bcos.sdk.v3.client.exceptions.ClientException;
import org.fisco.bcos.sdk.v3.crypto.exceptions.SignatureException;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.Reference;
import org.jline.reader.UserInterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Console {

    private static final Logger logger = LoggerFactory.getLogger(Console.class);

    public static LineReader createLineReader(ConsoleInitializer consoleInitializer)
            throws IOException {
        if (consoleInitializer.isDisableAutoCompleter()) {
            return null;
        }
        return JlineUtils.getLineReader(consoleInitializer.getClient());
    }

    @SuppressWarnings("resource")
    public static void main(String[] args) {

        LineReader lineReader = null;
        Scanner sc;
        ConsoleInitializer consoleInitializer = null;
        try {
            consoleInitializer = new ConsoleInitializer();
            consoleInitializer.init(args);
            lineReader = createLineReader(consoleInitializer);
            if (!consoleInitializer.isDisableAutoCompleter() && lineReader != null) {
                KeyMap<Binding> keymap = lineReader.getKeyMaps().get(LineReader.MAIN);
                keymap.bind(new Reference("beginning-of-line"), "\033[1~");
                keymap.bind(new Reference("end-of-line"), "\033[4~");
            }
            consoleInitializer.setLineReader(lineReader);
        } catch (Exception e) {
            System.out.println("Failed to initialize console, msg: " + e.getMessage());
            logger.error(" message: {}", e.getMessage(), e);
            System.exit(-1);
        }

        WelcomeInfo.welcome();
        String pwd = consoleInitializer.getPrecompiledFace().getPwd();
        SupportedCommand.setIsAuthOpen(
                consoleInitializer.getClient().isEnableCommittee()
                        && !consoleInitializer.getClient().isWASM());
        SupportedCommand.setIsWasm(consoleInitializer.getClient().isWASM());

        while (true) {
            try {
                if (consoleInitializer.getLineReader() == null) {
                    System.out.println("Console can not read commands.");
                    break;
                }
                String request;
                if (!consoleInitializer.isDisableAutoCompleter()) {
                    request =
                            consoleInitializer
                                    .getLineReader()
                                    .readLine(
                                            "["
                                                    + consoleInitializer.getGroupID()
                                                    + "]: "
                                                    + ConsoleUtils.prettyPwd(pwd)
                                                    + "> ");
                } else {
                    System.out.print(
                            "[group:"
                                    + consoleInitializer.getGroupID()
                                    + "]: "
                                    + ConsoleUtils.prettyPwd(pwd)
                                    + "> ");
                    sc = new Scanner(System.in);
                    request = sc.nextLine();
                }
                String[] params;
                params = ConsoleUtils.tokenizeCommand(request);
                if (params.length < 1) {
                    continue;
                }
                if (params[0].trim().isEmpty()) {
                    continue;
                }
                // execute the command
                CommandInfo commandInfo =
                        SupportedCommand.getCommandInfo(
                                params[0],
                                consoleInitializer.getClient().isWASM(),
                                consoleInitializer.getClient().isEnableCommittee());
                if (commandInfo != null) {
                    if (CrudCommand.CRUD_COMMANDS.contains(params[0])) {
                        String[] inputParamString = new String[1];
                        inputParamString[0] = request;
                        commandInfo.callCommand(consoleInitializer, inputParamString, null);
                    } else if (BfsCommand.BFS_COMMANDS.contains(params[0])) {
                        commandInfo.callCommand(consoleInitializer, params, pwd);
                        if (commandInfo.getCommand().equals(BfsCommand.CHANGE_DIR.getCommand())) {
                            pwd = consoleInitializer.getPrecompiledFace().getPwd();
                            JlineUtils.switchPwd(pwd);
                        }
                    } else {
                        String[] paramWithoutQuotation = new String[params.length];
                        for (int i = 0; i < params.length; i++) {
                            String param = params[i];
                            paramWithoutQuotation[i] = param;
                            // Remove the quotes around the input parameters
                            if (param.length() >= 3
                                    && param.startsWith("\"")
                                    && param.endsWith("\"")) {
                                paramWithoutQuotation[i] = param.substring(1, param.length() - 1);
                            }
                        }

                        String cmd = commandInfo.getCommand();
                        commandInfo.callCommand(consoleInitializer, paramWithoutQuotation, pwd);

                        if (cmd.equals(BasicCommand.SWITCH.getCommand())) {
                            // update the client when switch group
                            JlineUtils.switchGroup(consoleInitializer.getClient());
                            SupportedCommand.setIsAuthOpen(
                                    consoleInitializer.getClient().isEnableCommittee()
                                            && !consoleInitializer.getClient().isWASM());
                            SupportedCommand.setIsWasm(consoleInitializer.getClient().isWASM());
                        }
                    }
                } else {
                    System.out.println("Undefined command: \"" + params[0] + "\". Try \"help\".\n");
                }
            } catch (ClientException e) {
                String errorMessage = e.getMessage();
                if (e.getErrorMessage() != null) {
                    errorMessage = e.getErrorMessage();
                }
                ConsoleUtils.printJson(
                        "{\"code\":"
                                + e.getErrorCode()
                                + ", \"msg\":"
                                + "\""
                                + errorMessage
                                + "\"}");
                System.out.println();
                logger.error("ClientException, e: ", e);
            } catch (ContractException e) {
                ConsoleUtils.printJson(
                        "{\"code\":"
                                + e.getErrorCode()
                                + ", \"msg\":"
                                + "\""
                                + e.getMessage()
                                + "\"}");
                System.out.println();
                logger.error("ContractException, e: ", e);
            } catch (SignatureException e) {
                System.out.println("\nSignatureException for " + e.getMessage());
                if (consoleInitializer.getClient().getCryptoType() == CryptoType.SM_TYPE) {
                    System.out.println(
                            "Current ledger crypto type is SM, please make sure the account is a sm account!\n");
                } else if (consoleInitializer.getClient().getCryptoType()
                        == CryptoType.ECDSA_TYPE) {
                    System.out.println(
                            "Current ledger crypto type is ECDSA, please make sure the account is a ecdsa account!");
                }
                logger.error("SignatureException, e: ", e);
            } catch (ClassNotFoundException e) {
                System.out.println(e.getMessage() + " does not exist.");
                System.out.println();
                logger.error("ClassNotFoundException, e:", e);
            } catch (IOException e) {
                if (e.getMessage().startsWith("activeConnections")) {
                    System.out.println(
                            "Lost the connection to the node. "
                                    + "Please check the connection between the console and the node.");
                } else if (e.getMessage().startsWith("No value")) {
                    System.out.println(
                            "The groupID is not configured in dist/conf/applicationContext.xml file.");
                } else {
                    System.out.println(e.getMessage());
                    logger.error("IOException, e:", e);
                }
                System.out.println();
                logger.error("IOException, e:", e);
            } catch (InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                System.out.println(targetException.getMessage());
                System.out.println();
                logger.error("InvocationTargetException, e:", e);
            } catch (UserInterruptException e) {
                consoleInitializer.stop();
                break;
            } catch (EndOfFileException e) {
                consoleInitializer.stop();
                logger.error("EndOfFileException, e:", e);
                break;
            } catch (Exception e) {
                System.out.println("Exception, e: " + e);
                System.out.println();
                logger.error("Exception, e:", e);
            }
        }
    }
}
