package person.main;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import person.fx.ViewSwitcher;

public class Application  extends Main{

    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/mainview.fxml"));
        loader.setController(ViewSwitcher.getInstance());
        Parent rootNode = loader.load();
        Scene scene = new Scene(rootNode);
        stage.setScene(scene);
        stage.setTitle("Main Login view");
        stage.show();

    }
}
