package console.common;

public class WelcomeInfo {
	
  public static void welcome() {
      ConsoleUtils.doubleLine();
      System.out.println("Welcome to FISCO BCOS console!");
      System.out.println("Type 'help' or 'h' for help. Type 'quit' or 'q' to quit console.");
      String logo =
              " ________ ______  ______   ______   ______       _______   ______   ______   ______  \n"
                      + "|        |      \\/      \\ /      \\ /      \\     |       \\ /      \\ /      \\ /      \\ \n"
                      + "| $$$$$$$$\\$$$$$|  $$$$$$|  $$$$$$|  $$$$$$\\    | $$$$$$$|  $$$$$$|  $$$$$$|  $$$$$$\\\n"
                      + "| $$__     | $$ | $$___\\$| $$   \\$| $$  | $$    | $$__/ $| $$   \\$| $$  | $| $$___\\$$\n"
                      + "| $$  \\    | $$  \\$$    \\| $$     | $$  | $$    | $$    $| $$     | $$  | $$\\$$    \\ \n"
                      + "| $$$$$    | $$  _\\$$$$$$| $$   __| $$  | $$    | $$$$$$$| $$   __| $$  | $$_\\$$$$$$\\\n"
                      + "| $$      _| $$_|  \\__| $| $$__/  | $$__/ $$    | $$__/ $| $$__/  | $$__/ $|  \\__| $$\n"
                      + "| $$     |   $$ \\\\$$    $$\\$$    $$\\$$    $$    | $$    $$\\$$    $$\\$$    $$\\$$    $$\n"
                      + " \\$$      \\$$$$$$ \\$$$$$$  \\$$$$$$  \\$$$$$$      \\$$$$$$$  \\$$$$$$  \\$$$$$$  \\$$$$$$";
      System.out.println(logo);
      System.out.println();
      ConsoleUtils.doubleLine();
  }

  public static void help(String[] params) {
      if (HelpInfo.promptNoParams(params, "help")) {
          return;
      }
      if (params.length > 2) {
          HelpInfo.promptHelp("help");
          return;
      }
      ConsoleUtils.singleLine();
      StringBuilder sb = new StringBuilder();
      sb.append("help(h)                                  Provide help information.\n");
      sb.append("switch(s)                                Switch to a specific group by group ID.\n");
      sb.append("getBlockNumber                           Query the number of most recent block.\n");
      sb.append("getPbftView                              Query the pbft view of node.\n");
      sb.append("getSealerList                            Query nodeId list for sealer nodes.\n");
      sb.append("getObserverList                          Query nodeId list for observer nodes.\n");
      sb.append(
              "getNodeIDList                            Query nodeId list for all connected nodes.\n");
      sb.append(
              "getGroupPeers                            Query nodeId list for sealer and observer nodes.\n");
      sb.append(
              "getPeers                                 Query peers currently connected to the client.\n");
      sb.append("getConsensusStatus                       Query consensus status.\n");
      sb.append("getSyncStatus                            Query sync status.\n");
      sb.append("getNodeVersion                           Query the current node version.\n");
      sb.append("getGroupList                             Query group list.\n");
      sb.append(
              "getBlockByHash                           Query information about a block by hash.\n");
      sb.append(
              "getBlockByNumber                         Query information about a block by block number.\n");
      sb.append("getBlockHashByNumber                     Query block hash by block number.\n");
      sb.append(
              "getTransactionByHash                     Query information about a transaction requested by transaction hash.\n");
      sb.append(
              "getTransactionByBlockHashAndIndex        Query information about a transaction by block hash and transaction index position.\n");
      sb.append(
              "getTransactionByBlockNumberAndIndex      Query information about a transaction by block number and transaction index position.\n");
      sb.append(
              "getTransactionReceipt                    Query the receipt of a transaction by transaction hash.\n");
      sb.append("getPendingTransactions                   Query pending transactions.\n");
      sb.append("getPendingTxSize                         Query pending transactions size.\n");
      sb.append("getCode                                  Query code at a given address.\n");
      sb.append("getTotalTransactionCount                 Query total transaction count.\n");
      sb.append("deploy                                   Deploy a contract on blockchain.\n");
      sb.append("getDeployLog                             Query the log of deployed contracts.\n");
      sb.append(
              "call                                     Call a contract by a function and paramters.\n");
      sb.append("deployByCNS                              Deploy a contract on blockchain by CNS.\n");
      sb.append(
          "queryCNS                                 Query CNS information by contract name and contract version.\n");
      sb.append(
              "callByCNS                                Call a contract by a function and paramters by CNS.\n");
      sb.append("addSealer                                Add a sealer node.\n");
      sb.append("addObserver                              Add an observer node.\n");
      sb.append("removeNode                               Remove a node.\n");
      sb.append("setSystemConfigByKey                     Set a system config.\n");
      sb.append("getSystemConfigByKey                     Query a system config value by key.\n");
      sb.append(
              "grantPermissionManager                   Grant permission for permission configuration by address.\n");
      sb.append(
              "revokePermissionManager                  Revoke permission for permission configuration by address.\n");
      sb.append(
              "listPermissionManager                    Query permission information for permission configuration.\n");
      sb.append(
              "grantUserTableManager                    Grant permission for user table by table name and address.\n");
      sb.append(
              "revokeUserTableManager                   Revoke permission for user table by table name and address.\n");
      sb.append(
              "listUserTableManager                     Query permission for user table information.\n");
      sb.append(
              "grantDeployAndCreateManager              Grant permission for deploy contract and create user table by address.\n");
      sb.append(
              "revokeDeployAndCreateManager             Revoke permission for deploy contract and create user table by address.\n");
      sb.append(
              "listDeployAndCreateManager               Query permission information for deploy contract and create user table.\n");
      sb.append(
              "grantNodeManager                         Grant permission for node configuration by address.\n");
      sb.append(
              "revokeNodeManager                        Revoke permission for node configuration by address.\n");
      sb.append(
              "listNodeManager                          Query permission information for node configuration.\n");
      sb.append("grantCNSManager                          Grant permission for CNS by address.\n");
      sb.append("revokeCNSManager                         Revoke permission for CNS by address.\n");
      sb.append("listCNSManager                           Query permission information for CNS.\n");
      sb.append(
              "grantSysConfigManager                    Grant permission for system configuration by address.\n");
      sb.append(
              "revokeSysConfigManager                   Revoke permission for system configuration by address.\n");
      sb.append(
              "listSysConfigManager                     Query permission information for system configuration.\n");
      sb.append("[create sql]                             Create table by sql.\n");
      sb.append("[insert sql]                             Insert records by sql.\n");
      sb.append("[select sql]                             Select records by sql.\n");
      sb.append("[update sql]                             Update records by sql.\n");
      sb.append("[delete sql]                             Remove records by sql.\n");
      sb.append("quit(q)                                  Quit console.");
      System.out.println(sb.toString());
      ConsoleUtils.singleLine();
      System.out.println();
  }
}
