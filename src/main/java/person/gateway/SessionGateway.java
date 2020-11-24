package person.gateway;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class SessionGateway {
    public static String authenticate(String text, String text1) throws IOException {
        //for now just authenticate ragnar and flapjack
        final String WS_URL = "http://localhost:8080";
        final Logger logger = LogManager.getLogger();
        String sessionToken = null;
        CloseableHttpResponse response = null;
        CloseableHttpClient httpclient = null;
        try {
            httpclient = HttpClients.createDefault();

            // assemble credentials into a JSON encoded string
            JSONObject credentials = new JSONObject();
            credentials.put("username", text);
            credentials.put("password", text1);
            String credentialsString = credentials.toString();
            logger.info("credentials: " + credentialsString);

            // build the request
            HttpPost loginRequest = new HttpPost(WS_URL + "/login");
            // put credentials string into request body (as raw json)
            // this requires setting it up as a request entity where we can describe what the text is
            StringEntity reqEntity = new StringEntity(credentialsString);
            loginRequest.setEntity(reqEntity);
            loginRequest.setHeader("Accept", "application/json");
            loginRequest.setHeader("Content-type", "application/json");

            response = httpclient.execute(loginRequest);

            // a special response for invalid credentials
            if (response.getStatusLine().getStatusCode() == 401) {
                logger.error("Invalid login.json");
                httpclient.close();
                throw new IOException();
            }
            if (response.getStatusLine().getStatusCode() != 200) {
                logger.error("Non-200 status code returned: " + response.getStatusLine());
                httpclient.close();
                throw new IOException();
            }
            logger.info("Valid login.json.");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String responseString = null;
        try {
            responseString = getResponseAsString(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Login response as a string: " + responseString);

        try {

            logger.info("Session token: " + sessionToken);
            return responseString;
        } catch (Exception e) {
            logger.error("could not get session token: " + e.getMessage());
            httpclient.close();
            throw new IOException();
        }
    }

    public static String getResponseAsString(CloseableHttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        // use org.apache.http.util.EntityUtils to read json as string
        String strResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);
        EntityUtils.consume(entity);

        return strResponse;
    }
}
