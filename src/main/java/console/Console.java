package console;

import console.client.ConsoleClientFace;
import console.common.CommandInfo;
import console.common.ConsoleUtils;
import console.common.JlineUtils;
import console.common.SupportedCommand;
import console.common.WelcomeInfo;
import console.contract.ConsoleContractFace;
import console.precompiled.PrecompiledFace;
import console.precompiled.permission.PermissionFace;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.exceptions.ClientException;
import org.fisco.bcos.sdk.contract.exceptions.ContractException;
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

    private static Client client;
    private static BcosSDK bcosSDK;
    private static PrecompiledFace precompiledFace;
    private static PermissionFace permissionFace;
    private static ConsoleClientFace consoleClientFace;
    private static ConsoleContractFace consoleContractFace;

    public static int INPUT_FLAG = 0;

    @SuppressWarnings("resource")
    public static void main(String[] args) {

        LineReader lineReader = null;
        Scanner sc = null;
        ConsoleInitializer consoleInitializer = null;
        try {
            consoleInitializer = new ConsoleInitializer();
            consoleInitializer.init(args);
            client = consoleInitializer.getClient();
            bcosSDK = consoleInitializer.getBcosSDK();
            precompiledFace = consoleInitializer.getPrecompiledFace();
            permissionFace = consoleInitializer.getPermissionFace();
            consoleClientFace = consoleInitializer.getConsoleClientFace();
            consoleContractFace = consoleInitializer.getConsoleContractFace();
            lineReader = JlineUtils.getLineReader();
            sc = new Scanner(System.in);
            KeyMap<Binding> keymap = lineReader.getKeyMaps().get(LineReader.MAIN);
            keymap.bind(new Reference("beginning-of-line"), "\033[1~");
            keymap.bind(new Reference("end-of-line"), "\033[4~");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e: {}", e.getMessage(), e);
            return;
        }

        WelcomeInfo.welcome();

        while (true) {
            try {
                if (lineReader == null) {
                    System.out.println("Console can not read commands.");
                    break;
                }
                String request = "";
                if (INPUT_FLAG == 0) {
                    request =
                            lineReader.readLine(
                                    "[group:" + consoleInitializer.getGroupID() + "]> ");
                } else {
                    System.out.print("[group:" + consoleInitializer.getGroupID() + "]> ");
                    sc = new Scanner(System.in);
                    request = sc.nextLine();
                }
                String[] params = null;
                params = ConsoleUtils.tokenizeCommand(request);
                if (params.length < 1) {
                    System.out.print("");
                    continue;
                }
                if ("".equals(params[0].trim())) {
                    System.out.print("");
                    continue;
                }
                // execute the command
                CommandInfo commandInfo = SupportedCommand.getCommandInfo(params[0]);
                if (commandInfo != null) {
                    if (SupportedCommand.CRUD_COMMANDS.contains(params[0])) {
                        String[] inputParamString = new String[1];
                        inputParamString[0] = request;
                        commandInfo.callCommand(consoleInitializer, inputParamString);
                    } else {
                        commandInfo.callCommand(consoleInitializer, params);
                    }
                    // should exit
                    if (SupportedCommand.QUITE.commandEqual(params[0])) {
                        break;
                    }
                } else {
                    System.out.println("Undefined command: \"" + params[0] + "\". Try \"help\".\n");
                }
            } catch (ClientException e) {
                ConsoleUtils.printJson(
                        "{\"code\":"
                                + e.getErrorCode()
                                + ", \"msg\":"
                                + "\""
                                + e.getMessage()
                                + "\"}");
                System.out.println();
                logger.error(" message: {}, e: {}", e.getMessage(), e);
            } catch (ContractException e) {
                ConsoleUtils.printJson(
                        "{\"code\":"
                                + e.getErrorCode()
                                + ", \"msg\":"
                                + "\""
                                + e.getMessage()
                                + "\"}");
                System.out.println();
                logger.error(" message: {}, e: {}", e.getMessage(), e);
            } catch (ClassNotFoundException e) {
                System.out.println(e.getMessage() + " does not exist.");
                System.out.println();
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
                    logger.error(" message: {}, e: {}", e.getMessage(), e);
                }
                System.out.println();
            } catch (InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                System.out.println(targetException.getMessage());
                System.out.println();
            } catch (UserInterruptException e) {
                consoleInitializer.stop();
                break;
            } catch (EndOfFileException e) {
                consoleInitializer.stop();
                break;
            } catch (RuntimeException e) {

                System.out.println(e.getMessage());
                System.out.println();
                logger.error(" message: {}, e: {}", e.getMessage(), e);

            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println();
                logger.error(" message: {}, e: {}", e.getMessage(), e);
            }
        }
    }

    public static void setClient(Client client) {
        Console.client = client;
    }

    public static void setPrecompiledFace(PrecompiledFace precompiledFace) {
        Console.precompiledFace = precompiledFace;
    }

    public static void setConsoleContractFace(ConsoleContractFace consoleContractFace) {
        Console.consoleContractFace = consoleContractFace;
    }
}
