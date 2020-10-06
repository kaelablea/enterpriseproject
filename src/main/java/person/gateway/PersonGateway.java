package person.gateway;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import person.Person;
import person.PersonException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;

import static com.mysql.cj.conf.PropertyKey.logger;

public class PersonGateway {
    private static String wsURL;
    private static String sessionId;
    private static Logger logger = LogManager.getLogger();

    public PersonGateway(String url, String sessionId) {
        this.sessionId = sessionId;
        this.wsURL = url;
    }

    public ArrayList<Person> getPeople() {
        ArrayList<Person> people = new ArrayList<Person>();

        try {
            HttpGet request = new HttpGet(wsURL + "/people");
            // specify Authorization header
            request.setHeader("Authorization", sessionId);

            String response = waitForResponseAsString(request);

            for(Object obj : new JSONArray(response)) {
                JSONObject jsonObject = (JSONObject) obj;
                people.add(new Person(jsonObject.getInt("id"), jsonObject.getString("firstName"), jsonObject.getString("lastName"), LocalDate.parse(jsonObject.getString("dateOfBirth"))));
            }
        } catch (Exception e) {
            throw new PersonException(e);
        }

        return people;
    }

    public int insertPerson(Person newPerson){
        try{
            HttpPost request = new HttpPost(wsURL+"/people");
            // specify Authorization header
            request.setHeader("Authorization", sessionId);

            JSONObject person = new JSONObject();
            person.put("firstName", newPerson.getFirstName());
            person.put("lastName", newPerson.getLastName());
            person.put("dateOfBirth", newPerson.getDateOfBirth().toString());

            String personString = person.toString();
            StringEntity reqEntity = new StringEntity(personString);
            request.setEntity(reqEntity);
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");

            String response = waitForResponseAsString(request);
            JSONObject jsonObject = new JSONObject(response);
            int id = jsonObject.getInt("id");

            return id;
        } catch (Exception e) {
            throw new PersonException(e);
        }
    }

    public static String updatePerson(ArrayList<String> updateValues, int id) {
        String response = null;
        try {
            HttpPut request = new HttpPut(wsURL + "/people/" + String.valueOf(id));
            // specify Authorization header
            request.setHeader("Authorization", sessionId);

            JSONObject person = new JSONObject();

            if (updateValues.get(0) != null) {
                person.put("firstName", updateValues.get(0));
            }
            if (updateValues.get(1) != null) {
                person.put("lastName", updateValues.get(1));
            }
            if (updateValues.get(2) != null) {
                person.put("dateOfBirth", updateValues.get(2));
            }

            String personString = person.toString();
            StringEntity reqEntity = new StringEntity(personString);
            request.setEntity(reqEntity);
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");

            response = waitForResponseAsString(request);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }


    public static String deletePerson(int id){
        HttpDelete request = new HttpDelete(wsURL + "/people/" + String.valueOf(id));
        // specify Authorization header
         request.setHeader("Authorization", sessionId);
        String response = null;
        try {
            response = waitForResponseAsString(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
     }

    private static String waitForResponseAsString(HttpRequestBase request) throws IOException {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;

        try {

            httpclient = HttpClients.createDefault();
            logger.info(request.toString());
            response = httpclient.execute(request);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    // success
                    break;
                case 401:
                    // bad session token
                    throw new PersonException("401");
                default:
                    // something weird happened
                    throw new PersonException("Non-200 status code returned: " + response.getStatusLine());
            }

            return parseResponseToString(response);

        } catch(Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            throw new PersonException(e);
        } finally {
            if(response != null)
                response.close();
            if(httpclient != null)
                httpclient.close();
        }
    }

    private static String parseResponseToString(CloseableHttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        // use org.apache.http.util.EntityUtils to read json as string
        return EntityUtils.toString(entity, StandardCharsets.UTF_8);
    }

}
