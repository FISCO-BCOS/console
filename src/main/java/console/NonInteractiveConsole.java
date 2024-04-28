package console;

import console.command.SupportedCommand;
import console.command.category.CrudCommand;
import console.command.model.CommandInfo;
import console.common.ConsoleUtils;
import console.contract.utils.ContractCompiler;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.fisco.bcos.sdk.v3.client.exceptions.ClientException;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.StringUtils;
import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NonInteractiveConsole {
    private static final Logger logger = LoggerFactory.getLogger(NonInteractiveConsole.class);

    public static void main(String[] args) {

        if (args.length == 0 || args[0].equals("-h") || args[0].equals("--help")) {
            SupportedCommand.printNonInteractiveDescInfo();
            System.exit(0);
        }

        ConsoleInitializer consoleInitializer = null;
        String[] params = null;
        try {
            consoleInitializer = new ConsoleInitializer();
            params = new String[args.length];
            System.arraycopy(args, 0, params, 0, params.length);
            consoleInitializer.init(new String[] {});
        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e: ", e.getMessage(), e);
            return;
        }

        SupportedCommand.isWasm = consoleInitializer.getClient().isWASM();
        SupportedCommand.isAuthOpen = consoleInitializer.getClient().isEnableCommittee();
        try {
            String[] command = params[0].split(" ");
            CommandInfo commandInfo = null;
            // execute the command
            if (command.length > 1) {
                commandInfo =
                        SupportedCommand.getCommandInfo(
                                command[0],
                                consoleInitializer.getClient().isWASM(),
                                consoleInitializer.getClient().isEnableCommittee());
            } else {
                commandInfo =
                        SupportedCommand.getCommandInfo(
                                params[0],
                                consoleInitializer.getClient().isWASM(),
                                consoleInitializer.getClient().isEnableCommittee());
            }
            if (commandInfo != null) {
                if (CrudCommand.CRUD_COMMANDS.contains(command[0])) {
                    String sqlCommand = StringUtils.join(Arrays.asList(params), " ");
                    String[] inputParamString = new String[1];
                    inputParamString[0] = sqlCommand;
                    commandInfo.callCommand(consoleInitializer, inputParamString, null);
                } else {
                    String[] paramWithoutQuotation = new String[params.length];
                    for (int i = 0; i < params.length; i++) {
                        String param = params[i];
                        paramWithoutQuotation[i] = param;
                        // Remove the quotes around the input parameters
                        if (param.length() >= 3 && param.startsWith("\"") && param.endsWith("\"")) {
                            paramWithoutQuotation[i] = param.substring(1, param.length() - 1);
                        }
                    }
                    commandInfo.callCommand(
                            consoleInitializer,
                            paramWithoutQuotation,
                            ContractCompiler.BFS_APPS_PREFIX);
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
                    "{\"code\":" + e.getErrorCode() + ", \"msg\":" + "\"" + errorMessage + "\"}");
            System.out.println();
            logger.error(" message: {}, e: ", e.getMessage(), e);
        } catch (ContractException e) {
            ConsoleUtils.printJson(
                    "{\"code\":" + e.getErrorCode() + ", \"msg\":" + "\"" + e.getMessage() + "\"}");
            System.out.println();
            logger.error(" message: {}, e: ", e.getMessage(), e);
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
                logger.error(" message: {}, e: ", e.getMessage(), e);
            }
            System.out.println();
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            System.out.println(targetException.getMessage());
            System.out.println();
        } catch (UserInterruptException e) {
            consoleInitializer.stop();
        } catch (EndOfFileException e) {
            consoleInitializer.stop();
        } catch (RuntimeException e) {

            System.out.println(e.getMessage());
            System.out.println();
            logger.error(" message: {}, e: ", e.getMessage(), e);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println();
            logger.error(" message: {}, e: ", e.getMessage(), e);
        } finally {
            System.exit(0);
        }
    }
}
