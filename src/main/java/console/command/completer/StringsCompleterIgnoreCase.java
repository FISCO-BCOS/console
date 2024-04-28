package console.command.completer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.jline.reader.Buffer;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.utils.AttributedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringsCompleterIgnoreCase implements Completer {

    private static final Logger logger = LoggerFactory.getLogger(StringsCompleterIgnoreCase.class);

    protected final Collection<Candidate> candidates = new ArrayList<>();

    public StringsCompleterIgnoreCase() {}

    public StringsCompleterIgnoreCase(String... strings) {
        this(Arrays.asList(strings));
    }

    public StringsCompleterIgnoreCase(Iterable<String> strings) {
        assert strings != null;
        for (String string : strings) {
            candidates.add(
                    new Candidate(
                            AttributedString.stripAnsi(string),
                            string,
                            null,
                            null,
                            null,
                            null,
                            true));
        }
    }

    @Override
    public void complete(
            LineReader reader, final ParsedLine commandLine, final List<Candidate> candidates) {
        if (commandLine == null || candidates == null) {
            return;
        }

        Buffer buffer = reader.getBuffer();
        String start = (buffer == null) ? "" : buffer.toString();
        int index = start.lastIndexOf(" ");
        String tmp = start.substring(index + 1).toLowerCase();

        for (Candidate candidate : this.candidates) {
            String candidateStr = candidate.value().toLowerCase();
            if (candidateStr.startsWith(tmp)) {
                candidates.add(candidate);
            }
        }
    }
}
