package console.command.completer;

import console.client.ConsoleClientImpl;
import console.common.ConsoleUtils;
import java.io.File;
import java.util.List;
import org.fisco.bcos.sdk.client.Client;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.utils.AttributedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountCompleter extends StringsCompleterIgnoreCase {
    private static final Logger logger = LoggerFactory.getLogger(AccountCompleter.class);
    private Client client;
    public static int defaultRecordNum = 20;

    public AccountCompleter(Client client) {
        this.client = client;
    }

    @Override
    public void complete(LineReader reader, ParsedLine commandLine, List<Candidate> candidates) {
        List<String> accountList = ConsoleClientImpl.listAccount(client);
        String accountFileDir = ConsoleClientImpl.getAccountDir(client);
        int recordNum = 0;
        // list the account
        for (String account : accountList) {
            if (!ConsoleUtils.isValidAddress(account)) {
                continue;
            }
            candidates.add(
                    new Candidate(
                            AttributedString.stripAnsi(account),
                            account,
                            null,
                            null,
                            null,
                            null,
                            true));
            // list with the account path
            String accountPath = accountFileDir + File.separator + account + ".pem";
            if (!new File(accountPath).exists()) {
                accountPath = accountFileDir + File.separator + account + ".p12";
            }
            if (!new File(accountPath).exists()) {
                continue;
            }
            candidates.add(
                    new Candidate(
                            AttributedString.stripAnsi(accountPath),
                            accountPath,
                            null,
                            null,
                            null,
                            null,
                            true));
            recordNum++;
            if (recordNum == defaultRecordNum) {
                break;
            }
        }

        super.complete(reader, commandLine, candidates);
    }
}
