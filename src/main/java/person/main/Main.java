package person.main;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import person.fx.ViewSwitcher;

@SpringBootApplication
@ComponentScan({"person.login","person.gateway"})
@EntityScan({"person"})
public class Main {
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }

}
