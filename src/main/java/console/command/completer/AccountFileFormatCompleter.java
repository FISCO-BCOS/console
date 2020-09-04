package console.command.completer;

import java.util.Arrays;
import java.util.List;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.utils.AttributedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountFileFormatCompleter extends StringsCompleterIgnoreCase {
    private static final Logger logger = LoggerFactory.getLogger(AccountFileFormatCompleter.class);

    @Override
    public void complete(LineReader reader, ParsedLine commandLine, List<Candidate> candidates) {
        List<String> accountFileFormat = Arrays.asList("pem", "p12");
        for (String format : accountFileFormat) {
            candidates.add(
                    new Candidate(
                            AttributedString.stripAnsi(format),
                            format,
                            null,
                            null,
                            null,
                            null,
                            true));
        }
        super.complete(reader, commandLine, candidates);
    }
}
