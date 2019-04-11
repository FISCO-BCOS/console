package console.common;


import java.io.File;

import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.WalletUtils;

public class AccoutUtils {
		
	public static void main(String[] args) throws Exception{
   String destinationDir = "key";
   File destination = createDir(destinationDir);

//   try {
//       String password1 = "123";
//			 String walletFileName = WalletUtils.generateFullNewWalletFile(password1, destination);
//       System.out.println("Wallet file " + walletFileName + " successfully created in: " + destinationDir + "\n");
//   } catch (CipherException | IOException | InvalidAlgorithmParameterException
//           | NoSuchAlgorithmException | NoSuchProviderException e) {
//  	 System.out.println(e.getMessage());
//   }
   String password2 = "1234";
   Credentials loadCredentials = WalletUtils.loadCredentials(password2, "key/UTC--2019-04-07T15-01-48.346000000Z--53d3c2bbac439d750151acdc2be8b2736104e224.json");
   System.out.println(loadCredentials.getAddress());
   System.out.println(loadCredentials.getEcKeyPair().getPrivateKey());
	}
	
  private static File createDir(String destinationDir) {
    File destination = new File(destinationDir);

    if (!destination.exists()) {
       System.out.println("Creating directory: " + destinationDir + " ...");
        if (!destination.mkdirs()) {
            System.out.println("Unable to create destination directory ["
                    + destinationDir + "], exiting...");
        } else {
            System.out.println("complete\n");
        }
    }
    return destination;
  }

}
