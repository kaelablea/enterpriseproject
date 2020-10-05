package person.detail;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import person.Person;
import person.fx.ViewSwitcher;
import person.fx.ViewType;

import java.net.URL;
import java.util.ResourceBundle;

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
    private DatePicker dateOfBirth;

    @FXML
    private  TextField age;

    @FXML
    private Button addButton;

    // this is our model
    private Person person;

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

            //Random random = new Random();
            person.setFirstName(firstName.getText());
            person.setLastName(lastName.getText());
            /*
            person.setDateOfBirth(dateOfBirth.getValue());
            person.setId(random.nextInt(1000-9999)+1000);
            person.setAge(2020-person.getDateOfBirth().getYear());
             */
            logger.info("CREATING " + person.getFirstName() + " " + person.getLastName());

        }
        else{

            person.setFirstName(firstName.getText());
            person.setLastName(lastName.getText());
            /*
            person.setDateOfBirth(dateOfBirth.getValue());
            person.setAge(2020-person.getDateOfBirth().getYear());

             */
            logger.info("UPDATING " + person.getFirstName() + " " + person.getLastName());
        }
        ViewSwitcher.getInstance().switchView(ViewType.PersonListView);
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        firstName.setText(person.getFirstName());
        lastName.setText(person.getLastName());
        //dateOfBirth.setValue(person.getDateOfBirth());
    }

}