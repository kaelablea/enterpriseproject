package person.login;


import org.apache.http.client.methods.CloseableHttpResponse;

import org.apache.http.impl.client.CloseableHttpClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import person.User;
import person.db.DBConnect;
import person.gateway.SessionGateway;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

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
    public static ResponseEntity<String> login(@RequestBody User user) {
        final Logger logger = LogManager.getLogger();

        try {
            int id = new SessionGateway(connection).authenticate(user.getUserName(), user.getPassword());

            String token = SessionGateway.createSessionToken(user.getUserName());

            ResponseEntity<String> response = new ResponseEntity<String>(token, HttpStatus.valueOf(200));

            return response;

        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Invalid login.");
            ResponseEntity<String> response = new ResponseEntity<String>("Invalid login", HttpStatus.valueOf(401));
            return response;
        }
    }

}
