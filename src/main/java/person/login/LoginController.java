package person.login;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import person.Person;
import person.fx.SessionParameters;
import person.fx.ViewSwitcher;
import person.fx.ViewType;
import person.gateway.PersonGateway;
import person.gateway.SessionGateway;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class LoginController implements Initializable{
    private static final Logger logger = LogManager.getLogger();
    @FXML
    private Button login;
    @FXML
    private TextArea userName;
    @FXML
    private PasswordField password;
    @FXML
    private Alert badLogin;

    public LoginController(){
    }

    @FXML
    void handler(ActionEvent event){
        try {
            String sessionToken = SessionGateway.authenticate(userName.getText(), password.getText());
            SessionParameters.setSessionToken(sessionToken);
            PersonGateway personList = new PersonGateway("http://localhost:8080/people", sessionToken);
            List<Person> people = personList.getPeople();
            logger.info(" " + userName.getText() + "LOGGED IN");
            ViewSwitcher.getInstance().switchView(ViewType.PersonListView);
        } catch (RuntimeException | IOException e){
            Alert badLogin = new Alert(Alert.AlertType.ERROR, "Invalid Login! Try again.");
            badLogin.showAndWait();
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1){

    }
}
