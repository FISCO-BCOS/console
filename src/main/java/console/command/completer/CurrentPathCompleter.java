package console.command.completer;

import console.common.ConsoleUtils;
import java.util.List;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.precompiled.bfs.BFSService;
import org.fisco.bcos.sdk.contract.precompiled.bfs.FileInfo;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
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

    protected String getDisplay(Terminal terminal, FileInfo fileInfo) {
        String name = fileInfo.getName();
        if (fileInfo.getType().equals("directory")) {
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
            String buffer = reader.getBuffer().toString().trim();
            String[] ss = buffer.split(" ");
            String fixedPath = pwd;

            if (ss.length >= 2) {
                fixedPath = ConsoleUtils.fixedBfsParam(ss[1], pwd);
            }

            List<FileInfo> listResult = bfsService.list(fixedPath);
            logger.info("fixedPath: {}", fixedPath);
            for (FileInfo fileInfo : listResult) {
                if (fileInfo.getType().equals("directory")) {
                    candidates.add(
                            new Candidate(
                                    AttributedString.stripAnsi(
                                            fixedPath
                                                    + (fixedPath.equals("/") ? "" : "/")
                                                    + fileInfo.getName()
                                                    + "/"),
                                    getDisplay(reader.getTerminal(), fileInfo),
                                    null,
                                    null,
                                    null,
                                    null,
                                    false));
                } else {
                    candidates.add(
                            new Candidate(
                                    AttributedString.stripAnsi(
                                            fixedPath
                                                    + (fixedPath.equals("/") ? "" : "/")
                                                    + fileInfo.getName()),
                                    getDisplay(reader.getTerminal(), fileInfo),
                                    null,
                                    null,
                                    null,
                                    null,
                                    true));
                }
            }
            logger.info("candidates.size(): {}", candidates.size());
            super.complete(reader, commandLine, candidates);
        } catch (Exception e) {
            logger.debug("CurrentPathCompleter exception, error: {}", e.getMessage(), e);
        }
    }
}
