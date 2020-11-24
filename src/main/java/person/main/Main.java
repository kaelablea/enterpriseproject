package person.main;


import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"person.backend"})
@EntityScan("person")
public class Main {
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }

}
