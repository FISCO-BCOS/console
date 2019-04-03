package console.common;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.jline.builtins.Completers.FilesCompleter;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class JlineUtils {

	public static LineReader getLineReader() throws IOException {
			List<Completer> completers = new ArrayList<Completer>();
			completers.add(new ArgumentCompleter(new StringsCompleter("help")));
			completers.add(new ArgumentCompleter(new StringsCompleter("switch")));
			completers.add(new ArgumentCompleter(new StringsCompleter("getBlockNumber")));
			completers.add(new ArgumentCompleter(new StringsCompleter("getPbftView")));
			completers.add(new ArgumentCompleter(new StringsCompleter("getSealerList")));
			completers.add(new ArgumentCompleter(new StringsCompleter("getObserverList")));
			completers.add(new ArgumentCompleter(new StringsCompleter("getConsensusStatus")));
			completers.add(new ArgumentCompleter(new StringsCompleter("getSyncStatus")));
			completers.add(new ArgumentCompleter(new StringsCompleter("getNodeVersion")));
			completers.add(new ArgumentCompleter(new StringsCompleter("getPeers")));
			completers.add(new ArgumentCompleter(new StringsCompleter("getNodeIDList")));
			completers.add(new ArgumentCompleter(new StringsCompleter("getGroupPeers")));
			completers.add(new ArgumentCompleter(new StringsCompleter("getGroupList")));
			completers.add(new ArgumentCompleter(new StringsCompleter("getBlockByHash")));
			completers.add(new ArgumentCompleter(new StringsCompleter("getBlockByNumber")));
			completers.add(new ArgumentCompleter(new StringsCompleter("getBlockHashByNumber")));
			completers.add(new ArgumentCompleter(new StringsCompleter("getTransactionByHash")));
			completers.add(new ArgumentCompleter(new StringsCompleter("getTransactionByBlockHashAndIndex")));
			completers.add(new ArgumentCompleter(new StringsCompleter("getTransactionByBlockNumberAndIndex")));
			completers.add(new ArgumentCompleter(new StringsCompleter("getTransactionReceipt")));
			completers.add(new ArgumentCompleter(new StringsCompleter("getPendingTransactions")));
			completers.add(new ArgumentCompleter(new StringsCompleter("getPendingTxSize")));
			completers.add(new ArgumentCompleter(new StringsCompleter("getCode")));
			completers.add(new ArgumentCompleter(new StringsCompleter("getTotalTransactionCount")));
			Path path = FileSystems.getDefault().getPath("solidity/contracts/", "");
			completers.add(new ArgumentCompleter(new StringsCompleter("deploy"), new FilesCompleter(path)));
			completers.add(new ArgumentCompleter(new StringsCompleter("call"), new FilesCompleter(path)));
			completers.add(new ArgumentCompleter(new StringsCompleter("deployByCNS"), new FilesCompleter(path)));
			completers.add(new ArgumentCompleter(new StringsCompleter("callByCNS"), new FilesCompleter(path)));
			completers.add(new ArgumentCompleter(new StringsCompleter("queryCNS"), new FilesCompleter(path)));
			completers.add(new ArgumentCompleter(new StringsCompleter("getDeployLog")));
			completers.add(new ArgumentCompleter(new StringsCompleter("addSealer")));
			completers.add(new ArgumentCompleter(new StringsCompleter("addObserver")));
			completers.add(new ArgumentCompleter(new StringsCompleter("removeNode")));
			completers.add(new ArgumentCompleter(new StringsCompleter("grantUserTableManager")));
			completers.add(new ArgumentCompleter(new StringsCompleter("revokeUserTableManager")));
			completers.add(new ArgumentCompleter(new StringsCompleter("listUserTableManager")));
			completers.add(new ArgumentCompleter(new StringsCompleter("grantDeployAndCreateManager")));
			completers.add(new ArgumentCompleter(new StringsCompleter("revokeDeployAndCreateManager")));
			completers.add(new ArgumentCompleter(new StringsCompleter("listDeployAndCreateManager")));
			completers.add(new ArgumentCompleter(new StringsCompleter("grantPermissionManager")));
			completers.add(new ArgumentCompleter(new StringsCompleter("revokePermissionManager")));
			completers.add(new ArgumentCompleter(new StringsCompleter("listPermissionManager")));
			completers.add(new ArgumentCompleter(new StringsCompleter("grantNodeManager")));
			completers.add(new ArgumentCompleter(new StringsCompleter("revokeNodeManager")));
			completers.add(new ArgumentCompleter(new StringsCompleter("listNodeManager")));
			completers.add(new ArgumentCompleter(new StringsCompleter("grantCNSManager")));
			completers.add(new ArgumentCompleter(new StringsCompleter("revokeCNSManager")));
			completers.add(new ArgumentCompleter(new StringsCompleter("listCNSManager")));
			completers.add(new ArgumentCompleter(new StringsCompleter("grantSysConfigManager")));
			completers.add(new ArgumentCompleter(new StringsCompleter("revokeSysConfigManager")));
			completers.add(new ArgumentCompleter(new StringsCompleter("listSysConfigManager")));
			completers.add(new ArgumentCompleter(new StringsCompleter("setSystemConfigByKey"),
					new StringsCompleter(Common.TxCountLimit)));
			completers.add(
					new ArgumentCompleter(new StringsCompleter("setSystemConfigByKey"), new StringsCompleter(Common.TxGasLimit)));
			completers.add(new ArgumentCompleter(new StringsCompleter("getSystemConfigByKey"),
					new StringsCompleter(Common.TxCountLimit)));
			completers.add(
					new ArgumentCompleter(new StringsCompleter("getSystemConfigByKey"), new StringsCompleter(Common.TxGasLimit)));
			completers.add(new ArgumentCompleter(new StringsCompleter("quit")));
			completers.add(new ArgumentCompleter(new StringsCompleter("create")));
			completers.add(new ArgumentCompleter(new StringsCompleter("insert")));
			completers.add(new ArgumentCompleter(new StringsCompleter("select")));
			completers.add(new ArgumentCompleter(new StringsCompleter("update")));
			completers.add(new ArgumentCompleter(new StringsCompleter("delete")));
			completers.add(new ArgumentCompleter(new StringsCompleter("")));

			Terminal terminal = TerminalBuilder.terminal();
			return LineReaderBuilder.builder().terminal(terminal).completer(new AggregateCompleter(completers)).build();
	}
}
