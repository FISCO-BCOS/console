package console.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import console.common.ConsoleUtils;
import console.common.HelpInfo;
import console.data.entity.AccountInfo;
import console.data.entity.DataEscrowInfo;
import console.data.entity.PasswordInfo;
import console.data.service.SafeKeeperService;
import console.data.tools.AES;
import console.data.tools.Common;
import console.data.tools.ECC;
import console.exception.ConsoleMessageException;
import io.bretty.console.table.Alignment;
import io.bretty.console.table.ColumnFormatter;
import io.bretty.console.table.Table;
import java.io.*;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

public class DataEscrowImpl implements DataEscrowFace {

    private static final Logger logger = LoggerFactory.getLogger(DataEscrowImpl.class);

    private String urlPrefix;
    private String token;
    private String consoleAccountName;
    private String roleName;

    private SafeKeeperService safeKeeperService;

    public DataEscrowImpl() throws Exception {
        try {
            safeKeeperService = new SafeKeeperService();
        } catch (Exception e) {
            throw new ConsoleMessageException("Init rest template error: " + e);
        }
    }

    @Override
    public void setURLPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    @Override
    public String login(String[] params) throws Exception {
        String url = urlPrefix + "accounts/v1/login";
        String account = params[1];
        String accountPwd = params[2];
        logger.info("login");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        LinkedMultiValueMap paramsMap = new LinkedMultiValueMap();
        paramsMap.add("account", account);
        paramsMap.add("accountPwd", accountPwd);
        HttpEntity entity = new HttpEntity(paramsMap, headers);

        try {
            ResponseEntity<String> response =
                    safeKeeperService
                            .getRestTemplate()
                            .exchange(url, HttpMethod.POST, entity, String.class);
            String strBody = response.getBody();
            logger.info(strBody);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode bodyNode = objectMapper.readValue(strBody, JsonNode.class);
            if (0 == bodyNode.get("code").asInt()) {
                JsonNode dataNode = bodyNode.get("data");
                token = dataNode.get("token").asText();
                consoleAccountName = dataNode.get("account").asText();
                roleName = dataNode.get("roleName").asText();
                String accountInfo = consoleAccountName + ":" + roleName;
                return accountInfo;
            } else {
                throw new Exception(strBody);
            }
        } catch (HttpClientErrorException e) {
            throw new Exception(e.getResponseBodyAsString());
        }
    }

    @Override
    public void addAdminAccount(String[] params) throws Exception {
        if (!roleName.equals(Common.SafeKeeper_ROLE_ADMIN)) {
            System.out.println("This command can only be called by admin role.");
            return;
        }

        if (params.length < 2) {
            HelpInfo.promptHelp("addAdminAccount");
            return;
        }
        String param = params[1];
        if ("-h".equals(param) || "--help".equals(param)) {
            HelpInfo.addAdminAccountHelp();
            return;
        }
        if (params.length != 4) {
            HelpInfo.promptHelp("addAdminAccount");
            return;
        }
        String account = params[1];
        String accountPwd = params[2];
        String publicKey = params[3];
        if (!Common.checkUserName(account)) {
            System.out.println("Invalid account. " + Common.ACCOUNT_NAME_FORMAT);
            return;
        }
        if (!Common.checkPassword(accountPwd)) {
            System.out.println("Invalid password. " + Common.PASSWORD_FORMAT);
            return;
        }
        if (publicKey.length() != 128) {
            System.out.println("Invalid public key length, need 128. ");
            return;
        }

        String url = urlPrefix + "accounts/v1";
        AccountInfo bean =
                new AccountInfo(account, accountPwd, publicKey, Common.SafeKeeper_ROLE_ADMIN_ID);
        ObjectMapper objectMapper = new ObjectMapper();
        String str = objectMapper.writeValueAsString(bean);
        logger.info("addAdminAccount, param: {}", str);
        HttpHeaders headers = new HttpHeaders();
        headers.add("AuthorizationToken", "Token " + token);
        headers.add("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<String>(str, headers);
        try {
            ResponseEntity<String> response =
                    safeKeeperService
                            .getRestTemplate()
                            .exchange(url, HttpMethod.POST, entity, String.class);
            String strBody = response.getBody();
            logger.info(strBody);
            JsonNode bodyNode = objectMapper.readValue(strBody, JsonNode.class);
            JsonNode dataNode = bodyNode.get("data");
            String newAccount = dataNode.get("account").asText();
            System.out.println("Add an admin account \"" + newAccount + "\" successfully.");
        } catch (HttpClientErrorException e) {
            System.out.println("Fail, " + e.getResponseBodyAsString());
        }
    }

    @Override
    public void addVisitorAccount(String[] params) throws Exception {
        if (!roleName.equals(Common.SafeKeeper_ROLE_ADMIN)) {
            System.out.println("This command can only be called by admin role.");
            return;
        }

        if (params.length < 2) {
            HelpInfo.promptHelp("addVisitorAccount");
            return;
        }
        String param = params[1];
        if ("-h".equals(param) || "--help".equals(param)) {
            HelpInfo.addVisitorAccountHelp();
            return;
        }
        if (params.length != 3) {
            HelpInfo.promptHelp("addVisitorAccount");
            return;
        }
        String account = params[1];
        String accountPwd = params[2];
        if (!Common.checkUserName(account)) {
            System.out.println("Invalid account. " + Common.ACCOUNT_NAME_FORMAT);
            return;
        }
        if (!Common.checkPassword(accountPwd)) {
            System.out.println("Invalid password. " + Common.PASSWORD_FORMAT);
            return;
        }

        String url = urlPrefix + "accounts/v1";
        AccountInfo bean =
                new AccountInfo(account, accountPwd, "", Common.SafeKeeper_ROLE_VISITOR_ID);
        ObjectMapper objectMapper = new ObjectMapper();
        String str = objectMapper.writeValueAsString(bean);
        logger.info("addVisitorAccount, param: {}", str);
        HttpHeaders headers = new HttpHeaders();
        headers.add("AuthorizationToken", "Token " + token);
        headers.add("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<String>(str, headers);
        try {
            ResponseEntity<String> response =
                    safeKeeperService
                            .getRestTemplate()
                            .exchange(url, HttpMethod.POST, entity, String.class);
            String strBody = response.getBody();
            logger.info(strBody);
            JsonNode bodyNode = objectMapper.readValue(strBody, JsonNode.class);
            JsonNode dataNode = bodyNode.get("data");
            String newAccount = dataNode.get("account").asText();
            System.out.println("Add a visitor account \"" + newAccount + "\" successfully.");
        } catch (HttpClientErrorException e) {
            System.out.println("Fail, " + e.getResponseBodyAsString());
        }
    }

    @Override
    public void deleteAccount(String[] params) throws Exception {
        if (!roleName.equals(Common.SafeKeeper_ROLE_ADMIN)) {
            System.out.println("This command can only be called by admin role.");
            return;
        }

        if (params.length < 2) {
            HelpInfo.promptHelp("deleteAccount");
            return;
        }
        String param = params[1];
        if ("-h".equals(param) || "--help".equals(param)) {
            HelpInfo.deleteAccountHelp();
            return;
        }
        if (params.length != 2) {
            HelpInfo.promptHelp("deleteAccount");
            return;
        }

        String accountName = params[1];
        if (consoleAccountName.equals(accountName)) {
            System.out.println("Cannot delete your own account.");
            return;
        }

        String url = urlPrefix + "accounts/v1/" + accountName;
        HttpHeaders headers = new HttpHeaders();
        headers.add("AuthorizationToken", "Token " + token);
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        logger.info("deleteAccount, url: {}", url);
        try {
            safeKeeperService
                    .getRestTemplate()
                    .exchange(url, HttpMethod.DELETE, entity, String.class);
            System.out.println("Delete an account \"" + accountName + "\" successfully.");
        } catch (HttpClientErrorException e) {
            System.out.println("Fail, " + e.getResponseBodyAsString());
        }
    }

    @Override
    public void listAccount(String[] params) throws Exception {
        if (!roleName.equals(Common.SafeKeeper_ROLE_ADMIN)) {
            System.out.println("This command can only be called by admin role.");
            return;
        }

        if (params.length > 1 && ("-h".equals(params[1]) || "--help".equals(params[1]))) {
            HelpInfo.listAccountHelp();
            return;
        }

        int pageNumber = 1;
        int pageSize = 10;
        String url = urlPrefix + "accounts/v1?pageNumber=" + pageNumber + "&pageSize=" + pageSize;
        HttpHeaders headers = new HttpHeaders();
        headers.add("AuthorizationToken", "Token " + token);
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        ResponseEntity<String> response;
        logger.info("listAccount, url: {}", url);
        try {
            response =
                    safeKeeperService
                            .getRestTemplate()
                            .exchange(url, HttpMethod.GET, entity, String.class);
            String strBody = response.getBody();
            logger.info(strBody);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode bodyNode = objectMapper.readValue(strBody, JsonNode.class);
            String[] tableHeaders = {"name", "role", "createTime"};
            int accountTotalCount = bodyNode.get("totalCount").asInt();
            String[][] tableData = new String[accountTotalCount][3];
            JsonNode dataNode = bodyNode.get("data");
            for (int i = 0; i < dataNode.size(); i++) {
                JsonNode accountNode = dataNode.get(i);
                tableData[i][0] = accountNode.get("account").asText();
                tableData[i][1] = accountNode.get("roleName").asText();
                tableData[i][2] = accountNode.get("createTime").asText();
            }
            int pageTotalCount = accountTotalCount / pageSize;
            if (accountTotalCount % pageSize != 0) {
                pageTotalCount += 1;
            }
            for (int pageIdx = 2; pageIdx <= pageTotalCount; pageIdx++) {
                url = urlPrefix + "accounts/v1?pageNumber=" + pageIdx + "&pageSize=" + pageSize;
                logger.info("listAccount, url: {}", url);
                response =
                        safeKeeperService
                                .getRestTemplate()
                                .exchange(url, HttpMethod.GET, entity, String.class);
                strBody = response.getBody();
                logger.info(strBody);
                bodyNode = objectMapper.readValue(strBody, JsonNode.class);
                if (accountTotalCount != bodyNode.get("totalCount").asInt()) {
                    logger.warn(" the total count has changed");
                    throw new ConsoleMessageException(
                            "The count of accounts has changed, please inquire again.");
                }
                dataNode = bodyNode.get("data");
                for (int i = 0; i < dataNode.size(); i++) {
                    JsonNode accountNode = dataNode.get(i);
                    int index = (pageIdx - 1) * pageSize + i;
                    tableData[index][0] = accountNode.get("account").asText();
                    tableData[index][1] = accountNode.get("roleName").asText();
                    tableData[index][2] = accountNode.get("createTime").asText();
                }
            }
            System.out.println(
                    "The count of account created by \""
                            + consoleAccountName
                            + "\" is "
                            + accountTotalCount
                            + ".");
            if (0 == accountTotalCount) {
                return;
            }
            ConsoleUtils.singleLine();
            ColumnFormatter<String> cf = ColumnFormatter.text(Alignment.CENTER, 30);
            Table table = Table.of(tableHeaders, tableData, cf);
            System.out.println(table);
            ConsoleUtils.singleLine();
        } catch (HttpClientErrorException e) {
            System.out.println("Fail, " + e.getResponseBodyAsString());
        }
    }

    @Override
    public void updatePwd(String[] params) throws Exception {
        if (params.length < 2) {
            HelpInfo.promptHelp("updatePassword");
            return;
        }
        String param = params[1];
        if ("-h".equals(param) || "--help".equals(param)) {
            HelpInfo.updatePasswordHelp();
            return;
        }
        if (params.length != 3) {
            HelpInfo.promptHelp("updatePassword");
            return;
        }
        String oldPassword = params[1];
        String newPassword = params[2];
        if (oldPassword.equals(newPassword)) {
            System.out.println("Old and new password cannot be repeated.");
            return;
        }
        if (!Common.checkPassword(newPassword)) {
            System.out.println("Invalid new password. " + Common.PASSWORD_FORMAT);
            return;
        }

        String url = urlPrefix + "accounts/v1/password";
        PasswordInfo bean = new PasswordInfo(oldPassword, newPassword);
        ObjectMapper objectMapper = new ObjectMapper();
        String str = objectMapper.writeValueAsString(bean);
        logger.info("updatePwd, str: {}", str);
        HttpHeaders headers = new HttpHeaders();
        headers.add("AuthorizationToken", "Token " + token);
        headers.add("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<String>(str, headers);
        try {
            safeKeeperService
                    .getRestTemplate()
                    .exchange(url, HttpMethod.PATCH, entity, String.class);
            System.out.println("Update password successfully.");
        } catch (HttpClientErrorException e) {
            System.out.println("Fail, " + e.getResponseBodyAsString());
        }
    }

    @Override
    public void uploadData(String[] params) throws Exception {
        if (!roleName.equals(Common.SafeKeeper_ROLE_VISITOR)) {
            System.out.println("This command can only be called by visitor role.");
            return;
        }

        if (params.length < 2) {
            HelpInfo.promptHelp("uploadData");
            return;
        }
        String param = params[1];
        if ("-h".equals(param) || "--help".equals(param)) {
            HelpInfo.uploadDataHelp();
            return;
        }
        if (params.length != 4) {
            HelpInfo.promptHelp("uploadData");
            return;
        }

        String creatorPublicKey = getCreatorPublicKey();
        if (creatorPublicKey.length() == 0) {
            return;
        }

        String fileName = params[1];
        String password = params[2];
        String dataId = params[3];

        String plainText;
        try {
            plainText = readFile(Common.FILE_PATH + fileName);
            if (plainText == null) {
                return;
            }
            logger.info("plain text; {}", plainText);
        } catch (Exception e) {
            System.out.println("Fail, " + e.getMessage());
            return;
        }

        String cipherECC = ECC.encrypt(plainText, creatorPublicKey);
        if (cipherECC == null) {
            System.out.println("encrypt the escrow data by public key of creator fail.");
            return;
        }
        String cipherAES = AES.encrypt(plainText, password);
        if (cipherAES == null) {
            System.out.println("encrypt the escrow data by password of account fail.");
            return;
        }

        String url = urlPrefix + "escrow/v1/vaults";
        DataEscrowInfo bean = new DataEscrowInfo(dataId, cipherECC, cipherAES);
        ObjectMapper objectMapper = new ObjectMapper();
        String str = objectMapper.writeValueAsString(bean);
        logger.info("uploadData, str: {}", str);
        HttpHeaders headers = new HttpHeaders();
        headers.add("AuthorizationToken", "Token " + token);
        headers.add("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<String>(str, headers);
        try {
            ResponseEntity<String> response =
                    safeKeeperService
                            .getRestTemplate()
                            .exchange(url, HttpMethod.POST, entity, String.class);
            String strBody = response.getBody();
            logger.info(strBody);
            JsonNode bodyNode = objectMapper.readValue(strBody, JsonNode.class);
            JsonNode dataNode = bodyNode.get("data");
            dataId = dataNode.get("dataID").asText();
            System.out.println("Upload a escrow data \"" + dataId + "\" successfully.");
        } catch (HttpClientErrorException e) {
            System.out.println("Fail, " + e.getResponseBodyAsString());
        }

        return;
    }

    private String getCreatorPublicKey() throws ConsoleMessageException {
        String publicKey;

        String url = urlPrefix + "accounts/v1/publicKey";
        HttpHeaders headers = new HttpHeaders();
        headers.add("AuthorizationToken", "Token " + token);
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        try {
            ResponseEntity<String> response =
                    safeKeeperService
                            .getRestTemplate()
                            .exchange(url, HttpMethod.GET, entity, String.class);
            String strBody = response.getBody();
            logger.info(strBody);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode bodyNode = objectMapper.readValue(strBody, JsonNode.class);
            JsonNode dataNode = bodyNode.get("data");
            publicKey = dataNode.get("creatorPublicKey").asText();
        } catch (HttpClientErrorException e) {
            throw new ConsoleMessageException(
                    "getCreatorPublicKey fail, " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new ConsoleMessageException("getCreatorPublicKey fail, " + e.getMessage());
        }

        return publicKey;
    }

    private String readFile(String filePath) {
        File file = new File(filePath);
        if (file.length() > 4 * 1024) {
            System.out.println("Fail, the file length cannot be greater than 4KB");
            return null;
        }
        try {
            InputStream inputStream = Files.newInputStream(Paths.get(filePath));
            if (inputStream == null) {
                return null;
            }
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println(
                    "["
                            + Paths.get(filePath).toAbsolutePath()
                            + "]"
                            + " cannot be opened because it does not exist.");
        }
        return null;
    }

    private void writeFile(String filePath, String content) {
        FileWriter fwriter = null;
        try {
            fwriter = new FileWriter(filePath);
            fwriter.write(content);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                fwriter.flush();
                fwriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void listData(String[] params) throws Exception {
        if (!roleName.equals(Common.SafeKeeper_ROLE_VISITOR)) {
            System.out.println("This command can only be called by visitor role.");
            return;
        }

        if (params.length > 1 && ("-h".equals(params[1]) || "--help".equals(params[1]))) {
            HelpInfo.listDataHelp();
            return;
        }

        int pageNumber = 1;
        int pageSize = 10;
        String url =
                urlPrefix + "escrow/v1/vaults?pageNumber=" + pageNumber + "&pageSize=" + pageSize;
        HttpHeaders headers = new HttpHeaders();
        headers.add("AuthorizationToken", "Token " + token);
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        ResponseEntity<String> response;
        logger.info("listData, url: {}", url);
        try {
            response =
                    safeKeeperService
                            .getRestTemplate()
                            .exchange(url, HttpMethod.GET, entity, String.class);
            String strBody = response.getBody();
            logger.info(strBody);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode bodyNode = objectMapper.readValue(strBody, JsonNode.class);
            String[] tableHeaders = {"dataID", "createTime"};
            int dataTotalCount = bodyNode.get("totalCount").asInt();
            String[][] tableData = new String[dataTotalCount][2];
            JsonNode dataNode = bodyNode.get("data");
            for (int i = 0; i < dataNode.size(); i++) {
                JsonNode keyNode = dataNode.get(i);
                tableData[i][0] = keyNode.get("dataID").asText();
                tableData[i][1] = keyNode.get("createTime").asText();
            }
            int pageTotalCount = dataTotalCount / pageSize;
            if (dataTotalCount % pageSize != 0) {
                pageTotalCount += 1;
            }
            for (int pageIdx = 2; pageIdx <= pageTotalCount; pageIdx++) {
                url =
                        urlPrefix
                                + "escrow/v1/vaults?pageNumber="
                                + pageIdx
                                + "&pageSize="
                                + pageSize;
                logger.info("listData, url: {}", url);
                response =
                        safeKeeperService
                                .getRestTemplate()
                                .exchange(url, HttpMethod.GET, entity, String.class);
                strBody = response.getBody();
                logger.info(strBody);
                bodyNode = objectMapper.readValue(strBody, JsonNode.class);
                if (dataTotalCount != bodyNode.get("totalCount").asInt()) {
                    logger.warn(" the total count has changed");
                    throw new ConsoleMessageException(
                            "The count of escrow data has changed, please inquire again.");
                }
                dataNode = bodyNode.get("data");
                for (int i = 0; i < dataNode.size(); i++) {
                    JsonNode keyNode = dataNode.get(i);
                    int index = (pageIdx - 1) * pageSize + i;
                    tableData[index][0] = keyNode.get("dataID").asText();
                    tableData[index][1] = keyNode.get("createTime").asText();
                }
            }

            System.out.println(
                    "The count of escrow data uploaded by \""
                            + consoleAccountName
                            + "\" is "
                            + dataTotalCount
                            + ".");
            if (0 == dataTotalCount) {
                return;
            }
            ConsoleUtils.singleLine();
            ColumnFormatter<String> cf = ColumnFormatter.text(Alignment.CENTER, 45);
            Table table = Table.of(tableHeaders, tableData, cf);
            System.out.println(table);
            ConsoleUtils.singleLine();
        } catch (HttpClientErrorException e) {
            System.out.println("Fail, " + e.getResponseBodyAsString());
        }
    }

    @Override
    public void exportData(String[] params) throws Exception {
        if (!roleName.equals(Common.SafeKeeper_ROLE_VISITOR)) {
            System.out.println("This command can only be called by visitor role.");
            return;
        }

        if (params.length < 2) {
            HelpInfo.promptHelp("exportData");
            return;
        }
        String param = params[1];
        if ("-h".equals(param) || "--help".equals(param)) {
            HelpInfo.exportDataHelp();
            return;
        }
        if (params.length != 3) {
            HelpInfo.promptHelp("exportData");
            return;
        }

        String url = urlPrefix + "escrow/v1/vaults";
        String dataId = params[1];
        url += "/" + consoleAccountName + "/" + dataId;
        HttpHeaders headers = new HttpHeaders();
        headers.add("AuthorizationToken", "Token " + token);
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        logger.info("get escrow data in exportData, url: {}", url);
        try {
            ResponseEntity<String> response =
                    safeKeeperService
                            .getRestTemplate()
                            .exchange(url, HttpMethod.GET, entity, String.class);
            String strBody = response.getBody();
            logger.info(strBody);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode bodyNode = objectMapper.readValue(strBody, JsonNode.class);
            JsonNode dataNode = bodyNode.get("data");
            if (dataNode == null) {
                System.out.println("The escrow data not exists.");
                return;
            }
            String cipherText = dataNode.get("cipherText2").asText();
            if (cipherText == null) {
                System.out.println("Get cipher text fail.");
                return;
            }
            String plainText = AES.decrypt(cipherText, params[2]);
            if (plainText == null) {
                System.out.println("Password error.");
                return;
            }
            String filePath = Common.FILE_PATH + dataId + ".txt";
            writeFile(filePath, plainText);
            System.out.println("The escrow data \"" + dataId + "\" has been recorded in " + filePath + ".");
        } catch (HttpClientErrorException e) {
            System.out.println("export escrow data fail, " + e.getResponseBodyAsString());
        }
    }

    @Override
    public void deleteData(String[] params) throws Exception {
        if (!roleName.equals(Common.SafeKeeper_ROLE_VISITOR)) {
            System.out.println("This command can only be called by visitor role.");
            return;
        }

        if (params.length < 2) {
            HelpInfo.promptHelp("deleteData");
            return;
        }
        String param = params[1];
        if ("-h".equals(param) || "--help".equals(param)) {
            HelpInfo.deleteDataHelp();
            return;
        }
        if (params.length != 2) {
            HelpInfo.promptHelp("deleteData");
            return;
        }

        String dataId = params[1];
        String url = urlPrefix + "escrow/v1/vaults/" + dataId;
        HttpHeaders headers = new HttpHeaders();
        headers.add("AuthorizationToken", "Token " + token);
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        logger.info("deleteKey, url: {}", url);
        try {
            safeKeeperService
                    .getRestTemplate()
                    .exchange(url, HttpMethod.DELETE, entity, String.class);
            System.out.println("Delete the escrow data \"" + dataId + "\" successfully.");
        } catch (HttpClientErrorException e) {
            System.out.println("Fail, " + e.getResponseBodyAsString());
        }
    }

    @Override
    public void restoreData(String[] params) throws Exception {
        if (!roleName.equals(Common.SafeKeeper_ROLE_ADMIN)) {
            System.out.println("This command can only be called by admin role.");
            return;
        }

        if (params.length < 2) {
            HelpInfo.promptHelp("restoreData");
            return;
        }
        String param = params[1];
        if ("-h".equals(param) || "--help".equals(param)) {
            HelpInfo.restoreDataHelp();
            return;
        }
        if (params.length != 4) {
            HelpInfo.promptHelp("restoreData");
            return;
        }

        String accountName = params[1];
        String dataId = params[2];
        String privateKey = params[3];
        if (privateKey.length() != 64) {
            System.out.println("Invalid escrow data length, need 64. ");
            return;
        }

        String url = urlPrefix + "escrow/v1/vaults/" + accountName + "/" + dataId;
        HttpHeaders headers = new HttpHeaders();
        headers.add("AuthorizationToken", "Token " + token);
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        logger.info("get escrow data in restoreData, url: {}", url);
        try {
            ResponseEntity<String> response =
                    safeKeeperService
                            .getRestTemplate()
                            .exchange(url, HttpMethod.GET, entity, String.class);
            String strBody = response.getBody();
            logger.info(strBody);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode bodyNode = objectMapper.readValue(strBody, JsonNode.class);
            JsonNode dataNode = bodyNode.get("data");
            if (dataNode == null) {
                System.out.println("The escrow data not exists.");
                return;
            }
            String cipherText = dataNode.get("cipherText1").asText();
            if (cipherText == null) {
                System.out.println("Get cipher text fail.");
                return;
            }
            String plainText = ECC.decrypt(cipherText, privateKey);
            if (plainText == null) {
                System.out.println("Decrypt fail.");
                return;
            }
            String filePath = Common.FILE_PATH + dataId + ".txt";
            writeFile(filePath, plainText);
            System.out.println(
                    "The escrow data \""
                            + dataId
                            + "\" of account \""
                            + accountName
                            + "\" has been recorded in "
                            + filePath
                            + ".");
        } catch (HttpClientErrorException e) {
            System.out.println("restore escrow data fail, " + e.getResponseBodyAsString());
        }
    }
}
