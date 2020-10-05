package person.gateway;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import person.Person;
import person.PersonException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;

public class PersonGateway {
    private static String wsURL;
    private static String sessionId;

    public PersonGateway(String url, String sessionId) {
        this.sessionId = sessionId;
        this.wsURL = url;
    }

    public ArrayList<Person> getPeople() {
        ArrayList<Person> people = new ArrayList<Person>();

        try {
            // we know this is a GET request so create a get request and pass it to getResponseAsString
            // build the request
            HttpGet request = new HttpGet(wsURL);
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

    public String insertPerson(Person newPerson){
        try{
            HttpGet request = new HttpGet(wsURL);
            // specify Authorization header
            request.setHeader("Authorization", sessionId);

            String response = waitForResponseAsString(request);
            return response;
        } catch (Exception e) {
            throw new PersonException(e);
        }
    }

    public static String updatePerson(Person existingPerson){
        HttpGet request = new HttpGet(wsURL + "/" + String.valueOf(existingPerson.getId()));
        // specify Authorization header
        request.setHeader("Authorization", sessionId);
        //Need to send request
        String response = null;
        try {
            response = waitForResponseAsString(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
    public static String deletePerson(int id){
        HttpGet request = new HttpGet(wsURL + "/" + String.valueOf(id));
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
