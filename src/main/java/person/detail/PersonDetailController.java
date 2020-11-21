package person.detail;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import person.Person;
import person.PersonException;
import person.db.DBConnect;
import person.fx.SessionParameters;
import person.fx.ViewSwitcher;
import person.fx.ViewType;
import person.gateway.PersonController;
import person.gateway.PersonGateway;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class PersonDetailController implements Initializable {
    private static Logger logger = LogManager.getLogger();
    RestTemplate restTemplate= new RestTemplate();

    @FXML
    private TextField personID;

    @FXML
    private TextField firstName;

    @FXML
    private TextField lastName;

    @FXML
    private DatePicker dob;

    @FXML
    private  TextField age;

    @FXML
    private Button addButton;

    // this is our model
    private Person person;

    PersonController personController;

    public PersonDetailController(Person person) {
        this.person = person;
    }



    @FXML
    void handler(ActionEvent event) {

        if (firstName.getText() == null || lastName.getText() == null){
            ViewSwitcher.getInstance().switchView(ViewType.PersonListView);
        }
        else if(person.getId() == 0) {

            if(firstName.getText().isEmpty() || firstName.getText().length() > 100){
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid First Name, try again!");
                alert.showAndWait();
                throw new PersonException("Bad name.");
            }
            else{
                person.setFirstName(firstName.getText());
            }
            if(lastName.getText().isEmpty() || lastName.getText().length() > 100){
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Last Name, try again!");
                alert.showAndWait();
                throw new PersonException("Bad name.");
            }
            else{
                person.setLastName(lastName.getText());
            }

            if(dob.getValue().compareTo(LocalDate.now()) <= 0) {
                person.setDateOfBirth(dob.getValue());
            }
            else{
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid date, try again!");
                alert.showAndWait();
                throw new PersonException("Bad date.");
            }
            HttpHeaders  headers = new HttpHeaders();
            headers.set("Authorization", SessionParameters.getSessionToken());
            HttpEntity<Person> request = new HttpEntity<>(person,headers);

            ResponseEntity<Integer> response = restTemplate.exchange("http://localhost:8080/people", HttpMethod.POST,request, Integer.class);

            person.setId(response.getBody());

            logger.info("CREATING " + person.getFirstName() + " " + person.getLastName());

        }
        else{
            Map<String,String> changedValues = new HashMap<String,String>();

            //not sure is setting index to null is necessary
            if(!firstName.getText().equals(person.getFirstName())){
                if(lastName.getText().isEmpty() || lastName.getText().length() > 100){
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Last Name, try again!");
                    alert.showAndWait();
                    throw new PersonException("Bad name.");
                }
                else {
                    changedValues.put("firstName", firstName.getText());
                }
            }

            if(!lastName.getText().equals(person.getLastName())){
                if(lastName.getText().isEmpty() || lastName.getText().length() > 100){
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Last Name, try again!");
                    alert.showAndWait();
                    throw new PersonException("Bad name.");
                }
                else {
                    changedValues.put("lastName", lastName.getText());
                }
            }

            if(!dob.getValue().equals(person.getDateOfBirth())){
                if(dob.getValue().compareTo(LocalDate.now()) <= 0) {
                    changedValues.put("dateOfBirth", dob.getValue().toString());
                }
                else{
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid date, try again!");
                    alert.showAndWait();
                    throw new PersonException("Bad date.");
                }
            }
            //WOOOOOOOOOOOOOOOOOOOOOOORK
            logger.info(changedValues.toString());
            String uri = "http://localhost:8080/people/" + person.getId();
            person.setFirstName(firstName.getText());
            person.setLastName(lastName.getText());
            person.setDateOfBirth(dob.getValue());
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", SessionParameters.getSessionToken());
            HttpEntity<Map<String,String>> request = new HttpEntity(changedValues,headers);
            logger.info(request.toString());
            restTemplate.exchange(uri, HttpMethod.PUT, request, Person.class);
            logger.info("UPDATING " + person.getFirstName() + " " + person.getLastName());
        }

        ViewSwitcher.getInstance().switchView(ViewType.PersonListView);
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        firstName.setText(person.getFirstName());
        lastName.setText(person.getLastName());
        dob.setValue(person.getDateOfBirth());

    }

}