package person.gateway;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.cache.internal.StrategyCreatorRegionFactoryImpl;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import person.People;
import person.Person;
import person.PersonException;
import person.db.DBConnect;
import person.fx.SessionParameters;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class PersonController {
    private static final Logger logger = LogManager.getLogger();
    RestTemplate restTemplate = new RestTemplate();

    private static Connection connection;
    // create a connection on startup
    @PostConstruct
    public void startup() {
        try {
            logger.info("Trying to make db connection.");
            connection = DBConnect.connectToDB();
            logger.info("*** MySQL connection created");
        } catch (SQLException | IOException e) {
            logger.error("*** " + e);

            // TODO: find a better way to force shutdown on connect failure
            // System.exit(0);
        }
    }

    // close a connection on shutdown
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
    public ResponseEntity<People> fetchPeople(@RequestHeader("Authorization") String token){
        int valid= 0;
        People people = new People();

        try {
            valid = new PersonGateway(connection).validateSessionToken(token);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        if(valid != 200) {
            return new ResponseEntity<People>(HttpStatus.valueOf(401));
        }

        people = new PersonGateway(connection).fetchPeople();


        ResponseEntity<People> response = new ResponseEntity<People>(people, HttpStatus.valueOf(200));
        return response;
    }

    @PostMapping("/people")
    public ResponseEntity<Integer> insertPerson(@RequestHeader("Authorization") String token,@RequestBody Person newPerson){
        int valid = 0;
        try {
            valid =  new PersonGateway(connection).validateSessionToken(token);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        Person person = PersonGateway.insertPerson(newPerson);
        if(person.getId() <= 0) {
            logger.error("could not insert new person");
            ResponseEntity<String> response = new ResponseEntity<String>("", HttpStatus.valueOf(401));
        }

        ResponseEntity<Integer> response = new ResponseEntity(person.getId(), HttpStatus.valueOf(200));
        return response;
    }

    @PutMapping("/people/{id}")
    public static ResponseEntity<String> updatePerson(@RequestHeader(value = "Authorization") String token, Map<String,String> updateValues, @PathVariable("id") int id) {
        int valid = 0;
        try {
            valid =  new PersonGateway(connection).validateSessionToken(token);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        PersonGateway.updatePerson(updateValues, id);

        ResponseEntity<String> response = new ResponseEntity<String>("", HttpStatus.valueOf(200));

        return response;
    }

    @DeleteMapping("/people/{id}")
    public static ResponseEntity<String> deletePerson(@RequestHeader("Authorization") String token, @PathVariable("id") int id){
        int valid = 0;
        try {
            valid=  new PersonGateway(connection).validateSessionToken(token);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        PersonGateway.deletePerson(id);

        ResponseEntity<String> response = new ResponseEntity<String>("id",HttpStatus.valueOf(200));
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
}
