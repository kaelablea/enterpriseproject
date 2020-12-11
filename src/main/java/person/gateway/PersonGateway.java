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
import person.models.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class PersonGateway {
    private static String wsURL;
    private static String sessionId;
    private static Logger logger = LogManager.getLogger();

    public PersonGateway(String url, String sessionId) {
        this.sessionId = sessionId;
        this.wsURL = url;
    }

    public Person getPerson(int id){
        Person person = new Person();
        try {
            HttpGet request = new HttpGet(wsURL + "/people/" + String.valueOf(id));
            request.setHeader("Authorization", sessionId);
            String response = waitForResponseAsString(request);
            JSONObject obj = new JSONObject(response);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            person.setId(obj.getInt("id"));
            person.setFirstName(obj.getString("firstName"));
            person.setLastName(obj.getString("lastName"));
            person.setDateOfBirth(LocalDate.parse(obj.getString("dateOfBirth")));
            Date ts = Timestamp.valueOf(obj.getString("lastModified"));
            person.setLastModified((Timestamp) ts);
            return person;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return person;
    }

    public People getPeople() {
        People people = new People();
        ArrayList<Person> ppl = people.getPeople();

        try {
            HttpGet request = new HttpGet(wsURL + "/people");
            // specify Authorization header
            request.setHeader("Authorization", sessionId);

            String response = waitForResponseAsString(request);
            JSONObject arr = new JSONObject(response);
            JSONArray pArray = arr.getJSONArray("people");
            //People pList = new People(pArray.toList());
            for( Object obj : pArray) {
                JSONObject jsonObject = (JSONObject) obj;
                ppl.add(new Person(jsonObject.getInt("id"), jsonObject.getString("firstName"), jsonObject.getString("lastName"), LocalDate.parse(jsonObject.getString("dateOfBirth"))));

            }
            people.setPeople(ppl);
            if(arr.has("totalRecords")){
                people.setTotalRecords(arr.getInt("totalRecords"));
            }
            if(arr.has("pageSize")){
                people.setPageSize(arr.getInt("pageSize"));
            }
            if(arr.has("currentPage")){
                people.setCurrentPage(arr.getInt("currentPage"));
            }
        } catch (Exception e) {
            throw new PersonException(e);
        }

        return people;
    }
    public People getPeople(int page) {
        People people = new People();
        ArrayList<Person> ppl = people.getPeople();

        try {
            HttpGet request = new HttpGet(wsURL + "/people?pageNum=" + page );
            // specify Authorization header
            request.setHeader("Authorization", sessionId);

            String response = waitForResponseAsString(request);
            JSONObject arr = new JSONObject(response);
            JSONArray pArray = arr.getJSONArray("people");
            //People pList = new People(pArray.toList());
            for( Object obj : pArray) {
                JSONObject jsonObject = (JSONObject) obj;
                ppl.add(new Person(jsonObject.getInt("id"), jsonObject.getString("firstName"), jsonObject.getString("lastName"), LocalDate.parse(jsonObject.getString("dateOfBirth"))));

            }
            people.setPeople(ppl);
            if(arr.has("totalRecords")){
                people.setTotalRecords(arr.getInt("totalRecords"));
            }
            if(arr.has("pageSize")){
                people.setPageSize(arr.getInt("pageSize"));
            }
            if(arr.has("currentPage")){
                people.setCurrentPage(arr.getInt("currentPage"));
            }
        } catch (Exception e) {
            throw new PersonException(e);
        }

        return people;
    }

    // for searching for people
    public People getPeople(String lastName, int page) {
        People people = new People();
        ArrayList<Person> ppl = new ArrayList<>();

        try {
            HttpGet request = new HttpGet(wsURL + "/people?pageNum="+String.valueOf(page)+"&lastName="+lastName);
            // specify Authorization header
            request.setHeader("Authorization", sessionId);

            String response = waitForResponseAsString(request);
            JSONObject arr = new JSONObject(response);
            JSONArray pArray = arr.getJSONArray("people");
            //People pList = new People(pArray.toList());
            for( Object obj : pArray) {
                JSONObject jsonObject = (JSONObject) obj;
                ppl.add(new Person(jsonObject.getInt("id"), jsonObject.getString("firstName"), jsonObject.getString("lastName"), LocalDate.parse(jsonObject.getString("dateOfBirth"))));

            }
            people.setPeople(ppl);
            if(arr.has("totalRecords")){
                people.setTotalRecords(arr.getInt("totalRecords"));
            }
            if(arr.has("pageSize")){
                people.setPageSize(arr.getInt("pageSize"));
            }
            if(arr.has("currentPage")){
                people.setCurrentPage(arr.getInt("currentPage"));
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
            //JSONObject jsonObject = new JSONObject(response);
            int id = Integer.parseInt(response);

            return id;
        } catch (Exception e) {
            throw new PersonException(e);
        }
    }

    public static String updatePerson(Map<String, String> updateValues, int id) {
        String response = null;
        try {
            HttpPut request = new HttpPut(wsURL + "/people/" + String.valueOf(id));
            // specify Authorization header
            request.setHeader("Authorization", sessionId);

            JSONObject person = new JSONObject();

            if (updateValues.containsKey("firstName")) {
                person.put("firstName", updateValues.get("firstName"));
            }
            if (updateValues.containsKey("lastName")) {
                person.put("lastName", updateValues.get("lastName"));
            }
            if (updateValues.containsKey("dateOfBirth")) {
                person.put("dateOfBirth", updateValues.get("dateOfBirth"));
            }
            person.put("lastModified", updateValues.get("lastModified"));
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

    public static AuditTrail getAuditTrail(int id){
        HttpGet request = new HttpGet(wsURL + "/people/" + String.valueOf(id) + "/audittrail");
        request.setHeader("Authorization", sessionId);
        String response = null;
        try {
            response = waitForResponseAsString(request);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            AuditTrail auditTrail = new AuditTrail();
            ArrayList<Audit> list = new ArrayList<Audit>();
            JSONObject arr = new JSONObject(response);
            JSONArray aArray = arr.getJSONArray("audits");
            for( Object obj : aArray) {
                JSONObject jsonObject = (JSONObject) obj;
                logger.info(jsonObject.getString("whenOccurred"));
                Timestamp ts = Timestamp.valueOf(jsonObject.getString("whenOccurred"));
                list.add(new Audit(jsonObject.getInt("id"), jsonObject.getString("changeMsg"), jsonObject.getInt("changedBy"), jsonObject.getInt("personId"), ts));
            }
            auditTrail.setAudits(list);
            return auditTrail;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

        String ent = EntityUtils.toString(entity, StandardCharsets.UTF_8);
        // use org.apache.http.util.EntityUtils to read json as string
        return ent;
    }

}
