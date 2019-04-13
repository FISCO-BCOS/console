package console;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.channel.ResponseExcepiton;
import org.fisco.bcos.web3j.protocol.core.Response;
import org.fisco.bcos.web3j.protocol.exceptions.MessageDecodingException;
import org.jline.builtins.Completers.FilesCompleter;
import org.jline.reader.Buffer;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Attributes;
import org.jline.terminal.Attributes.ControlChar;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;

import console.common.Common;
import console.common.ConsoleUtils;
import console.common.HelpInfo;

class StringsCompleterIgnoreCase implements Completer {
  protected final Collection<Candidate> candidates = new ArrayList<>();

  public StringsCompleterIgnoreCase() {
  }

  public StringsCompleterIgnoreCase(String... strings) {
    this(Arrays.asList(strings));
  }

  public StringsCompleterIgnoreCase(Iterable<String> strings) {
    assert strings != null;
    for (String string : strings) {
      candidates.add(new Candidate(AttributedString.stripAnsi(string), string, null, null, null, null, true));
    }
  }

  public void complete(LineReader reader, final ParsedLine commandLine, final List<Candidate> candidates) {
    assert commandLine != null;
    assert candidates != null;

    Buffer buffer = reader.getBuffer();
    String start = (buffer == null) ? "" : buffer.toString();
    int index = start.lastIndexOf(" ");
    String tmp = start.substring(index + 1, start.length()).toLowerCase();

    for (Iterator<Candidate> iter = this.candidates.iterator(); iter.hasNext();) {
      Candidate candidate = iter.next();
      String candidateStr = candidate.value().toLowerCase();
      if (candidateStr.startsWith(tmp)) {
        candidates.add(candidate);
      }
    }
  }
}

public class ConsoleClient {
  @SuppressWarnings("resource")
  public static void main(String[] args) {
    ConsoleFace console = null;
    LineReader lineReader = null;
    try {

      console = new ConsoleImpl();
      console.init(args);
      console.welcome();

      List<Completer> completers = new ArrayList<Completer>();

      List<String> commands = Arrays.asList("help", "switch", "getBlockNumber", "getPbftView", "getSealerList",
          "getObserverList", "getConsensusStatus", "getSyncStatus", "getNodeVersion", "getPeers", "getNodeIDList",
          "getGroupPeers", "getGroupList", "getBlockByHash", "getBlockByNumber", "getBlockHashByNumber",
          "getTransactionByHash", "getTransactionByBlockHashAndIndex", "getTransactionByBlockNumberAndIndex",
          "getTransactionReceipt", "getPendingTransactions", "getPendingTxSize", "getCode", "getTotalTransactionCount",
          "getDeployLog", "addSealer", "addObserver", "removeNode", "grantUserTableManager", "revokeUserTableManager",
          "listUserTableManager", "grantDeployAndCreateManager", "revokeDeployAndCreateManager",
          "listDeployAndCreateManager", "grantPermissionManager", "revokePermissionManager", "listPermissionManager",
          "grantNodeManager", "revokeNodeManager", "listNodeManager", "grantCNSManager", "revokeCNSManager",
          "listCNSManager", "grantSysConfigManager", "revokeSysConfigManager", "listSysConfigManager", "quit", "exit");

      for (String command : commands) {
        completers.add(new ArgumentCompleter(new StringsCompleterIgnoreCase(command)));
      }

      Path path = FileSystems.getDefault().getPath("solidity/contracts/", "");
      commands = Arrays.asList("deploy", "call", "deployByCNS", "callByCNS", "queryCNS");

      for (String command : commands) {
        completers.add(new ArgumentCompleter(new StringsCompleter(command), new FilesCompleter(path)));
      }

      commands = Arrays.asList("setSystemConfigByKey", "getSystemConfigByKey");

      for (String command : commands) {
        completers.add(new ArgumentCompleter(new StringsCompleterIgnoreCase(command),
            new StringsCompleterIgnoreCase(Common.TxCountLimit)));
        completers.add(new ArgumentCompleter(new StringsCompleterIgnoreCase(command),
            new StringsCompleterIgnoreCase(Common.TxGasLimit)));
      }

    Terminal terminal = TerminalBuilder.builder()
          .nativeSignals(true)
          .signalHandler(Terminal.SignalHandler.SIG_IGN)
          .build();
    Attributes termAttribs = terminal.getAttributes();
    // enable CTRL+D shortcut
    termAttribs.setControlChar(ControlChar.VEOF, 4);
    // enable CTRL+C shortcut
    termAttribs.setControlChar(ControlChar.VINTR, 4);
    terminal.setAttributes(termAttribs);
    lineReader = LineReaderBuilder.builder().terminal(terminal).
      		completer(new AggregateCompleter(completers)).
      		build().option(LineReader.Option.HISTORY_IGNORE_SPACE, false)
      		.option(LineReader.Option.HISTORY_REDUCE_BLANKS, false);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return;
    }

    while (true) {

      try {
        String request = lineReader.readLine("[group:" + ConsoleImpl.groupID + "]> ");
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

        String command = params[0].trim().toLowerCase();

        if ("quit".equals(command) || "q".equals(command) || "exit".equals(command)) {
          if (HelpInfo.promptNoParams(params, "q")) {
            continue;
          } else if (params.length > 2) {
            HelpInfo.promptHelp("q");
            continue;
          }
          console.close();
          break;
        }

        if ("h".equals(command) || "help".equals(command)) {
          console.help(params);
        } else {
          if ("s".equals(command) || "switch".equals(command)) {
            console.switchGroupID(params);
          } else {
            Class<?> consoleClass = console.getClass();
            try {
              Method method = consoleClass.getDeclaredMethod(params[0].trim(), String[].class);
              method.invoke(console, (Object) params);
            } catch (NoSuchMethodException e) {
              System.out.println("Undefined command: \"" + params[0] + "\". Try \"help\".\n");
            }
          }
        }
      } 
      catch (ResponseExcepiton e) {
        ConsoleUtils.printJson("{\"code\":" + e.getCode() + ", \"msg\":" + "\"" + e.getMessage() + "\"}");
        System.out.println();
      } catch (ClassNotFoundException e) {
        System.out.println(e.getMessage() + " does not exist.");
        System.out.println();
      } catch (MessageDecodingException e) {
        pringMessageDecodeingException(e);
      } catch (IOException e) {
        if (e.getMessage().startsWith("activeConnections")) {
          System.out.println(
              "Lost the connection to the node. " + "Please check the connection between the console and the node.");
        } else if (e.getMessage().startsWith("No value")) {
          System.out.println("The groupID is not configured in dist/conf/applicationContext.xml file.");
        } else {
          System.out.println(e.getMessage());
        }
        System.out.println();
      } 
    	catch (InvocationTargetException e) {
    		Throwable targetException = e.getTargetException();
      	if(targetException.getMessage().contains("\"status\":\"0x1a\""))
      	{
      		System.out.println("The contract address is incorrect.");
      	}
      	else
      	{
      		System.out.println("Contract call failed.");
      	}
        System.out.println();
    	}
      catch (UserInterruptException e) {
        console.close();
      }
      catch (EndOfFileException e) {
      	console.close();
      } catch (Exception e) {
        if (e.getMessage().contains("MessageDecodingException")) {
          pringMessageDecodeingException(
              new MessageDecodingException(e.getMessage().split("MessageDecodingException: ")[1]));
        }
      	if(e.getMessage().contains("\"status\":\"0x1a\""))
      	{
      		System.out.println("The contract address is incorrect.");
      	}
        else {
          System.out.println(e.getMessage());
        }
      	System.out.println();
      }
    }
  }

  private static void pringMessageDecodeingException(MessageDecodingException e) {
    String message = e.getMessage();
    Response t = null;
    try {
      t = ObjectMapperFactory.getObjectMapper(true)
          .readValue(message.substring(message.indexOf("{"), message.lastIndexOf("}") + 1), Response.class);
      if (t != null) {
        ConsoleUtils
            .printJson("{\"code\":" + t.getError().getCode() + ", \"msg\":" + "\"" + t.getError().getMessage() + "\"}");
        System.out.println();
      }
    } catch (Exception e1) {
      System.out.println(e1.getMessage());
      System.out.println();
    }
  }
}
