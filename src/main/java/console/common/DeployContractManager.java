package console.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeployContractManager {

    private static final Logger logger = LoggerFactory.getLogger(DeployContractManager.class);

    private String groupId;

    /** key: groupId value: deployed contract list */
    private Map<String, List<DeployedContract>> groupId2DeployContractMap =
            new ConcurrentHashMap<>();

    public Map<String, List<DeployedContract>> getGroupId2DeployContractMap() {
        return this.groupId2DeployContractMap;
    }

    public void setGroupId2DeployContractMap(
            final Map<String, List<DeployedContract>> groupId2DeployContractMap) {
        this.groupId2DeployContractMap = groupId2DeployContractMap;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(final String groupId) {
        this.groupId = groupId;
    }

    /**
     * @param groupId
     * @return
     */
    public List<DeployedContract> getDeployContractList(String groupId) {
        return groupId2DeployContractMap.get(groupId);
    }

    /**
     * @param groupId
     * @param contractName
     * @return
     */
    public List<DeployedContract> getDeployContractList(String groupId, String contractName) {
        List<DeployedContract> deployedContracts = groupId2DeployContractMap.get(groupId);
        List<DeployedContract> deployedContractList = new ArrayList<>();
        if (deployedContracts != null) {
            for (int i = 0; i < deployedContracts.size(); i++) {
                if (deployedContracts.get(i).getContractName().equals(contractName)) {
                    deployedContractList.add(deployedContracts.get(i));
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug(" groupId: {}, list: {}", groupId, deployedContractList);
        }

        return deployedContractList;
    }

    /**
     * @param groupId
     * @return
     */
    public DeployedContract getLatestDeployContract(String groupId, String contractName) {
        List<DeployedContract> deployContractList = getDeployContractList(groupId);
        if (deployContractList == null || deployContractList.isEmpty()) {
            return null;
        }

        for (int i = 0; i < deployContractList.size(); i++) {
            if (deployContractList.get(i).getContractName().equals(contractName)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            " latest contract: {}, deploy info: {}",
                            contractName,
                            deployContractList.get(i));
                }
                return deployContractList.get(i);
            }
        }

        return null;
    }

    /**
     * @param groupId
     * @return
     */
    public DeployedContract getDeployContractByIndex(
            String groupId, String contractName, int index) {
        List<DeployedContract> deployContractList = getDeployContractList(groupId);
        if (deployContractList == null || deployContractList.size() < index) {
            return null;
        }

        int count = 0;
        for (int i = 0; i < deployContractList.size(); i++) {
            if (deployContractList.get(i).getContractName().equals(contractName)) {
                if (count == index) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                " index: {}, contract: {}, deploy info: {}",
                                index,
                                contractName,
                                deployContractList.get(i));
                    }
                    return deployContractList.get(i);
                }
                count = count + 1;
            }
        }

        return null;
    }

    /** @return */
    public static DeployContractManager newGroupDeployedContractManager() {

        DeployContractManager deployContractManager = new DeployContractManager();
        File logFile = new File(Common.ContractLogFileName);
        if (!logFile.exists()) {
            logger.info("{} not exist ", Common.ContractLogFileName);
            return deployContractManager;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                try {
                    String[] ss = ConsoleUtils.tokenizeCommand(line);
                    if (Objects.isNull(ss) || ss.length < 5) {
                        continue;
                    }

                    /**
                     * 2020-08-19 17:27:49 [group:1] HelloWorld
                     * 0x88b54647d6bcc9784a7c1406de4fef33da10c63e
                     */
                    int i = ss[2].indexOf(":");
                    int j = ss[2].indexOf("]");

                    deployContractManager.addDeployContract(
                            ss[0] + " " + ss[1], ss[2].substring(i + 1, j), ss[3], ss[4]);

                } catch (Exception e) {
                    logger.error("line: {}, e: {}", line, e);
                }
            }
        } catch (Exception e) {
            logger.error(" load deployed, e: {}", e);
        }

        if (logger.isDebugEnabled()) {
            Map<String, List<DeployedContract>> groupId2DeployContractMap =
                    deployContractManager.getGroupId2DeployContractMap();

            for (Map.Entry<String, List<DeployedContract>> entry :
                    groupId2DeployContractMap.entrySet()) {
                logger.debug(
                        " groupId: {}, entry size: {}", entry.getKey(), entry.getValue().size());
            }
        }

        return deployContractManager;
    }

    /**
     * @param groupId
     * @param contractName
     * @param contractAddress
     */
    public void addNewDeployContract(String groupId, String contractName, String contractAddress) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestamp = LocalDateTime.now().format(formatter);
        addDeployContract(timestamp, groupId, contractName, contractAddress);
        writeDeployContract(groupId, contractName, contractAddress, timestamp);
    }

    /**
     * @param groupId
     * @param contractName
     * @param contractAddress
     * @param timestamp
     */
    public void writeDeployContract(
            String groupId, String contractName, String contractAddress, String timestamp) {

        String log =
                timestamp + "  [group:" + groupId + "]  " + contractName + "  " + contractAddress;

        try (PrintWriter pw = new PrintWriter(new FileWriter(Common.ContractLogFileName, true))) {
            pw.println(log);
            logger.info(" deploy log: {}", log);
        } catch (Exception e) {
            logger.warn(" deploy log, log: {}, e: {}", log, e);
        }
    }

    /**
     * @param timestamp
     * @param groupId
     * @param contractName
     * @param contractAddress
     * @return
     */
    public void addDeployContract(
            String timestamp, String groupId, String contractName, String contractAddress) {

        List<DeployedContract> deployedContractList = groupId2DeployContractMap.get(groupId);
        if (deployedContractList == null) {
            deployedContractList = new LinkedList<>();
            groupId2DeployContractMap.put(groupId, deployedContractList);
        }

        DeployedContract deployedContract = new DeployedContract();
        deployedContract.setTimestamp(timestamp);
        deployedContract.setGroupId(groupId);
        deployedContract.setContractName(contractName);
        deployedContract.setContractAddress(contractAddress);

        deployedContractList.add(0, deployedContract);
    }

    public static class DeployedContract {

        private String groupId;
        private String timestamp;
        private String contractName;
        private String contractAddress;

        public String getTimestamp() {
            return this.timestamp;
        }

        public void setTimestamp(final String timestamp) {
            this.timestamp = timestamp;
        }

        public String getContractName() {
            return this.contractName;
        }

        public void setContractName(final String contractName) {
            this.contractName = contractName;
        }

        public String getContractAddress() {
            return this.contractAddress;
        }

        public void setContractAddress(final String contractAddress) {
            this.contractAddress = contractAddress;
        }

        public String getGroupId() {
            return this.groupId;
        }

        public void setGroupId(final String groupId) {
            this.groupId = groupId;
        }

        @Override
        public String toString() {
            return "DeployedContract{"
                    + "groupId='"
                    + groupId
                    + '\''
                    + ", timestamp='"
                    + timestamp
                    + '\''
                    + ", contractName='"
                    + contractName
                    + '\''
                    + ", contractAddress='"
                    + contractAddress
                    + '\''
                    + '}';
        }
    }
}
