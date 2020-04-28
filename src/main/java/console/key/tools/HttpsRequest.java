package console.key.tools;

import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpsRequest {

    private static final Logger logger = LoggerFactory.getLogger(HttpsRequest.class);

    private static CloseableHttpClient httpClient;

    static {
        try {
            SSLContext sslContext =
                    SSLContextBuilder.create()
                            .useProtocol(SSLConnectionSocketFactory.SSL)
                            .loadTrustMaterial((x, y) -> true)
                            .build();
            RequestConfig config =
                    RequestConfig.custom()
                            .setConnectTimeout(5000)
                            .setSocketTimeout(5000)
                            .setConnectionRequestTimeout(3000)
                            .build();
            httpClient =
                    HttpClientBuilder.create()
                            .setDefaultRequestConfig(config)
                            .setSSLContext(sslContext)
                            .setSSLHostnameVerifier((x, y) -> true)
                            .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String httpsPostWithForm(String url, Map<String, String> paramsMap) {
        logger.info("httpsPostWithForm start. url:{}", url);

        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse httpResponse = null;
        String result = null;

        try {
            List<BasicNameValuePair> pairList = new ArrayList<BasicNameValuePair>();
            for (String key : paramsMap.keySet()) {
                pairList.add(new BasicNameValuePair(key, paramsMap.get(key)));
                logger.info("httpPostWithForm paramsMap[{}]: {}", key, paramsMap.get(key));
            }
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairList, "utf-8");
            httpPost.setEntity(entity);

            httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            HttpEntity entityResponse = httpResponse.getEntity();
            if (entityResponse != null) {
                result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                logger.info("httpsPostWithForm statusCode: {}, result {}: ", statusCode, result);
            }
            EntityUtils.consume(httpResponse.getEntity());
            httpResponse.close();
            return result;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.warn("httpsPostWithForm warn message: {}, e: {}", e.getMessage(), e);
        } finally {
            httpPost.releaseConnection();
            if (httpResponse != null) {
                try {
                    EntityUtils.consume(httpResponse.getEntity());
                    httpResponse.close();
                } catch (IOException e) {
                    logger.warn("httpsPostWithForm warn message: {}, e: {}", e.getMessage(), e);
                }
            }
        }

        return result;
    }

    public static String httpsPostWithJson(String url, JSONObject jsonParam, String token) {
        logger.info(
                "httpsPostWithJson start. url:{},params:{}",
                url,
                JSONObject.toJSONString(jsonParam));

        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse httpResponse = null;
        String result = null;

        try {
            httpPost.addHeader("AuthorizationToken", "Token " + token);

            StringEntity entity = new StringEntity(jsonParam.toString(), "utf-8");
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            httpPost.setEntity(entity);

            httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            HttpEntity entityResponse = httpResponse.getEntity();
            if (entityResponse != null) {
                result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                logger.info("httpsPostWithJson statusCode: {}, result {}: ", statusCode, result);
            }
            EntityUtils.consume(httpResponse.getEntity());
            httpResponse.close();
            return result;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.warn("httpsPostWithJson warn message: {}, e: {}", e.getMessage(), e);
        } finally {
            httpPost.releaseConnection();
            if (httpResponse != null) {
                try {
                    EntityUtils.consume(httpResponse.getEntity());
                    httpResponse.close();
                } catch (IOException e) {
                    logger.warn("httpsPostWithJson warn message: {}, e: {}", e.getMessage(), e);
                }
            }
        }

        return result;
    }

    public static String httpsGet(String url, String token) {
        logger.info("httpsGet start. url: {}", url);

        HttpGet httpGet = null;
        CloseableHttpResponse httpResponse = null;
        String result = null;

        try {
            httpGet = new HttpGet(url);
            httpGet.addHeader("AuthorizationToken", "Token " + token);

            httpResponse = httpClient.execute(httpGet);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            HttpEntity entityResponse = httpResponse.getEntity();
            if (entityResponse != null) {
                result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                logger.info("httpsGet statusCode: {}, result {}: ", statusCode, result);
            }
            EntityUtils.consume(httpResponse.getEntity());
            httpResponse.close();
            return result;
        } catch (Exception e) {
            logger.warn("httpsGet warn message: {}, e: {}", e.getMessage(), e);
        } finally {
            httpGet.releaseConnection();
            if (httpResponse != null) {
                try {
                    EntityUtils.consume(httpResponse.getEntity());
                    httpResponse.close();
                } catch (IOException e) {
                    logger.warn("httpsGet warn message: {}, e: {}", e.getMessage(), e);
                }
            }
        }
        return result;
    }

    public static String httpsDelete(String url, String token) {
        logger.info("httpsDelete start. url: {}", url);

        HttpDelete httpDelete = null;
        CloseableHttpResponse httpResponse = null;
        String result = null;

        try {
            httpDelete = new HttpDelete(url);
            httpDelete.addHeader("AuthorizationToken", "Token " + token);

            httpResponse = httpClient.execute(httpDelete);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            HttpEntity entityResponse = httpResponse.getEntity();
            if (entityResponse != null) {
                result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                logger.info("httpsDelete statusCode: {}, result {}: ", statusCode, result);
            }
            EntityUtils.consume(httpResponse.getEntity());
            httpResponse.close();
            return result;
        } catch (Exception e) {
            logger.warn("httpsDelete warn message: {}, e: {}", e.getMessage(), e);
        } finally {
            httpDelete.releaseConnection();
            if (httpResponse != null) {
                try {
                    EntityUtils.consume(httpResponse.getEntity());
                    httpResponse.close();
                } catch (IOException e) {
                    logger.warn("httpsDelete warn message: {}, e: {}", e.getMessage(), e);
                }
            }
        }
        return result;
    }

    public static String httpsPut(String url, JSONObject jsonParam, String token) {
        logger.info("httpsPut start. url:{},params:{}", url, JSONObject.toJSONString(jsonParam));

        HttpPut httpPut = new HttpPut(url);
        CloseableHttpResponse httpResponse = null;
        String result = null;

        try {
            httpPut.addHeader("AuthorizationToken", "Token " + token);

            StringEntity entity = new StringEntity(jsonParam.toString(), "utf-8");
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            httpPut.setEntity(entity);

            httpResponse = httpClient.execute(httpPut);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            HttpEntity entityResponse = httpResponse.getEntity();
            if (entityResponse != null) {
                result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                logger.info("httpsPut statusCode: {}, result {}: ", statusCode, result);
            }
            EntityUtils.consume(httpResponse.getEntity());
            httpResponse.close();
            return result;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.warn("httpsPut warn message: {}, e: {}", e.getMessage(), e);
        } finally {
            httpPut.releaseConnection();
            if (httpResponse != null) {
                try {
                    EntityUtils.consume(httpResponse.getEntity());
                    httpResponse.close();
                } catch (IOException e) {
                    logger.warn("httpsPut warn message: {}, e: {}", e.getMessage(), e);
                }
            }
        }

        return result;
    }
}
