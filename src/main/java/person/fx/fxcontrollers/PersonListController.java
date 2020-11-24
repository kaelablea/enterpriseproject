package person.fx.fxcontrollers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import person.models.Person;
import person.fx.PersonParameters;
import person.fx.SessionParameters;
import person.fx.ViewSwitcher;
import person.fx.ViewType;
import person.gateway.PersonGateway;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class PersonListController implements Initializable {
    private static final Logger logger = LogManager.getLogger();
    PersonGateway personGateway;
    @FXML
    private Button addPerson, deletePerson, updatePerson;

    @FXML
    private ListView<Person> personList;

    private ObservableList<Person> people;

    public PersonListController() {
    }

    @FXML
    void handler(ActionEvent event) {
        if (event.getSource() == addPerson) {
            PersonParameters.setPersonParm(new Person(0,"","",LocalDate.now()));
            ViewSwitcher.getInstance().switchView(ViewType.PersonDetailView);
        } else if (event.getSource() == deletePerson) {
            if(personList.getSelectionModel().isEmpty() == false) {
                int index = personList.getSelectionModel().getSelectedIndex();
                Person person = personList.getSelectionModel().getSelectedItem();
                logger.info("DELETING " + person.getFirstName() + " " + person.getLastName());
                personList.getItems().remove(index);
                people.remove(person);
                personGateway.deletePerson(person.getId());
            }
            else{
                logger.error("No Person Selected To Delete");
            }
        } else if (event.getSource() == updatePerson){
            if(personList.getSelectionModel().getSelectedItem() != null){
                Person person = personList.getSelectionModel().getSelectedItem();
                PersonParameters.setPersonParm(person);
                logger.info("READING " + person.getFirstName() + " " + person.getLastName());
                ViewSwitcher.getInstance().switchView(ViewType.PersonDetailView);

            }
            else{
                logger.error("No Person Selected To Update");
            }
        }

    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        people = FXCollections.observableArrayList();
        //Should I put PersonGateway in personParam?
        //Would be cleaner(?) and still could make more than one if necessary
        personGateway = new PersonGateway("http://localhost:8080", SessionParameters.getSessionToken());
        ArrayList<Person> listOfPeople = personGateway.getPeople();
        logger.info("Retrieved list of people.");
        for(Person i :  listOfPeople){
            people.add(i);
        }
        personList.setItems(people);
        personList.setCellFactory(new Callback<ListView<Person>, ListCell<Person>>() {
            @Override
            public ListCell<Person> call(ListView<Person> p) {

                ListCell<Person> cell = new ListCell<Person>() {

                    @Override
                    protected void updateItem(Person t, boolean bln) {
                        super.updateItem(t, bln);
                        if (t != null) {
                            setText(t.getId() + " : " + t.getFirstName() + " " + t.getLastName());
                        }
                    }
                };
                return cell;
            }
        });
        logger.info(personList.toString());
    }

}