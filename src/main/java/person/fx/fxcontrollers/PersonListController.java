package person.fx.fxcontrollers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import person.fx.PersonParameters;
import person.fx.SessionParameters;
import person.fx.ViewSwitcher;
import person.fx.ViewType;
import person.gateway.PersonGateway;
import person.models.People;
import person.models.Person;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class PersonListController implements Initializable {
    private static final Logger logger = LogManager.getLogger();
    PersonGateway personGateway;
    int getPage;
    int currentPage;
    @FXML
    private Button addPerson, deletePerson, updatePerson, search, firstPage, lastPage, previousPage, nextPage;

    @FXML
    private Label startRecord, lastRecord, totalRecords;

    @FXML
    private TextField lastName;

    @FXML
    private ListView<Person> personList;

    private ObservableList<Person> people;

    public PersonListController() {
    }

    @FXML
    void handler(ActionEvent event) {
        if (event.getSource() == addPerson) {
            PersonParameters.setPersonParm(new Person());
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

        } else if(event.getSource()== search){
            logger.info("Searching for last name: " + lastName.getText() );
            people.clear();
            getPage = 1;
            previousPage.setDisable(true);
            People ppl =personGateway.getPeople(lastName.getText(), getPage );
            ArrayList<Person> listOfPeople = ppl.getPeople();
            logger.info("Retrieved list of people.");
            for(Person i :  listOfPeople){
                people.add(i);
            }
            personList.getItems().setAll(people);
            personList.getItems().addAll(people);
            //personList.setItems(people);

            if(ppl.getTotalRecords() == 0){
                startRecord.setText(String.valueOf(0));
                previousPage.setDisable(true);
                nextPage.setDisable(true);
                firstPage.setDisable(true);
                lastPage.setDisable(true);
            }else {
                startRecord.setText(String.valueOf(1));
            }
            if(ppl.getTotalRecords()<10) {
                lastRecord.setText(String.valueOf(ppl.getTotalRecords()));
                nextPage.setDisable(true);
            }else{
                lastRecord.setText(String.valueOf(10));
            }
            totalRecords.setText(String.valueOf(ppl.getTotalRecords()));

        } else if(event.getSource() == firstPage){
            people.clear();
            previousPage.setDisable(true);
            if(lastName.getText() != null){
                getPage = 1;
                People ppl =personGateway.getPeople(lastName.getText(), getPage );
                ArrayList<Person> listOfPeople = ppl.getPeople();
                logger.info("Retrieved list of people.");
                for(Person i :  listOfPeople){
                    people.add(i);
                }

                personList.setItems(people);

                startRecord.setText(String.valueOf(1));
                lastRecord.setText(String.valueOf(10));
                totalRecords.setText(String.valueOf(ppl.getTotalRecords()));
            }
            else{
                getPage = 1;
                People ppl=personGateway.getPeople(getPage );
                ArrayList<Person> listOfPeople = ppl.getPeople();

                logger.info("Retrieved list of people.");
                for(Person i :  listOfPeople){
                    people.add(i);
                }


                personList.setItems(people);

                startRecord.setText(String.valueOf(1));
                lastRecord.setText(String.valueOf(10));
                totalRecords.setText(String.valueOf(ppl.getTotalRecords()));
            }



        } else if(event.getSource() == previousPage){
            people.clear();
            currentPage-=1;
            getPage = currentPage;
            if(currentPage == 1){
                previousPage.setDisable(true);
            }
            if(currentPage < 9+ Integer.parseInt(totalRecords.getText())/10){
                nextPage.setDisable(false);
            }
            if(lastName.getText() != null){
                People ppl =personGateway.getPeople(lastName.getText(), getPage );
                ArrayList<Person> listOfPeople = ppl.getPeople();
                logger.info("Retrieved list of people.");
                for(Person i :  listOfPeople){
                    people.add(i);
                }

                personList.setItems(people);

                startRecord.setText(String.valueOf((currentPage-1)*10+1));
                lastRecord.setText(String.valueOf(currentPage*10));
                totalRecords.setText(String.valueOf(ppl.getTotalRecords()));
            }
            else{
                People ppl=personGateway.getPeople(getPage );
                ArrayList<Person> listOfPeople = ppl.getPeople();
                logger.info("Retrieved list of people.");
                for(Person i :  listOfPeople){
                    people.add(i);
                }

                personList.setItems(people);

                startRecord.setText(String.valueOf((currentPage-1)*10+1));
                lastRecord.setText(String.valueOf(currentPage*10));
                totalRecords.setText(String.valueOf(ppl.getTotalRecords()));
            }


        } else if (event.getSource() == nextPage){
            currentPage+= 1;
            getPage = currentPage;
            people.clear();
            previousPage.setDisable(false);
            int lPage=0;
            if(currentPage == 9+ Integer.parseInt(totalRecords.getText())/10){
                nextPage.setDisable(true);
                lPage =currentPage;
            }
            if(lastName.getText() != null){
                People ppl =personGateway.getPeople(lastName.getText(), getPage );
                ArrayList<Person> listOfPeople = ppl.getPeople();
                logger.info("Retrieved list of people.");
                for(Person i :  listOfPeople){
                    people.add(i);
                }

                personList.setItems(people);

                startRecord.setText(String.valueOf((currentPage-1)*10+1));
                if(lPage >0){
                    lastRecord.setText(String.valueOf(ppl.getTotalRecords()));
                }else {
                    lastRecord.setText(String.valueOf(currentPage * 10));
                }
                totalRecords.setText(String.valueOf(ppl.getTotalRecords()));
            }
            else{
                People ppl=personGateway.getPeople(getPage );
                ArrayList<Person> listOfPeople = ppl.getPeople();
                logger.info("Retrieved list of people.");
                for(Person i :  listOfPeople){
                    people.add(i);
                }

                personList.setItems(people);

                startRecord.setText(String.valueOf((currentPage-1)*10+1));
                if(lPage >0){
                    lastRecord.setText(String.valueOf(ppl.getTotalRecords()));
                }else {
                    lastRecord.setText(String.valueOf(currentPage * 10));
                }
                totalRecords.setText(String.valueOf(ppl.getTotalRecords()));
            }

        } else if (event.getSource() == lastPage){
            people.clear();
            currentPage = (9+ Integer.parseInt(totalRecords.getText()))/10;
            getPage = currentPage;
            nextPage.setDisable(true);
            if(lastName.getText() != null){
                People ppl =personGateway.getPeople(lastName.getText(), getPage );
                ArrayList<Person> listOfPeople = ppl.getPeople();
                logger.info("Retrieved list of people.");
                for(Person i :  listOfPeople){
                    people.add(i);
                }

                personList.setItems(people);

                startRecord.setText(String.valueOf((currentPage-1)*10+1));
                lastRecord.setText(String.valueOf(ppl.getTotalRecords()));
                totalRecords.setText(String.valueOf(ppl.getTotalRecords()));
            }
            else{
                People ppl=personGateway.getPeople(getPage );
                ArrayList<Person> listOfPeople = ppl.getPeople();

                logger.info("Retrieved list of people.");
                for(Person i :  listOfPeople){
                    people.add(i);
                }

                personList.setItems(people);

                startRecord.setText(String.valueOf((currentPage-1)*10+1));
                lastRecord.setText(String.valueOf(ppl.getTotalRecords()));
                totalRecords.setText(String.valueOf(ppl.getTotalRecords()));
            }

        }
        personList.refresh();

    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        people = FXCollections.observableArrayList();
        //Should I put PersonGateway in personParam?
        //Would be cleaner(?) and still could make more than one if necessary
        personGateway = new PersonGateway("http://localhost:8080", SessionParameters.getSessionToken());
        getPage =1;
        currentPage =1;
        lastName.setText(null);
        previousPage.setDisable(true);

        People ppl = personGateway.getPeople();
        ArrayList<Person> listOfPeople = ppl.getPeople();
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
        startRecord.setText(String.valueOf(1));
        lastRecord.setText(String.valueOf(10));
        totalRecords.setText(String.valueOf(ppl.getTotalRecords()));

    }

}