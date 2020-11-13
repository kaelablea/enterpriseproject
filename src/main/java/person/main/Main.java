package person.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import person.fx.ViewSwitcher;

@SpringBootApplication
@ComponentScan({"login", "detail", "list"})
public class Main{

    public static void main(String[] args) {
        SpringApplication.run(person.main.Application.class, args);
    }


}
