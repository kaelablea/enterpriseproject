package person.main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import person.fx.ViewSwitcher;

import java.io.IOException;

public class App extends Application{
    private ConfigurableApplicationContext applicationContext;
    @Override
    public void init() {
        String[] args = getParameters().getRaw().toArray(new String[0]);

        this.applicationContext = new SpringApplicationBuilder()
                .sources(Main.class)
                .run(args);
    }

    @Override
    public void stop() {
        this.applicationContext.close();
        Platform.exit();
    }
    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/mainview.fxml"));
            loader.setController(ViewSwitcher.getInstance());
            Parent rootNode = loader.load();

            Scene scene = new Scene(rootNode);
            stage.setScene(scene);
            stage.setTitle("Main Login view");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
