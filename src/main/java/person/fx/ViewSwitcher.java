package person.fx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import person.detail.PersonDetailController;
import person.list.PersonListController;
import person.login.LoginController;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ViewSwitcher implements Initializable {
    private static ViewSwitcher instance = null;

    @FXML
    private BorderPane rootPane;

    private ViewSwitcher() {

    }

    public static ViewSwitcher getInstance() {
        if(instance == null)
            instance = new ViewSwitcher();
        return instance;
    }

    public void switchView(ViewType viewType) {
        FXMLLoader loader = null;

        try {
            switch(viewType) {
                case LoginView:
                    loader = new FXMLLoader(ViewSwitcher.class.getResource("/loginView.fxml"));
                    loader.setController(new LoginController());
                    break;
                case PersonDetailView:
                    loader = new FXMLLoader(ViewSwitcher.class.getResource("/personDetailView.fxml"));
                    loader.setController(new PersonDetailController(PersonParameters.getPersonParm(), SessionParameters.getSessionToken()));
                    break;

                case PersonListView:
                    loader = new FXMLLoader(ViewSwitcher.class.getResource("/personListView.fxml"));
                    loader.setController(new PersonListController());
                    break;
                default:
                    break;
            }
            Parent rootNode = loader.load();
            rootPane.setCenter(rootNode);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public void initialize(URL arg0, ResourceBundle arg1) {
        // load the initial view: person detail into the main view
        //PersonParameters.setPersonParm(new Person(1234, "Bob", "Smith", LocalDate.of(1980, 1, 1),0));
        switchView(ViewType.LoginView);

    }
}
