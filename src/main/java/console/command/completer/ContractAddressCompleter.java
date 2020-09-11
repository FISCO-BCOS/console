package console.command.completer;

import console.common.ConsoleUtils;
import console.contract.utils.ContractCompiler;
import java.io.File;
import java.util.List;
import org.fisco.bcos.sdk.client.Client;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.utils.AttributedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContractAddressCompleter extends StringsCompleterIgnoreCase {

    private static final Logger logger = LoggerFactory.getLogger(ContractAddressCompleter.class);

    private Client client;

    public ContractAddressCompleter(Client client) {
        this.client = client;
    }

    @Override
    public void complete(LineReader reader, ParsedLine commandLine, List<Candidate> candidates) {
        String buffer = reader.getBuffer().toString().trim();
        String[] ss = buffer.split(" ");
        if (ss.length >= 2) {
            String contractName = ss[1];
            File contractDir =
                    new File(
                            ContractCompiler.COMPILED_PATH
                                    + File.separator
                                    + client.getGroupId()
                                    + File.separator
                                    + contractName);
            if (!contractDir.exists()) {
                return;
            }
            File[] contractAddressFiles = contractDir.listFiles();
            ConsoleUtils.sortFiles(contractAddressFiles);
            for (File contractAddressFile : contractAddressFiles) {
                candidates.add(
                        new Candidate(
                                AttributedString.stripAnsi(contractAddressFile.getName()),
                                contractAddressFile.getName(),
                                null,
                                null,
                                null,
                                null,
                                true));
            }
        }
        super.complete(reader, commandLine, candidates);
    }
}
