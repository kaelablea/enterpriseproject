package person.main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import person.fx.ViewSwitcher;
import person.fx.ViewType;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable{
    private static final Logger logger = LogManager.getLogger();
    @FXML
    private Button login;
    @FXML
    private TextArea userName;

    public  MainController(){
    }

    @FXML
    void handler(ActionEvent event){
        //remove this if when we implement authentication
        if (userName.getText() == null){
            ViewSwitcher.getInstance().switchView(ViewType.PersonListView);
        }
        //login stuff here later
        logger.info(" " + userName.getText() + "LOGGED IN");
        ViewSwitcher.getInstance().switchView(ViewType.PersonListView);
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1){

    }
}
