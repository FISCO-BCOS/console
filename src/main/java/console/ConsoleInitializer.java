package console;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Properties;

import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.protocol.channel.ResponseExcepiton;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import console.common.Common;
import console.common.HelpInfo;
import console.contract.ContractFace;
import console.contract.ContractImpl;
import console.precompiled.PrecompiledFace;
import console.precompiled.PrecompiledImpl;
import console.precompiled.permission.PermissionFace;
import console.precompiled.permission.PermissionImpl;
import console.web3j.Web3jFace;
import console.web3j.Web3jImpl;

public class ConsoleInitializer {
	
	 private ChannelEthereumService channelEthereumService;
   private ApplicationContext context;
   private ECKeyPair keyPair;
   private StaticGasProvider gasProvider = new StaticGasProvider(new BigInteger("300000000"), new BigInteger("300000000"));
   private Web3j web3j = null;
   private Credentials credentials;
   private String privateKey = "";
   private int groupID;
   public static final int InvalidRequest = 40009;
   
 	 private Web3jFace web3jFace; 
 	 private PrecompiledFace precompiledFace;
 	 private PermissionFace permissionFace;
 	 private ContractFace contractFace;
   
   public void init(String[] args)
   {
  		context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
  		Service service = context.getBean(Service.class);
      groupID = service.getGroupId();
      if (args.length < 2) {
          InputStream is = null;
          OutputStream os = null;
          try {
              // read private key from privateKey.properties
              Properties prop = new Properties();
              Resource keyResource = new ClassPathResource("privateKey.properties");
              if (!keyResource.exists()) {
                  File privateKeyDir = new File("conf/privateKey.properties");
                  privateKeyDir.createNewFile();
                  keyResource = new ClassPathResource("privateKey.properties");
              }
              is = keyResource.getInputStream();
              prop.load(is);
              privateKey = prop.getProperty("privateKey");
              is.close();
              if (privateKey == null) {
                  // save private key in privateKey.properties
                  keyPair = Keys.createEcKeyPair();
                  privateKey = keyPair.getPrivateKey().toString(16);
                  prop.setProperty("privateKey", privateKey);
                  os = new FileOutputStream(keyResource.getFile());
                  prop.store(os, "private key");
                  os.close();
              }
          } catch (Exception e) {
              System.out.println(e.getMessage());
              close();
          }
      }
      switch (args.length) {
          case 0:
              break;
          case 1:
              groupID = setGroupID(args, groupID);
              break;
          default:
              groupID = setGroupID(args, groupID);
              privateKey = args[1];
              break;
      }
      try {
          credentials = GenCredential.create(privateKey);
      } catch (NumberFormatException e) {
          System.out.println("Please provide private key by hex format.");
          close();
      }
      service.setGroupId(groupID);
      try {
          service.run();
      } catch (Exception e) {
          System.out.println(
                  "Failed to connect to the node. Please check the node status and the console configruation.");
          close();
      }
      channelEthereumService = new ChannelEthereumService();
      channelEthereumService.setChannelService(service);
      channelEthereumService.setTimeout(60000);
      web3j = Web3j.build(channelEthereumService, groupID);
      try {
         web3j.getBlockNumber().sendForReturnString();
     		 
         web3jFace = new Web3jImpl();
     		 web3jFace.setWeb3j(web3j);
     		 
     		 precompiledFace = new PrecompiledImpl();
     		 precompiledFace.setWeb3j(web3j);
     		 precompiledFace.setCredentials(credentials);
     		 
     		 permissionFace = new PermissionImpl();
     		 permissionFace.setWeb3j(web3j);
     		 permissionFace.setCredentials(credentials);
     		 
     		 contractFace = new ContractImpl();
     		 contractFace.setGroupID(groupID);
     		 contractFace.setGasProvider(gasProvider);
     		 contractFace.setCredentials(credentials);
     		 contractFace.setWeb3j(web3j);
     		 
      } catch (ResponseExcepiton e) {
          if (e.getCode() == InvalidRequest) {
              System.out.println("Don't connect a removed node.");
          } else {
              System.out.println(e.getMessage());
          }
          close();
      } catch (Exception e) {
          System.out.println(
                  "Failed to connect to the node. Please check the node status and the console configruation.");
          close();
      }
   }
   
   public void switchGroupID(String[] params) throws IOException {
       if (params.length < 2) {
           HelpInfo.promptHelp("switch");
           return;
       }
       if (params.length > 2) {
           HelpInfo.promptHelp("switch");
           return;
       }
       String groupIDStr = params[1];
       if ("-h".equals(groupIDStr) || "--help".equals(groupIDStr)) {
           HelpInfo.switchGroupIDHelp();
           return;
       }
       int toGroupID = 1;
       try {
           toGroupID = Integer.parseInt(groupIDStr);
           if(toGroupID <= 0)
           {
             System.out.println("Please provide group ID by positive integer mode, " + Common.PositiveIntegerRange +".");
             System.out.println();
             return;
           }
       } catch (NumberFormatException e) {
           System.out.println("Please provide group ID by positive integer mode, " + Common.PositiveIntegerRange +".");
           System.out.println();
           return;
       }
       ((AbstractRefreshableApplicationContext)context).refresh();
       Service service = context.getBean(Service.class);
       service.setGroupId(toGroupID);
	    	try {
					service.run();
	        groupID = toGroupID;
				} catch (Exception e) {
	      	System.out.println(
	            "Switch to group "+ toGroupID +" failed! Please check the node status and the console configruation.");
		    	System.out.println();
		    	service.setGroupId(groupID);
		    	try {
						service.run();
					} catch (Exception e1) {
					}
		    	return;
				}
       ChannelEthereumService channelEthereumService = new ChannelEthereumService();
       channelEthereumService.setChannelService(service);
       channelEthereumService.setTimeout(60000);
       web3j = Web3j.build(channelEthereumService, groupID);
       
   		 web3jFace.setWeb3j(web3j);
   		 precompiledFace.setCredentials(credentials);
   		 permissionFace.setWeb3j(web3j);
   		 contractFace.setWeb3j(web3j);
   		 contractFace.setGroupID(groupID);
   		 
       System.out.println("Switched to group " + groupID + ".");
       System.out.println();
   }
	   
	 private int setGroupID(String[] args, int groupID) {
	     try {
	         groupID = Integer.parseInt(args[0]);
	     } catch (NumberFormatException e) {
	         System.out.println("Please provide groupID by integer format.");
	         close();
	     }
	     return groupID;
	   }
 
		 public void close() {
		   try {
		     if (channelEthereumService != null) {
		         channelEthereumService.close();
		     }
		     System.exit(0);
		 } catch (IOException e) {
		     System.out.println(e.getMessage());
		 }
   }

		public ApplicationContext getContext() {
			return context;
		}

		public void setContext(ApplicationContext context) {
			this.context = context;
		}

		public StaticGasProvider getGasProvider() {
			return gasProvider;
		}

		public Web3j getWeb3j() {
			return web3j;
		}

		public void setWeb3j(Web3j web3j) {
			this.web3j = web3j;
		}

		public Credentials getCredentials() {
			return credentials;
		}

		public void setCredentials(Credentials credentials) {
			this.credentials = credentials;
		}

		public int getGroupID() {
			return this.groupID;
		}

		public void setGroupID(int groupID) {
			this.groupID = groupID;
		}

		public Web3jFace getWeb3jFace() {
			return web3jFace;
		}

		public PrecompiledFace getPrecompiledFace() {
			return precompiledFace;
		}

		public PermissionFace getPermissionFace() {
			return permissionFace;
		}

		public ContractFace getContractFace() {
			return contractFace;
		}

}
