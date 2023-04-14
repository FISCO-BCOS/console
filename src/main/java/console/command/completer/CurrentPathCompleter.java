package console.command.completer;

import console.common.Common;
import console.common.ConsoleUtils;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSPrecompiled.BfsInfo;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSService;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurrentPathCompleter extends StringsCompleterIgnoreCase {
    private static final Logger logger = LoggerFactory.getLogger(CurrentPathCompleter.class);

    private String pwd = "/apps";
    private Client client;
    private BFSService bfsService;

    public CurrentPathCompleter(Client client) {
        this.client = client;
        CryptoKeyPair cryptoKeyPair = client.getCryptoSuite().getCryptoKeyPair();
        this.bfsService = new BFSService(this.client, cryptoKeyPair);
    }

    public void setPwd(String absolutePath) {
        this.pwd = absolutePath;
    }

    protected String getDisplay(Terminal terminal, BfsInfo fileInfo) {
        String name = fileInfo.getFileName();
        if (fileInfo.getFileType().equals(Common.BFS_TYPE_DIR)) {
            AttributedStringBuilder sb = new AttributedStringBuilder();
            sb.styled(AttributedStyle.BOLD.foreground(AttributedStyle.RED), name);
            sb.append("/");
            name = sb.toAnsi(terminal);
        }
        return name;
    }

    @Override
    public void complete(LineReader reader, ParsedLine commandLine, List<Candidate> candidates) {
        try {
            String buffer = commandLine.word().substring(0, commandLine.wordCursor());
            String curBuf;
            int lastSep = buffer.lastIndexOf('/');
            if (lastSep >= 0) {
                curBuf = buffer.substring(0, lastSep + 1);
            } else {
                curBuf = "";
            }

            String fixedPath = pwd;

            if (!curBuf.isEmpty()) {
                fixedPath = ConsoleUtils.fixedBfsParam(curBuf, pwd);
            }

            List<BfsInfo> listResult = bfsService.list(fixedPath);
            for (BfsInfo bfsInfo : listResult) {
                String relativePath = curBuf + bfsInfo.getFileName();
                if (bfsInfo.getFileType().equals(Common.BFS_TYPE_DIR)) {
                    candidates.add(
                            new Candidate(
                                    AttributedString.stripAnsi(relativePath + "/"),
                                    getDisplay(reader.getTerminal(), bfsInfo),
                                    null,
                                    null,
                                    null,
                                    null,
                                    false));
                } else {
                    candidates.add(
                            new Candidate(
                                    AttributedString.stripAnsi(relativePath),
                                    getDisplay(reader.getTerminal(), bfsInfo),
                                    null,
                                    null,
                                    null,
                                    null,
                                    true));
                }
            }
            super.complete(reader, commandLine, candidates);
        } catch (Exception e) {
            logger.debug("CurrentPathCompleter exception, error: {}", e.getMessage(), e);
        }
    }

    public void setClient(Client client) {
        this.client = client;
        this.bfsService = new BFSService(client, client.getCryptoSuite().getCryptoKeyPair());
        this.pwd = "/apps";
    }
}
