package console.common;

import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.core.Response;
import org.fisco.bcos.web3j.protocol.exceptions.MessageDecodingException;

public class ConsoleExceptionUtils {
	
	public static void pringMessageDecodeingException(MessageDecodingException e) {
		String message = e.getMessage();
		Response t = null;
		try {
		    t = ObjectMapperFactory.getObjectMapper(true).readValue(
		                message.substring(message.indexOf("{"), message.lastIndexOf("}") + 1),
		                Response.class);
		    if (t != null) {
		      ConsoleUtils.printJson(
		          "{\"code\":"
		              + t.getError().getCode()
		              + ", \"msg\":"
		              + "\""
		              + t.getError().getMessage()
		              + "\"}");
		      System.out.println();
		    }
		  }catch (Exception e1) {
		    System.out.println(e1.getMessage());
		    System.out.println();
    }
	}
}
