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
import person.db.DBException;
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
            return new ResponseEntity<People>(HttpStatus.valueOf(401));
        }

        people = new PersonGateway(connection).fetchPeople();


        ResponseEntity<People> response = new ResponseEntity<People>(people, HttpStatus.valueOf(200));
        return response;
    }

    @PostMapping("/people")
    public ResponseEntity<String> insertPerson(@RequestHeader("Authorization") String token,@RequestBody Person newPerson){
        int valid = 0;
        try {
            valid =  new PersonGateway(connection).validateSessionToken(token);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return new ResponseEntity<String>("", HttpStatus.valueOf(401));
        }
        logger.info(newPerson.toString());
        try {
            PersonGateway.insertPerson(newPerson);
            ResponseEntity<String> response = new ResponseEntity("", HttpStatus.valueOf(200));
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("could not insert new person");
            ResponseEntity<String> response = new ResponseEntity<String>("Could not insert person", HttpStatus.valueOf(400));
            return  response;
        }


    }

    @PutMapping("/people/{id}")
    public static ResponseEntity<String> updatePerson(@RequestHeader(value = "Authorization") String token, @RequestBody Map<String,String> updateValues, @PathVariable("id") int id) {
        int valid = 0;
        try {
            valid =  new PersonGateway(connection).validateSessionToken(token);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return new ResponseEntity<String>("", HttpStatus.valueOf(401));
        }
        logger.info(updateValues.toString());
        try {
            PersonGateway.updatePerson(updateValues, id);
            ResponseEntity<String> response = new ResponseEntity<String>("", HttpStatus.valueOf(200));
            return response;
        } catch(DBException db){
            db.printStackTrace();
            return new ResponseEntity<String>("",HttpStatus.valueOf(404));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<String>("Error with update request", HttpStatus.valueOf(400));
        }
    }

    @DeleteMapping("/people/{id}")
    public static ResponseEntity<String> deletePerson(@RequestHeader("Authorization") String token, @PathVariable("id") int id){
        int valid = 0;
        try {
            valid=  new PersonGateway(connection).validateSessionToken(token);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return new ResponseEntity<String>("",HttpStatus.valueOf(401));
        }

        try {
            PersonGateway.deletePerson(id);
            ResponseEntity<String> response = new ResponseEntity<String>("id", HttpStatus.valueOf(200));
            return response;
        } catch (DBException db){
            db.printStackTrace();
            return new ResponseEntity<String>("",HttpStatus.valueOf(404));
        }
    }

    @GetMapping("/people/{id}")
    public static ResponseEntity<Person> fetchPerson(@RequestHeader("Authorization") String token, @PathVariable int id){
        int valid = 0;
        try {
            valid=  new PersonGateway(connection).validateSessionToken(token);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return new ResponseEntity("",HttpStatus.valueOf(401));
        }

        try{
            Person person = PersonGateway.getPerson(id);
            return new ResponseEntity<Person>(person, HttpStatus.valueOf(200));
        } catch (DBException db) {
            db.printStackTrace();
            return new ResponseEntity("", HttpStatus.valueOf(404));
        }
    }
}
