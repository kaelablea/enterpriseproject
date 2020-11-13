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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import person.Person;
import person.PersonException;
import person.db.DBConnect;
import person.fx.SessionParameters;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static com.mysql.cj.conf.PropertyKey.logger;
@RestController
public class PersonGateway {
    private static String wsURL;
    private static String sessionId;
    private static Connection connection;
    private static Logger logger = LogManager.getLogger();


    public PersonGateway(String url, String sessionId, Connection connection) {
        this.sessionId = sessionId;
        this.wsURL = url;
        this.connection = connection;
    }

    @PostConstruct
    public void startUp(){
        try {
            connection = DBConnect.connectToDB();
            logger.info("Person MySQL Connection Created!");
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

    @GetMapping("/people")
    public ArrayList<Person> getPeople() {
        ResponseEntity<String> token = null;
        try {
            token =  PersonGateway.validateSessionToken();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        ArrayList<Person> people = new ArrayList<Person>();

        PreparedStatement st = null;
        ResultSet sqlPeople = null;

        try {
            st = connection.prepareStatement("select * from person", PreparedStatement.RETURN_GENERATED_KEYS);
            st.executeUpdate();
            sqlPeople = st.getResultSet();
            while(sqlPeople.next()){
                int id = sqlPeople.getInt("id");
                String firstName = sqlPeople.getString("first_name");
                String lastname= sqlPeople.getString("last_name");
                LocalDate dob = sqlPeople.getDate("dob").toLocalDate();
                Person nPerson = new Person(id,firstName, lastname,dob);
                people.add(nPerson);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

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

    @PostMapping("/person")
    public int insertPerson( Person newPerson){
        ResponseEntity<String> token = null;
        try {
            token =  PersonGateway.validateSessionToken();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        PreparedStatement st = null;

        try {
            st = connection.prepareStatement("insert into person values(first_name = ?, last_name = ?, dob = ?)", PreparedStatement.RETURN_GENERATED_KEYS);
            st.setString(1,newPerson.getFirstName());
            st.setString(2, newPerson.getLastName());
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
            java.util.Date utilDate = format.parse(newPerson.getDateOfBirth().toString());
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
            st.setDate(3, sqlDate);
            st.executeUpdate();
        } catch (SQLException | ParseException throwables) {
            throwables.printStackTrace();
        }
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

    @PutMapping("/people/{id}")
    public static String updatePerson( ArrayList<String> updateValues,Person upPerson, @PathVariable("id") int id) {
        ResponseEntity<String> token = null;
        String response = null;
        try {
            token =  PersonGateway.validateSessionToken();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        PreparedStatement st = null;
        ResultSet success = null;
        Time time = null;
        try {
            st = connection.prepareStatement("update person set (first_name = ?, last_name=?, dob = ?) where id = ?", PreparedStatement.RETURN_GENERATED_KEYS);
            st.setInt(4,id);
            if (updateValues.get(0) != null) {
                st.setString(1, updateValues.get(0));
            }else{
                st.setString(1, upPerson.getFirstName());
            }
            if (updateValues.get(1) != null) {
                st.setString(2, updateValues.get(1));
            }else{
                st.setString(2, upPerson.getLastName());
            }
            if (updateValues.get(2) != null) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
                java.util.Date utilDate = format.parse(updateValues.get(2));
                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
                st.setDate(3, sqlDate);
            }else{
                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
                java.util.Date utilDate = format.parse(upPerson.getDateOfBirth().toString());
                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
                st.setDate(3, sqlDate);
            }
            st.executeUpdate();

        } catch (SQLException | ParseException throwables) {
            throwables.printStackTrace();
            return "error";
        }

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


    @DeleteMapping("/people/{id}")
    public static String deletePerson( @PathVariable("id") int id){
        ResponseEntity<String> token = null;
        try {
            token =  PersonGateway.validateSessionToken();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        PreparedStatement st = null;
        ResultSet success = null;
        Time time = null;
        try {
            st = connection.prepareStatement("delete * from person where id = ?", PreparedStatement.RETURN_GENERATED_KEYS);
            st.setInt(1,id);
            st.executeUpdate();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return "error";
        }

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

    private static String parseResponseToString(CloseableHttpResponse response) {
        HttpEntity entity = response.getEntity();
        // use org.apache.http.util.EntityUtils to read json as string
        try {
            return EntityUtils.toString(entity, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //verify session token
    public static ResponseEntity<String> validateSessionToken() throws SQLException {
        String sessionToken = SessionParameters.getSessionToken();

        PreparedStatement st = null;
        ResultSet success = null;
        Time time = null;
        try {
            st = connection.prepareStatement("select * from session where token = ?", PreparedStatement.RETURN_GENERATED_KEYS);
            st.setString(1,sessionToken);
            st.executeUpdate();
            return new ResponseEntity<String>("", HttpStatus.valueOf(200));

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return new ResponseEntity<String>("", HttpStatus.valueOf(401));
        }
    }

}
