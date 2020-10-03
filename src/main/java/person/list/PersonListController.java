package person.list;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import person.Person;
import person.fx.PersonParameters;
import person.fx.ViewSwitcher;
import person.fx.ViewType;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class PersonListController implements Initializable {
    private static final Logger logger = LogManager.getLogger();
    @FXML
    private Button addPerson, deletePerson;

    @FXML
    private ListView<Person> personList;

    private ObservableList<Person> people;

    public PersonListController() {
        people = FXCollections.observableArrayList();
        people.add(new Person(1237, "Nob", "Smith", LocalDate.of(1980, 1, 1)));
        people.add(new Person(1235, "Rob", "Smith", LocalDate.of(1980, 2, 2)));
        people.add(new Person(1236, "Tina", "Smith", LocalDate.of(1980, 3, 3)));

    }

    @FXML
    void handler(ActionEvent event) {
        if (event.getSource() == addPerson) {
            if(personList.getSelectionModel().getSelectedItem() != null){
                Person person = personList.getSelectionModel().getSelectedItem();
                PersonParameters.setPersonParm(person);
                logger.info("READING " + person.getFirstName() + " " + person.getLastName());
                ViewSwitcher.getInstance().switchView(ViewType.PersonDetailView);
            }
            else {
                PersonParameters.setPersonParm(new Person(0,"","",null));
                ViewSwitcher.getInstance().switchView(ViewType.PersonDetailView);
            }
        } else if (event.getSource() == deletePerson) {
            if(personList.getSelectionModel().isEmpty() == false) {
                int index = personList.getSelectionModel().getSelectedIndex();
                Person person = personList.getSelectionModel().getSelectedItem();
                logger.info("DELETING " + person.getFirstName() + " " + person.getLastName());
                personList.getItems().remove(index);
                people.remove(person);
            }
            else{
                logger.error("No Person Selected To Delete");
            }
        }
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        personList.setItems(people);

    }

}