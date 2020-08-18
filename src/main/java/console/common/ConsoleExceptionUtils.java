package console.common;

import org.fisco.bcos.sdk.client.protocol.response.JsonRpcResponse;
import org.fisco.bcos.sdk.utils.ObjectMapperFactory;
import org.fisco.bcos.sdk.utils.exceptions.MessageDecodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleExceptionUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleExceptionUtils.class);

    public static void pringMessageDecodeingException(MessageDecodingException e) {
        String message = e.getMessage();
        JsonRpcResponse t = null;
        try {
            t =
                    ObjectMapperFactory.getObjectMapper()
                            .readValue(
                                    message.substring(
                                            message.indexOf("{"), message.lastIndexOf("}") + 1),
                                    JsonRpcResponse.class);
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
        } catch (Exception e1) {
            System.out.println(e1.getMessage());
            System.out.println();
            logger.error(" message: {}, e: {}", e1.getMessage(), e1);
        }
    }
}
