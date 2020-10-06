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
import person.Person;
import person.PersonException;
import person.fx.SessionParameters;
import person.fx.ViewSwitcher;
import person.fx.ViewType;
import person.gateway.PersonGateway;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;

public class PersonDetailController implements Initializable {
    private static Logger logger = LogManager.getLogger();
    private final String sessionToken;


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

    PersonGateway personGateway;

    public PersonDetailController(Person person, String sessionToken) {
        this.person = person;
        this.sessionToken = sessionToken;
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
            person.setId(personGateway.insertPerson(person));

            logger.info("CREATING " + person.getFirstName() + " " + person.getLastName());

        }
        else{
            ArrayList<String> changedValues = new ArrayList<String>();

            //not sure is setting index to null is necessary
            if(firstName.getText() != person.getFirstName()){
                if(lastName.getText().isEmpty() || lastName.getText().length() > 100){
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Last Name, try again!");
                    alert.showAndWait();
                    throw new PersonException("Bad name.");
                }
                else {
                    changedValues.add(0, firstName.getText());
                }
            }
            else{
                changedValues.add(0, null);
            }
            if(lastName.getText() != person.getLastName()){
                if(lastName.getText().isEmpty() || lastName.getText().length() > 100){
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Last Name, try again!");
                    alert.showAndWait();
                    throw new PersonException("Bad name.");
                }
                else {
                    changedValues.add(1, lastName.getText());
                }
            }
            else{
                changedValues.add(1, null);
            }
            if(dob.getValue() != person.getDateOfBirth()){
                if(dob.getValue().compareTo(LocalDate.now()) <= 0) {
                    changedValues.add(2, dob.getValue().toString());
                }
                else{
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid date, try again!");
                    alert.showAndWait();
                    throw new PersonException("Bad date.");
                }
            }
            else{
                changedValues.add(2, null);
            }

            person.setFirstName(firstName.getText());
            person.setLastName(lastName.getText());
            person.setDateOfBirth(dob.getValue());
            personGateway.updatePerson(changedValues, person.getId());
            logger.info("UPDATING " + person.getFirstName() + " " + person.getLastName());
        }
        ViewSwitcher.getInstance().switchView(ViewType.PersonListView);
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        firstName.setText(person.getFirstName());
        lastName.setText(person.getLastName());
        dob.setValue(person.getDateOfBirth());
        personGateway = new PersonGateway("http://localhost:8080",SessionParameters.getSessionToken());
    }

}