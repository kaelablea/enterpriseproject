package person.fx.fxcontrollers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import person.fx.SessionParameters;
import person.fx.ViewSwitcher;
import person.fx.ViewType;
import person.gateway.PersonGateway;
import person.models.Audit;
import person.models.AuditTrail;
import person.models.Person;
import person.models.PersonException;

import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class PersonDetailController implements Initializable {
    private static Logger logger = LogManager.getLogger();
    //private final String sessionToken;


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

    @FXML
    private TableView auditTable;

    // this is our model
    private Person person;

    PersonGateway personGateway;

    public PersonDetailController(Person person) {
        this.person = person;
       //this.sessionToken = sessionToken;
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
        //UPDATE PERSON
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


            person.setFirstName(firstName.getText());
            person.setLastName(lastName.getText());
            person.setDateOfBirth(dob.getValue());
            changedValues.put("lastModified", person.getLastModified().toString());
            try{
                personGateway.updatePerson(changedValues, person.getId());
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "This person has been modified by someone else!");
                alert.showAndWait();
                throw new PersonException("Optimistic locking.");
            }
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
        TableColumn<Audit, String> chngMsg = new TableColumn<>("Change");
        TableColumn<Audit, String> chngBy= new TableColumn<>("Changed By");
        TableColumn<Audit, String> when = new TableColumn<>("When Occurred");

        if(person.getId() == 0){
            auditTable.getColumns().addAll(chngMsg, chngBy, when);
            auditTable.setPlaceholder(new Label("No audits to display."));
        } else{
            person = personGateway.getPerson(person.getId());
            ObservableList<Audit> data = FXCollections.observableArrayList();
            AuditTrail auditTrail = personGateway.getAuditTrail(person.getId());
            for(Audit a : auditTrail.getAudits()){
                data.add(a);
            }
            chngMsg.setCellValueFactory(new PropertyValueFactory<>("changeMsg"));
            chngBy.setCellValueFactory(new PropertyValueFactory<>("changedBy"));
            when.setCellValueFactory(new PropertyValueFactory<>("whenOccurred"));
            auditTable.setItems(data);
            auditTable.getColumns().setAll(chngMsg, chngBy, when);

        }

    }

}