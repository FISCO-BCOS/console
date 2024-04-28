package console.command.completer;

import console.client.ConsoleClientImpl;
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

public class AccountCompleter extends StringsCompleterIgnoreCase {
    private static final Logger logger = LoggerFactory.getLogger(AccountCompleter.class);
    private Client client;
    public static int defaultRecordNum = 20;
    private boolean showAccountPath = true;

    public AccountCompleter(Client client) {
        this.client = client;
    }

    public AccountCompleter(Client client, boolean showAccountPath) {
        this(client);
        this.showAccountPath = showAccountPath;
    }

    @Override
    public void complete(LineReader reader, ParsedLine commandLine, List<Candidate> candidates) {
        List<String> accountList = ConsoleClientImpl.listAccount(client);
        String accountFileDir = ConsoleClientImpl.getAccountDir(client);
        int recordNum = 0;
        // list the account
        String prefix = "[ Account.";
        for (String account : accountList) {
            if (!AddressUtils.isValidAddress(account)) {
                continue;
            }
            candidates.add(
                    new Candidate(
                            AttributedString.stripAnsi(account),
                            account,
                            prefix + String.valueOf(recordNum) + " ]",
                            null,
                            null,
                            null,
                            true));
            recordNum++;
            // list with the account path
            String accountPath = accountFileDir + File.separator + account + ".pem";
            if (!new File(accountPath).exists()) {
                accountPath = accountFileDir + File.separator + account + ".p12";
            }
            if (!new File(accountPath).exists()) {
                continue;
            }
            if (!showAccountPath) {
                continue;
            }
            candidates.add(
                    new Candidate(
                            AttributedString.stripAnsi(accountPath),
                            accountPath,
                            prefix + String.valueOf(recordNum) + " ]",
                            null,
                            null,
                            null,
                            true));
            if (recordNum == defaultRecordNum) {
                break;
            }
        }

        super.complete(reader, commandLine, candidates);
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
