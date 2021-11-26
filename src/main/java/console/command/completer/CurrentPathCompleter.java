package console.command.completer;

import java.util.Arrays;
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

    private String pwd = "/";
    private Client client;
    private BFSService bfsService;
    private List<String> fallbackStrings =
            Arrays.asList("..", "../", "../..", "../../", "../../..");

    public CurrentPathCompleter(Client client) {
        this.client = client;
        CryptoKeyPair cryptoKeyPair = client.getCryptoSuite().getCryptoKeyPair();
        this.bfsService = new BFSService(this.client, cryptoKeyPair);
    }

    public void setPwd(String absolutePath) {
        this.pwd = absolutePath;
    }

    private void handleFallbackSymbol(String curPath, String buffer) {
        int fallBackTime = 0;
        for (String str : buffer.split("/")) {
            if (str.equals("..")) fallBackTime++;
        }
        for (int i = 0; i < fallBackTime; ++i) {
            int lastIndexOfSeparator = curPath.lastIndexOf('/');
            curPath =
                    lastIndexOfSeparator <= 0
                            ? "/"
                            : curPath.substring(0, lastIndexOfSeparator - 1);
        }
        pwd = curPath;
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

            if (ss.length >= 2 && fallbackStrings.contains(ss[1])) {
                // FIXME: fix fallback symbol
                handleFallbackSymbol(pwd, ss[1]);
            }

            List<FileInfo> listResult;
            listResult = bfsService.list(pwd);
            if (!listResult.isEmpty()) {
                for (FileInfo fileInfo : listResult) {
                    if (!fileInfo.getName().matches("^[0-9a-zA-Z-_]{1,56}$")) {
                        continue;
                    }
                    if (fileInfo.getType().equals("directory")) {
                        candidates.add(
                                new Candidate(
                                        AttributedString.stripAnsi(fileInfo.getName()),
                                        getDisplay(reader.getTerminal(), fileInfo),
                                        null,
                                        null,
                                        null,
                                        null,
                                        false));
                    } else {
                        candidates.add(
                                new Candidate(
                                        AttributedString.stripAnsi(fileInfo.getName()),
                                        getDisplay(reader.getTerminal(), fileInfo),
                                        null,
                                        null,
                                        null,
                                        null,
                                        true));
                    }
                }
            } else {
                logger.debug("CurrentPathCompleter: no such file or directory: " + pwd);
            }
            super.complete(reader, commandLine, candidates);
        } catch (Exception e) {
            logger.debug("CurrentPathCompleter exception, error: {}", e.getMessage(), e);
        }
    }
}
