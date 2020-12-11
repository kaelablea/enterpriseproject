package person.backend.repocontrollers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import person.backend.repositories.AuditRepo;
import person.backend.repositories.PersonRepo;
import person.models.Audit;
import person.models.AuditTrail;
import person.models.People;
import person.models.Person;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

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
    public ResponseEntity<People> fetchPeople(@RequestHeader("Authorization") String token, @RequestParam(required = false) Map<String,String> X){
        int valid= 0;
        People people = new People();
        String lName =null;
        int page=0;
        if(X.containsKey("lastName")){
            lName = X.get("lastName");
        }
        if(X.containsKey("pageNum")){
            page= Integer.parseInt(X.get("pageNum"));
        }

        try {
            valid = new PersonRepo(connection).validateSessionToken(token);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return new ResponseEntity<People>(HttpStatus.valueOf(401));
        }
        if (page>0) {
            if(lName != null){
                people = new PersonRepo(connection).fetchPeople(page,lName);
            }
            else{
                people = new PersonRepo(connection).fetchPeople(page);
            }
        }
        else{
            people = new PersonRepo(connection).fetchPeople();
        }


        ResponseEntity<People> response = new ResponseEntity<People>(people, HttpStatus.valueOf(200));
        return response;
    }

    @PostMapping("/people")
    public ResponseEntity<Integer> insertPerson(@RequestHeader("Authorization") String token,@RequestBody Person newPerson){
        int valid = 0;
        try {
            valid =  new PersonRepo(connection).validateSessionToken(token);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return new ResponseEntity("", HttpStatus.valueOf(401));
        }
        logger.info(newPerson.toString());
        try {
            Audit audit = PersonRepo.insertPerson(newPerson, token);
            new AuditRepo(connection).addAudit(audit, token);
            ResponseEntity<Integer> response = new ResponseEntity(audit.getPersonId(), HttpStatus.valueOf(200));
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("could not insert new person");
            ResponseEntity response = new ResponseEntity<String>("Could not insert person", HttpStatus.valueOf(400));
            return response;
        }


    }

    @PutMapping("/people/{id}")
    public static ResponseEntity<String> updatePerson(@RequestHeader(value = "Authorization") String token, @RequestBody Map<String,String> updateValues, @PathVariable("id") int id) {
        int valid = 0;
        try {
            valid =  new PersonRepo(connection).validateSessionToken(token);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return new ResponseEntity<String>("", HttpStatus.valueOf(401));
        }
        logger.info(updateValues.toString());
        try {
            Person checkModified = PersonRepo.getPerson(id);
            if(!checkModified.getLastModified().toString().equals(updateValues.get("lastModified"))){
                return new ResponseEntity<String>("", HttpStatus.valueOf(201));
            }
            ArrayList<String> update =PersonRepo.updatePerson(updateValues, id);
            for(String msg: update){
                 Audit newAudit = new Audit(msg,id);
                 new AuditRepo(connection).addAudit(newAudit, token);
            }
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
            valid=  new PersonRepo(connection).validateSessionToken(token);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return new ResponseEntity<String>("",HttpStatus.valueOf(401));
        }

        try {
            PersonRepo.deletePerson(id);
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
            valid=  new PersonRepo(connection).validateSessionToken(token);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return new ResponseEntity("",HttpStatus.valueOf(401));
        }

        try{
            Person person = PersonRepo.getPerson(id);
            return new ResponseEntity<Person>(person, HttpStatus.valueOf(200));
        } catch (DBException db) {
            db.printStackTrace();
            return new ResponseEntity("", HttpStatus.valueOf(404));
        }
    }

    @GetMapping("people/{id}/audittrail")
    public static ResponseEntity<AuditTrail> fetchAuditTrail(@RequestHeader("Authorization") String token, @PathVariable int id){
        int valid = 0;
        try {
            valid=  new PersonRepo(connection).validateSessionToken(token);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return new ResponseEntity("",HttpStatus.valueOf(401));
        }

        try{
            AuditTrail auditTrail= new AuditTrail();
            auditTrail.setAudits(new AuditRepo(connection).getAuditTrail(id));
            //logger.info(auditTrail.getAudits().get(0).getWhenOccurred().toString());
            return new ResponseEntity<AuditTrail>(auditTrail, HttpStatus.valueOf(200));
        }catch (DBException db) {
            db.printStackTrace();
            return new ResponseEntity("", HttpStatus.valueOf(404));
        }

    }
}
