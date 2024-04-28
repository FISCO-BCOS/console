package console;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

import console.client.model.TotalTransactionCountResult;
import org.fisco.bcos.sdk.v3.utils.ObjectMapperFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class ConsoleClientTest extends TestBase {

    @Rule
    public final SystemOutRule log = new SystemOutRule().enableLog();

    @Test
    public void clientNotParamsTest() throws IOException {
        String[] emptyParams = {};
        consoleClientFace.getBlockNumber(emptyParams);
        Assert.assertTrue(BigInteger.valueOf(
                        Long.parseLong(log.getLog().replace("\n", "")))
                .compareTo(BigInteger.ZERO) >= 0);
        log.clearLog();

        consoleClientFace.getPbftView(emptyParams);
        Assert.assertTrue(BigInteger.valueOf(
                        Long.parseLong(log.getLog().replace("\n", "")))
                .compareTo(BigInteger.ZERO) >= 0);
        log.clearLog();

        // []
        consoleClientFace.getObserverList(emptyParams);
        Assert.assertTrue(log.getLog().startsWith("["));
        log.clearLog();

        // []
        consoleClientFace.getSealerList(emptyParams);
        Assert.assertTrue(log.getLog().startsWith("["));
        log.clearLog();

        consoleClientFace.getSyncStatus(emptyParams);
        Assert.assertTrue(log.getLog().startsWith("SyncStatusInfo"));
        log.clearLog();

        consoleClientFace.getConsensusStatus(emptyParams);
        Assert.assertTrue(log.getLog().startsWith("ConsensusStatusInfo"));
        log.clearLog();

        consoleClientFace.getPeers(emptyParams);
        Assert.assertTrue(log.getLog().startsWith("PeersInfo"));
        log.clearLog();

        consoleClientFace.getPendingTxSize(emptyParams);
        Assert.assertTrue(BigInteger.valueOf(
                        Long.parseLong(log.getLog().replace("\n", "")))
                .compareTo(BigInteger.ZERO) >= 0);
        log.clearLog();

        consoleClientFace.getTotalTransactionCount(emptyParams);
        TotalTransactionCountResult totalTransactionCountResult = ObjectMapperFactory.getObjectMapper()
                .readValue(log.getLog(), TotalTransactionCountResult.class);
        Assert.assertTrue(totalTransactionCountResult.getBlockNumber().compareTo(BigInteger.ZERO) >= 0);
        Assert.assertTrue(totalTransactionCountResult.getTxSum().compareTo(BigInteger.ZERO) >= 0);
        Assert.assertTrue(totalTransactionCountResult.getFailedTxSum().compareTo(BigInteger.ZERO) >= 0);
        log.clearLog();

        consoleClientFace.listAccount(emptyParams);
        assertFalse(log.getLog().isEmpty());
        log.clearLog();

        consoleClientFace.getGroupList(emptyParams);
        Assert.assertTrue(log.getLog().startsWith("["));
        log.clearLog();

        consoleClientFace.getGroupPeers(emptyParams);
        Assert.assertTrue(log.getLog().startsWith("peer"));
        log.clearLog();

        consoleClientFace.getGroupInfo(emptyParams);
        assertTrue(log.getLog().startsWith("{"));
        log.clearLog();

        consoleClientFace.getGroupInfoList(emptyParams);
        assertTrue(log.getLog().startsWith("["));
        log.clearLog();
        consoleClientFace.getNodeName(consoleInitializer);
    }

    @Test
    public void clientWithParamsTest() throws Exception {

        String transactionHash;
        String contractAddress;
        if (!isWasm) {
            String[] deployParams = {"", "contracts/solidity/HelloWorld.sol"};
            consoleContractFace.deploy(deployParams, "/apps");
            String[] split = log.getLog().split("\n");
            transactionHash = split[0].split(": ")[1];
            contractAddress = split[1].split(": ")[1];
            log.clearLog();

            String[] sendTxParams = {"", "HelloWorld", contractAddress, "set", "testHelloWorld"};
            consoleContractFace.call(sendTxParams, "/apps");
            Assert.assertTrue(log.getLog().contains("transaction status: 0"));
            log.clearLog();

            String[] callParams = {"", "HelloWorld", contractAddress, "get"};
            consoleContractFace.call(callParams, "/apps");
            Assert.assertTrue(log.getLog().contains("testHelloWorld"));
            log.clearLog();

            String version = String.valueOf(Math.abs(new Random().nextInt()));
            String[] deployWithLinkParams = {"", "contracts/solidity/HelloWorld.sol", "-l", "hello/" + version};
            consoleContractFace.deploy(deployWithLinkParams, "/apps");
            Assert.assertTrue(log.getLog().contains("/apps/hello/" + version));
            log.clearLog();

            String[] sendTxWithLinkParams = {"", "/apps/hello/" + version, "set", "testLink"};
            consoleContractFace.call(sendTxWithLinkParams, "/apps");
            Assert.assertTrue(log.getLog().contains("transaction status: 0"));
            log.clearLog();

            String[] callWithLinkParams = {"", "/apps/hello/" + version, "get"};
            consoleContractFace.call(callWithLinkParams, "/apps");
            Assert.assertTrue(log.getLog().contains("testLink"));
            log.clearLog();
        } else {
            String[] deployParams = {"", "contracts/liquid/hello_world", "hello" + Math.abs(new Random().nextInt())};
            consoleContractFace.deploy(deployParams, "/apps");
            String[] split = log.getLog().split("\n");
            transactionHash = split[0].split(": ")[1];
            contractAddress = split[1].split(": ")[1];
            log.clearLog();
        }

        String[] emptyParams = {};
        consoleClientFace.getBlockNumber(emptyParams);
        BigInteger blockNumber = BigInteger.valueOf(
                Long.parseLong(log.getLog().replace("\n", "")));

        assertTrue(blockNumber.compareTo(BigInteger.ZERO) >= 0);
        log.clearLog();

        String[] blockNumberParams = {"", blockNumber.toString()};
        consoleClientFace.getBlockHashByNumber(blockNumberParams);
        assertTrue(log.getLog().startsWith("0x"));
        String blockHash = log.getLog().replace("\n", "");
        log.clearLog();

        // block
        consoleClientFace.getBlockByNumber(blockNumberParams);
        Assert.assertTrue(log.getLog().contains("number='" + blockNumber + "'"));
        Assert.assertTrue(log.getLog().contains("hash='" + blockHash + "'"));
        log.clearLog();

        // block
        String[] blockHashParams = {"", blockHash};
        consoleClientFace.getBlockByHash(blockHashParams);
        Assert.assertTrue(log.getLog().contains("number='" + blockNumber + "'"));
        Assert.assertTrue(log.getLog().contains("hash='" + blockHash + "'"));
        log.clearLog();

        // header
        consoleClientFace.getBlockHeaderByHash(blockHashParams);
        Assert.assertTrue(log.getLog().contains("transactions=null"));
        Assert.assertTrue(log.getLog().contains("number='" + blockNumber + "'"));
        Assert.assertTrue(log.getLog().contains("hash='" + blockHash + "'"));
        log.clearLog();

        // header
        consoleClientFace.getBlockHeaderByNumber(blockNumberParams);
        Assert.assertTrue(log.getLog().contains("transactions=null"));
        Assert.assertTrue(log.getLog().contains("number='" + blockNumber + "'"));
        Assert.assertTrue(log.getLog().contains("hash='" + blockHash + "'"));
        log.clearLog();

        String[] txHashParams = {"", transactionHash};
        // tx
        consoleClientFace.getTransactionByHash(txHashParams);
        Assert.assertTrue(log.getLog().contains(transactionHash));
        log.clearLog();
        consoleClientFace.getTransactionByHashWithProof(txHashParams);
        Assert.assertTrue(log.getLog().contains(transactionHash));
        log.clearLog();

        // receipt
        consoleClientFace.getTransactionReceipt(txHashParams);
        Assert.assertTrue(log.getLog().contains(transactionHash));
        log.clearLog();
        consoleClientFace.getTransactionReceiptByHashWithProof(txHashParams);
        Assert.assertTrue(log.getLog().contains(transactionHash));
        log.clearLog();

        // address
        String[] addressParams = {"", contractAddress};
        consoleClientFace.getCode(addressParams, isWasm, "/apps");
        Assert.assertTrue(log.getLog().startsWith("0x"));
        log.clearLog();

        consoleContractFace.listAbi(consoleInitializer, addressParams, "/apps");
        Assert.assertTrue(log.getLog().startsWith("Method list:"));
        log.clearLog();

        String[] configParams = {"", "tx_gas_limit"};
        consoleClientFace.getSystemConfigByKey(configParams);
        Assert.assertTrue(BigInteger.valueOf(
                        Long.parseLong(log.getLog().replace("\n", "")))
                .compareTo(BigInteger.ZERO) >= 0);
    }
}
