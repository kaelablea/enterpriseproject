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
import org.springframework.web.bind.annotation.RestController;
import person.db.DBConnect;
import person.utility.HashUtil;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.*;

@RestController
public class SessionGateway {
    public static Connection connection;
    private static Logger logger = LogManager.getLogger();

    public SessionGateway(){
    }

    @PostConstruct
    public void startUp(){
        try {
            connection = DBConnect.connectToDB();
            logger.info("Session MySQL Connection Created!");
        } catch(SQLException | IOException e) {
            logger.error(e);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            connection.close();
            logger.info("*** MySQL connection closed");
        } catch (SQLException e) {
            logger.error("*** " + e);
        }
    }

    //This function is waaaaaaaaaaaaaaaaaaay too hefty
    //definitely need to refactor
    public static String authenticate(String text, String text1) throws IOException {
        //for now just authenticate ragnar and flapjack
        final String WS_URL = "http://localhost:8080";
        final Logger logger = LogManager.getLogger();
        String sessionToken = null;
        CloseableHttpResponse response = null;
        CloseableHttpClient httpclient = null;
        try {

            PreparedStatement st = null;
            ResultSet success = null;
            Time time = null;
            try {
                st = connection.prepareStatement("select id from user where (username = ? and password =?) ", PreparedStatement.RETURN_GENERATED_KEYS);
                st.executeUpdate();
                success = st.getGeneratedKeys();
                success.first();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } finally {
                st.close();
            }
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
        } catch (IOException | SQLException e) {
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
            JSONObject responseJSON = new JSONObject(responseString);
            sessionToken = responseJSON.getString("session_id");
            logger.info("Session token: " + sessionToken);
            return sessionToken;
        } catch (Exception e) {
            logger.error("could not get session token: " + e.getMessage());
            httpclient.close();
            throw new IOException();
        }
    }

    //Create session token with username and timestamp generated by db
    public static String createSessionToken(String text){
        PreparedStatement st = null;
        ResultSet timeStamp = null;
        Time time = null;
        String token = null;
        try{
            st = connection.prepareStatement("insert into session", PreparedStatement.RETURN_GENERATED_KEYS);
            st.executeUpdate();
            timeStamp = st.getGeneratedKeys();
            timeStamp.first();
            time = timeStamp.getTime("time");
            token = HashUtil.getCryptoHash(text + time.toString(), "SHA-256");

            st = connection.prepareStatement("update session set token = ? where time = ?", PreparedStatement.RETURN_GENERATED_KEYS);
            st.setString(1, token);
            st.setTime(2, time);
            st.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            //Do I even need this?
            // i assume this is redundant because of the postconstruct and predestroy
            try {
                if(st != null)

                    st.close();
                    return token;
            } catch (SQLException e2) {
                e2.printStackTrace();
                return "error";
            }
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
