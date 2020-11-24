package person.backend.repocontrollers;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import person.backend.repositories.SessionRepo;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@RestController
public class SessionController{
    private static final Logger logger = LogManager.getLogger();

    private static Connection connection;
    // create a connection on startup
    @PostConstruct
    public void startup() {
        try {
            logger.info("Trying to make db connection.");
            connection = DBConnect.connectToDB();
            logger.info("*** MySQL connection created");
        } catch (SQLException | IOException e) {
            logger.error("*** " + e);

            // TODO: find a better way to force shutdown on connect failure
            // System.exit(0);
        }
    }

    // close a connection on shutdown
    @PreDestroy
    public void cleanup() {
        try {
            connection.close();
            logger.info("*** MySQL connection closed");
        } catch (SQLException e) {
            logger.error("*** " + e);
        }
    }

    @PostMapping("/login")
    public static ResponseEntity<String> login(@RequestBody Map<String,String> user) {
        final Logger logger = LogManager.getLogger();

        try {
            logger.info(user.get("username")+ " " + user.get("password"));
            int id = new SessionRepo(connection).authenticate(user);

            String token = SessionRepo.createSessionToken(user.get("username"), id);

            ResponseEntity<String> response = new ResponseEntity<String>(token, HttpStatus.valueOf(200));

            return response;

        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Invalid login.");
            ResponseEntity response = new ResponseEntity("Invalid login", HttpStatus.valueOf(401));
            return response;
        }
    }

}
