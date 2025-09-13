package com.myapp.client.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.client.dto.AuthResponse;
import com.myapp.client.dto.LoginRequest;
import com.myapp.client.dto.SignupRequest;
import com.myapp.client.util.Api;
import com.myapp.client.util.RefreshStore;
import com.myapp.client.util.Router;
import com.myapp.client.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

public class ControllerLogin {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML private void clearFields(){
        errorLabel.setText("");
        usernameField.clear();
        passwordField.clear();
    }

    @FXML private void handleLogin() {
        System.out.println("Login: " + usernameField.getText() + " " + passwordField.getText());
        Api api = new Api();
        try{
            HttpResponse<String> response = api.login(new LoginRequest(usernameField.getText(),passwordField.getText()));
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> json = mapper.readValue(response.body(), new TypeReference<>(){});
            if(response.statusCode() == 200){
                errorLabel.setText("Login effettuato con successo!");
                AuthResponse authResponse = mapper.readValue(response.body(), new TypeReference<>(){});
                Session.set(authResponse.accessToken());
                RefreshStore.save(authResponse.refreshToken());
                clearFields();
                Router.go("home");
            }else {
                errorLabel.setText("Login fallito! " + json.get("message"));
            }

        }catch (Exception e){
            //NON E' STATO POSSIBILE EFFETTUARE IL LOGIN
            errorLabel.setText("Login fallito per problemi di connessione al server! Riprovare piu' tardi\t\t" + e.getMessage());
        }
    }

    public Instant parseExpFromJwt(String jwt) throws Exception {
        String[] parts = jwt.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), java.nio.charset.StandardCharsets.UTF_8);
        JsonNode n = new ObjectMapper().readTree(payload);
        long exp = n.get("exp").asLong(); // seconds since epoch
        return Instant.ofEpochSecond(exp);
    }
}
