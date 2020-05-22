package console.key;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import console.common.ConsoleUtils;
import console.common.HelpInfo;
import console.exception.ConsoleMessageException;
import console.key.tools.AES;
import console.key.tools.Common;
import console.key.tools.ECC;
import console.key.tools.HttpsRequest;
import io.bretty.console.table.Alignment;
import io.bretty.console.table.ColumnFormatter;
import io.bretty.console.table.Table;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.fisco.bcos.channel.client.P12Manager;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyImpl implements KeyFace {

    private static final Logger logger = LoggerFactory.getLogger(KeyImpl.class);

    private String urlPrefix;
    private String token;
    private String consoleAccountName;
    private String roleName;

    @Override
    public void setURLPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    @Override
    public String login(String[] params) throws Exception {
        String url = urlPrefix + "account/login";
        String account = params[1];
        String accountPwd = params[2];

        Map<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put("account", account);
        paramsMap.put("accountPwd", accountPwd);
        String strBody = HttpsRequest.httpsPostWithForm(url, paramsMap);
        logger.info(strBody);
        JSONObject jsonBody = JSONObject.parseObject(strBody);
        int code = Integer.parseInt(jsonBody.getString("code"));
        if (0 == code) {
            JSONObject data = jsonBody.getJSONObject("data");
            token = data.getString("token");
            consoleAccountName = data.getString("account");
            roleName = data.getString("roleName");
            String accountInfo = consoleAccountName + ":" + roleName;
            return accountInfo;
        } else {
            System.out.println(jsonBody.getString("message"));
            return "";
        }
    }

    @Override
    public void addAdminAccount(String[] params) throws Exception {
        if (!roleName.equals(Common.KMS_ROLE_ADMIN)) {
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

        String url = urlPrefix + "account/addAccount";
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("account", account);
        jsonParam.put("accountPwd", accountPwd);
        jsonParam.put("publicKey", publicKey);
        jsonParam.put("roleId", Common.KMS_ROLE_ADMIN_ID);

        String strBody = HttpsRequest.httpsPostWithJson(url, jsonParam, token);
        JSONObject jsonBody = JSONObject.parseObject(strBody);
        if (jsonBody == null) {
            throw new ConsoleMessageException("The https result is error.");
        }
        if (0 != Integer.parseInt(jsonBody.getString("code"))) {
            System.out.println("Fail, " + jsonBody.getString("message"));
        } else {
            JSONObject data = jsonBody.getJSONObject("data");
            String newAccount = data.getString("account");
            System.out.println("Add an admin account \"" + newAccount + "\" successfully.");
        }
    }

    @Override
    public void addVisitorAccount(String[] params) throws Exception {
        if (!roleName.equals(Common.KMS_ROLE_ADMIN)) {
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

        String url = urlPrefix + "account/addAccount";
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("account", account);
        jsonParam.put("accountPwd", accountPwd);
        jsonParam.put("roleId", Common.KMS_ROLE_VISITOR_ID);

        String strBody = HttpsRequest.httpsPostWithJson(url, jsonParam, token);
        JSONObject jsonBody = JSONObject.parseObject(strBody);
        if (jsonBody == null) {
            throw new ConsoleMessageException("The https result is error.");
        }
        if (0 != Integer.parseInt(jsonBody.getString("code"))) {
            System.out.println("Fail, " + jsonBody.getString("message"));
        } else {
            JSONObject data = jsonBody.getJSONObject("data");
            String newAccount = data.getString("account");
            System.out.println("Add a visitor account \"" + newAccount + "\" successfully.");
        }
    }

    @Override
    public void deleteAccount(String[] params) throws Exception {
        if (!roleName.equals(Common.KMS_ROLE_ADMIN)) {
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

        String url = urlPrefix + "account/deleteAccount/" + accountName;
        String strBody = HttpsRequest.httpsDelete(url, token);
        JSONObject jsonBody = JSONObject.parseObject(strBody);
        if (jsonBody == null) {
            throw new ConsoleMessageException("The https result is error.");
        }
        if (0 != Integer.parseInt(jsonBody.getString("code"))) {
            System.out.println("Fail, " + jsonBody.getString("message"));
        } else {
            System.out.println("Delete an account \"" + accountName + "\" successfully.");
        }
    }

    @Override
    public void listAccount(String[] params) throws Exception {
        if (!roleName.equals(Common.KMS_ROLE_ADMIN)) {
            System.out.println("This command can only be called by admin role.");
            return;
        }

        if (params.length > 1 && ("-h".equals(params[1]) || "--help".equals(params[1]))) {
            HelpInfo.listAccountHelp();
            return;
        }

        int pageNumber = 1;
        int pageSize = 10;
        String url = urlPrefix + "account/accountList/" + pageNumber + "/" + pageSize;
        String strBody = HttpsRequest.httpsGet(url, token);
        JSONObject jsonBody = JSONObject.parseObject(strBody);
        if (jsonBody == null) {
            throw new ConsoleMessageException("The https result is error.");
        }
        if (0 != Integer.parseInt(jsonBody.getString("code"))) {
            System.out.println(jsonBody.getString("message"));
        } else {
            String[] headers = {"name", "role", "createTime"};
            int accountTotalCount = jsonBody.getIntValue("totalCount");
            String[][] tableData = new String[accountTotalCount][3];
            JSONArray data = jsonBody.getJSONArray("data");
            for (int i = 0; i < data.size(); i++) {
                JSONObject account = data.getJSONObject(i);
                tableData[i][0] = account.getString("account");
                tableData[i][1] = account.getString("roleName");
                tableData[i][2] = account.getString("createTime");
            }
            int pageTotalCount = accountTotalCount / pageSize;
            if (accountTotalCount % pageSize != 0) {
                pageTotalCount += 1;
            }
            for (int pageIdx = 2; pageIdx <= pageTotalCount; pageIdx++) {
                url = urlPrefix + "account/accountList/" + pageIdx + "/" + pageSize;
                strBody = HttpsRequest.httpsGet(url, token);
                jsonBody = JSONObject.parseObject(strBody);
                if (jsonBody == null) {
                    throw new ConsoleMessageException("The https result is error.");
                }
                if (0 != Integer.parseInt(jsonBody.getString("code"))) {
                    System.out.println(jsonBody.getString("message"));
                    return;
                } else {
                    if (accountTotalCount != jsonBody.getIntValue("totalCount")) {
                        logger.warn(" the total count has changed");
                        throw new ConsoleMessageException(
                                "The count of accounts has changed, please inquire again.");
                    }
                    data = jsonBody.getJSONArray("data");
                    for (int i = 0; i < data.size(); i++) {
                        JSONObject account = data.getJSONObject(i);
                        int index = (pageIdx - 1) * pageSize + i;
                        tableData[index][0] = account.getString("account");
                        tableData[index][1] = account.getString("roleName");
                        tableData[index][2] = account.getString("createTime");
                    }
                }
            }
            System.out.println(
                    "The count of account created by \""
                            + consoleAccountName
                            + "\" is "
                            + accountTotalCount
                            + ".");
            ConsoleUtils.singleLine();
            ColumnFormatter<String> cf = ColumnFormatter.text(Alignment.CENTER, 30);
            Table table = Table.of(headers, tableData, cf);
            System.out.println(table);
            ConsoleUtils.singleLine();
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

        String url = urlPrefix + "account/updatePassword";
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("oldAccountPwd", oldPassword);
        jsonParam.put("newAccountPwd", newPassword);
        String strBody = HttpsRequest.httpsPut(url, jsonParam, token);
        JSONObject jsonBody = JSONObject.parseObject(strBody);
        if (jsonBody == null) {
            throw new ConsoleMessageException("The https result is error.");
        }
        if (0 != Integer.parseInt(jsonBody.getString("code"))) {
            System.out.println("Fail, " + jsonBody.getString("message"));
        } else {
            System.out.println("Update password successfully.");
        }
    }

    @Override
    public void uploadPrivateKey(String[] params) throws Exception {
        if (!roleName.equals(Common.KMS_ROLE_VISITOR)) {
            System.out.println("This command can only be called by visitor role.");
            return;
        }

        if (params.length < 2) {
            HelpInfo.promptHelp("uploadPrivateKey");
            return;
        }
        String param = params[1];
        if ("-h".equals(param) || "--help".equals(param)) {
            HelpInfo.uploadPrivateKeyHelp();
            return;
        }
        if (params.length < 3 || params.length > 4) {
            HelpInfo.promptHelp("uploadPrivateKey");
            return;
        }

        ECKeyPair keyPair = getECKeyPair(params);
        if (keyPair == null) {
            System.out.println("Cannot get the private key to be uploaded.");
            return;
        }

        String creatorPublicKey = getCreatorPublicKey();
        if (creatorPublicKey.equals("")) {
            System.out.println("The public key used to encrypt private key of account is empty.");
            return;
        } else {
            logger.info(
                    "The public key used to encrypt private key of account is {}",
                    creatorPublicKey);
        }

        String password = params[2];
        String privateKeyHex = keyPair.getPrivateKey().toString(16);
        logger.info(
                "The private key is {}, public key is {}",
                privateKeyHex,
                keyPair.getPublicKey().toString(16));
        String alias;
        if (params.length == 4) {
            alias = params[3];
        } else if (params.length == 3) {
            Credentials credentials = Credentials.create(keyPair);
            alias = credentials.getAddress();
        } else {
            HelpInfo.promptHelp("uploadPrivateKey");
            return;
        }

        String cipherECC = ECC.encrypt(privateKeyHex, creatorPublicKey);
        if (cipherECC == null) {
            System.out.println("encrypt the private key by public key of creator fail.");
            return;
        }
        String cipherAES = AES.encrypt(privateKeyHex, password);
        if (cipherAES == null) {
            System.out.println("encrypt the private key by password of account fail.");
            return;
        }

        String url = urlPrefix + "escrow/addKey";
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("keyAlias", alias);
        jsonParam.put("cipherText", cipherECC);
        jsonParam.put("privateKey", cipherAES);
        String strBody = HttpsRequest.httpsPostWithJson(url, jsonParam, token);
        JSONObject jsonBody = JSONObject.parseObject(strBody);
        if (jsonBody == null) {
            throw new ConsoleMessageException("The https result is error.");
        }
        if (0 != Integer.parseInt(jsonBody.getString("code"))) {
            System.out.println(jsonBody.getString("message"));
        } else {
            JSONObject data = jsonBody.getJSONObject("data");
            alias = data.getString("keyAlias");
            System.out.println("Upload a private key \"" + alias + "\" successfully.");
        }

        return;
    }

    private String getCreatorPublicKey() {
        String publicKey = "";

        String url = urlPrefix + "account/getPublicKey";
        String strBody = HttpsRequest.httpsGet(url, token);
        JSONObject jsonBody = JSONObject.parseObject(strBody);
        if (jsonBody == null) {
            logger.error("The http result is error.");
            return "";
        }
        if (0 != Integer.parseInt(jsonBody.getString("code"))) {
            logger.error("getCreatorPublicKey return: {}", strBody);
        } else {
            JSONObject data = jsonBody.getJSONObject("data");
            publicKey = data.getString("publicKey");
        }

        return publicKey;
    }

    private ECKeyPair getECKeyPair(String[] params) {
        ECKeyPair keyPair = null;

        String keyFile = Common.FILE_PATH + params[1];
        String password = params[2];
        InputStream in = readAccountFile(keyFile);
        if (null == in) {
            return null;
        }

        try {
            if (keyFile.endsWith("p12")) {
                P12Manager p12Manager = new P12Manager();
                p12Manager.setPassword(password);
                p12Manager.load(in, password);
                keyPair = p12Manager.getECKeyPair();
            } else if (keyFile.endsWith("pem")) {
                PEMManager pem = new PEMManager();
                pem.load(in);
                keyPair = pem.getECKeyPair();
            } else {
                System.out.println(" invalid file format, file name: " + keyFile);
                logger.error(" invalid file format, file name: {}", keyFile);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.error(" message: {}, e: {}", e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    logger.error(" message: {}, e: {}", e.getMessage(), e);
                }
            }
        }

        return keyPair;
    }

    private InputStream readAccountFile(String fileName) {
        try {
            return Files.newInputStream(Paths.get(fileName));
        } catch (IOException e) {
            System.out.println(
                    "["
                            + Paths.get(fileName).toAbsolutePath()
                            + "]"
                            + " cannot be opened because it does not exist.");
        }
        return null;
    }

    @Override
    public void listPrivateKey(String[] params) throws Exception {
        if (!roleName.equals(Common.KMS_ROLE_VISITOR)) {
            System.out.println("This command can only be called by visitor role.");
            return;
        }

        if (params.length > 1 && ("-h".equals(params[1]) || "--help".equals(params[1]))) {
            HelpInfo.listPrivateKeyHelp();
            return;
        }

        int pageNumber = 1;
        int pageSize = 10;
        String url = urlPrefix + "escrow/keyList/" + pageNumber + "/" + pageSize;
        String strBody = HttpsRequest.httpsGet(url, token);
        JSONObject jsonBody = JSONObject.parseObject(strBody);
        if (jsonBody == null) {
            throw new ConsoleMessageException("The https result is error.");
        }
        if (0 != Integer.parseInt(jsonBody.getString("code"))) {
            System.out.println(jsonBody.getString("message"));
        } else {
            String[] headers = {"alias", "createTime"};
            int keyTotalCount = jsonBody.getIntValue("totalCount");
            String[][] tableData = new String[keyTotalCount][2];
            JSONArray data = jsonBody.getJSONArray("data");
            for (int i = 0; i < data.size(); i++) {
                JSONObject key = data.getJSONObject(i);
                tableData[i][0] = key.getString("keyAlias");
                tableData[i][1] = key.getString("createTime");
            }
            int pageTotalCount = keyTotalCount / pageSize;
            if (keyTotalCount % pageSize != 0) {
                pageTotalCount += 1;
            }
            for (int pageIdx = 2; pageIdx <= pageTotalCount; pageIdx++) {
                url = urlPrefix + "escrow/keyList/" + pageIdx + "/" + pageSize;
                strBody = HttpsRequest.httpsGet(url, token);
                jsonBody = JSONObject.parseObject(strBody);
                if (jsonBody == null) {
                    throw new ConsoleMessageException("The https result is error.");
                }
                if (0 != Integer.parseInt(jsonBody.getString("code"))) {
                    System.out.println(jsonBody.getString("message"));
                    return;
                } else {
                    if (keyTotalCount != jsonBody.getIntValue("totalCount")) {
                        logger.warn(" the count of uploaded keys has changed");
                        throw new ConsoleMessageException(
                                "The count of uploaded keys has changed, please inquire again.");
                    }
                    data = jsonBody.getJSONArray("data");
                    for (int i = 0; i < data.size(); i++) {
                        JSONObject key = data.getJSONObject(i);
                        int index = (pageIdx - 1) * pageSize + i;
                        tableData[index][0] = key.getString("keyAlias");
                        tableData[index][1] = key.getString("createTime");
                    }
                }
            }

            System.out.println(
                    "The count of keys uploaded by \""
                            + consoleAccountName
                            + "\" is "
                            + keyTotalCount
                            + ".");
            ConsoleUtils.singleLine();
            ColumnFormatter<String> cf = ColumnFormatter.text(Alignment.CENTER, 45);
            Table table = Table.of(headers, tableData, cf);
            System.out.println(table);
            ConsoleUtils.singleLine();
        }
    }

    @Override
    public void exportPrivateKey(String[] params) throws Exception {
        if (!roleName.equals(Common.KMS_ROLE_VISITOR)) {
            System.out.println("This command can only be called by visitor role.");
            return;
        }

        if (params.length < 2) {
            HelpInfo.promptHelp("exportPrivateKey");
            return;
        }
        String param = params[1];
        if ("-h".equals(param) || "--help".equals(param)) {
            HelpInfo.exportPrivateKeyHelp();
            return;
        }
        if (params.length != 3) {
            HelpInfo.promptHelp("exportPrivateKey");
            return;
        }

        String url = urlPrefix + "escrow/queryKey";
        String keyAlias = params[1];
        url += "/" + consoleAccountName + "/" + keyAlias;
        String strBody = HttpsRequest.httpsGet(url, token);
        JSONObject jsonBody = JSONObject.parseObject(strBody);
        if (jsonBody == null) {
            throw new ConsoleMessageException("The https result is error.");
        }
        if (0 != Integer.parseInt(jsonBody.getString("code"))) {
            System.out.println(jsonBody.getString("message"));
            return;
        }

        JSONObject data = jsonBody.getJSONObject("data");
        if (data == null) {
            System.out.println("The private key not exists.");
            return;
        }
        String cipherText = data.getString("privateKey");
        if (cipherText == null) {
            System.out.println("Get cipher text fail.");
            return;
        }
        String plainText = AES.decrypt(cipherText, params[2]);
        if (plainText == null) {
            System.out.println("Password error.");
            return;
        }
        System.out.println("The private key \"" + keyAlias + "\" is " + plainText + ".");
    }

    @Override
    public void deletePrivateKey(String[] params) throws Exception {
        if (!roleName.equals(Common.KMS_ROLE_VISITOR)) {
            System.out.println("This command can only be called by visitor role.");
            return;
        }

        if (params.length < 2) {
            HelpInfo.promptHelp("deletePrivateKey");
            return;
        }
        String param = params[1];
        if ("-h".equals(param) || "--help".equals(param)) {
            HelpInfo.deletePrivateKeyHelp();
            return;
        }
        if (params.length != 2) {
            HelpInfo.promptHelp("deletePrivateKey");
            return;
        }

        String url = urlPrefix + "escrow/deleteKey";
        String keyAlias = params[1];
        url += "/" + consoleAccountName + "/" + keyAlias;
        String strBody = HttpsRequest.httpsDelete(url, token);
        JSONObject jsonBody = JSONObject.parseObject(strBody);
        if (jsonBody == null) {
            throw new ConsoleMessageException("The https result is error.");
        }
        if (0 != Integer.parseInt(jsonBody.getString("code"))) {
            System.out.println(jsonBody.getString("message"));
            return;
        } else {
            System.out.println("Delete the private key \"" + keyAlias + "\" successfully.");
        }
    }

    @Override
    public void restorePrivateKey(String[] params) throws Exception {
        if (!roleName.equals(Common.KMS_ROLE_ADMIN)) {
            System.out.println("This command can only be called by admin role.");
            return;
        }

        if (params.length < 2) {
            HelpInfo.promptHelp("restorePrivateKey");
            return;
        }
        String param = params[1];
        if ("-h".equals(param) || "--help".equals(param)) {
            HelpInfo.restorePrivateKeyHelp();
            return;
        }
        if (params.length != 4) {
            HelpInfo.promptHelp("restorePrivateKey");
            return;
        }

        String accountName = params[1];
        String keyAlias = params[2];
        String privateKey = params[3];
        if (privateKey.length() != 64) {
            System.out.println("Invalid private key length, need 64. ");
            return;
        }

        String url = urlPrefix + "escrow/queryKey";
        url += "/" + accountName + "/" + keyAlias;
        String strBody = HttpsRequest.httpsGet(url, token);
        JSONObject jsonBody = JSONObject.parseObject(strBody);
        if (jsonBody == null) {
            throw new ConsoleMessageException("The https result is error.");
        }
        if (0 != Integer.parseInt(jsonBody.getString("code"))) {
            System.out.println(jsonBody.getString("message"));
            return;
        }

        JSONObject data = jsonBody.getJSONObject("data");
        if (data == null) {
            System.out.println("The private key not exists.");
            return;
        }
        String cipherText = data.getString("cipherText");
        if (cipherText == null) {
            System.out.println("Get cipher text fail.");
            return;
        }
        String plainText = ECC.decrypt(cipherText, privateKey);
        if (plainText == null) {
            System.out.println("Decrypt fail.");
            return;
        }
        System.out.println(
                "The private key \""
                        + keyAlias
                        + "\" of account \""
                        + accountName
                        + "\" is "
                        + plainText
                        + ".");
    }
}
