package person.list;

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
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import person.People;
import person.Person;
import person.fx.PersonParameters;
import person.fx.SessionParameters;
import person.fx.ViewSwitcher;
import person.fx.ViewType;
import person.gateway.PersonController;
import person.gateway.PersonGateway;

import java.net.URL;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;


public class PersonListController implements Initializable {
    private static final Logger logger = LogManager.getLogger();
    RestTemplate restTemplate= new RestTemplate();



    @FXML
    private Button addPerson, deletePerson, updatePerson;

    @FXML
    private ListView<Person> personList;

    private ObservableList<Person> people;

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
                String uri = "/people/" + person.getId();
                HttpHeaders header = new HttpHeaders();
                header.set("Authorization",SessionParameters.getSessionToken());
                HttpEntity auth = new HttpEntity(header);
                restTemplate.exchange(uri, HttpMethod.DELETE, auth, String.class);

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
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", SessionParameters.getSessionToken());
        HttpEntity auth = new HttpEntity<>(headers);
        ResponseEntity<People> response = restTemplate.exchange("http://localhost:8080/people", HttpMethod.GET, auth, People.class);
        logger.info("Retrieved list of people." + response.getBody());
        People list = response.getBody();
        ArrayList<Person> personArrayList = list.getPeople();
        for (Person p : personArrayList) {
            logger.info("Person: " + p.toString());
            people.add(p);
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