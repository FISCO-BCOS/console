package console.command.completer;

import console.contract.model.AbiAndBin;
import console.contract.utils.ContractCompiler;
import java.util.List;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.codegen.CodeGenUtils;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.utils.AttributedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContractMethodCompleter extends StringsCompleterIgnoreCase {

    private static final Logger logger = LoggerFactory.getLogger(ContractMethodCompleter.class);
    private Client client;

    public ContractMethodCompleter(Client client) {
        this.client = client;
    }

    @Override
    public void complete(LineReader reader, ParsedLine commandLine, List<Candidate> candidates) {

        String buffer = reader.getBuffer().toString().trim();
        String[] ss = buffer.split(" ");

        if (ss.length >= 3) {
            String contractName = ContractCompiler.removeSolPostfix(ss[1]);
            String contractAddress = ss[2];
            try {
                AbiAndBin abiAndBin =
                        ContractCompiler.loadAbiAndBin(
                                client.getGroupId(), contractName, contractAddress);
                List<ABIDefinition> abiDefinitions =
                        CodeGenUtils.loadContractAbiDefinition(abiAndBin.getAbi());
                for (ABIDefinition definition : abiDefinitions) {
                    String functionName = definition.getName();
                    candidates.add(
                            new Candidate(
                                    AttributedString.stripAnsi(functionName),
                                    functionName,
                                    null,
                                    null,
                                    null,
                                    null,
                                    true));
                }
            } catch (Exception e) {
                logger.trace("e: ", e);
            }
        }
        super.complete(reader, commandLine, candidates);
    }
}
