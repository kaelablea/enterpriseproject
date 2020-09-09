package person.detail;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import person.Person;
import person.fx.ViewSwitcher;
import person.fx.ViewType;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class PersonDetailController implements Initializable {
    private static Logger logger = LogManager.getLogger();

    @FXML
    private TextField personID;

    @FXML
    private TextField firstName;

    @FXML
    private TextField lastName;

    @FXML
    private DatePicker dateOfBirth;

    @FXML
    private  TextField age;


    // this is our model
    private Person person;

    public PersonDetailController(Person person) {
        this.person = person;
    }

    @FXML
    void handler(ActionEvent event) {
        if(person.getId() == 0) {
            Random random = new Random();

            person.setFirstName(firstName.getText());
            person.setLastName(lastName.getText());
            person.setDateOfBirth(dateOfBirth.getValue());
            person.setId(random.nextInt(1000-9999)+1000);
            person.setAge(2020-person.getDateOfBirth().getYear());
            logger.info("CREATING " + person.getFirstName() + " " + person.getLastName());
        }
        else{
            person.setFirstName(firstName.getText());
            person.setLastName(lastName.getText());
            person.setDateOfBirth(dateOfBirth.getValue());
            person.setAge(2020-person.getDateOfBirth().getYear());
            logger.info("UPDATING " + person.getFirstName() + " " + person.getLastName());
        }
        ViewSwitcher.getInstance().switchView(ViewType.PersonListView);
    }


    public void initialize(URL arg0, ResourceBundle arg1) {
        firstName.setText(person.getFirstName());
        lastName.setText(person.getLastName());
        personID.setText(String.valueOf(person.getId()));
        dateOfBirth.setValue(person.getDateOfBirth());
    }

}