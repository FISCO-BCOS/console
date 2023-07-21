package console.command.completer;

import console.common.ConsoleUtils;
import console.contract.utils.ContractCompiler;
import java.io.File;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.utils.AddressUtils;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.utils.AttributedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContractAddressCompleter extends StringsCompleterIgnoreCase {

    private static final Logger logger = LoggerFactory.getLogger(ContractAddressCompleter.class);

    private Client client;
    private static int defaultRecordNum = 20;

    public ContractAddressCompleter(Client client) {
        this.client = client;
    }

    @Override
    public void complete(LineReader reader, ParsedLine commandLine, List<Candidate> candidates) {
        try {
            String buffer = reader.getBuffer().toString().trim();
            String[] ss = buffer.split(" ");
            if (ss.length >= 2) {
                String contractName = ConsoleUtils.getContractName(ConsoleUtils.resolvePath(ss[1]));
                File contractDir =
                        new File(
                                ContractCompiler.COMPILED_PATH
                                        + File.separator
                                        + client.getGroup()
                                        + File.separator
                                        + contractName);
                if (!contractDir.exists()) {
                    return;
                }
                File[] contractAddressFiles = contractDir.listFiles();
                if (contractAddressFiles == null || contractAddressFiles.length == 0) {
                    return;
                }
                String contractPrefix = "[ " + contractName + ".";
                candidates.add(
                        new Candidate(
                                AttributedString.stripAnsi("latest"),
                                "latest",
                                contractPrefix + "0 ]",
                                null,
                                null,
                                null,
                                true));
                ConsoleUtils.sortFiles(contractAddressFiles);
                int recordNum = 0;

                for (File contractAddressFile : contractAddressFiles) {
                    if (!AddressUtils.isValidAddress(contractAddressFile.getName())) {
                        continue;
                    }
                    candidates.add(
                            new Candidate(
                                    AttributedString.stripAnsi(contractAddressFile.getName()),
                                    contractAddressFile.getName(),
                                    contractPrefix + String.valueOf(recordNum + 1) + " ]",
                                    null,
                                    null,
                                    null,
                                    true));
                    recordNum++;
                    if (recordNum == defaultRecordNum) {
                        break;
                    }
                }
            }
            super.complete(reader, commandLine, candidates);
        } catch (Exception e) {
            logger.debug("ContractAddressCompleter exception, error: {}", e.getMessage(), e);
        }
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
