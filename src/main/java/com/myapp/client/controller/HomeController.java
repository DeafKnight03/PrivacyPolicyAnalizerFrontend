package com.myapp.client.controller;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import jfxtras.styles.jmetro.Style;
import com.myapp.client.App;


public class HomeController {


    /*@FXML private void handleLogout() {
        System.out.println("Logout clicked");

    }*/
    @FXML
    private void handleTitleClick(MouseEvent e) {
        System.out.println("Page title clicked!");
        Style style = App.getJMetro().getStyle();

        if(style.equals(Style.DARK)){
            App.getJMetro().setStyle(Style.LIGHT);
        }else{
            App.getJMetro().setStyle(Style.DARK);
        }
        style = App.getJMetro().getStyle();
        App.getMainScene().getStylesheets().removeIf(s ->
                s.endsWith("/css/homeLight.css") || s.endsWith("/css/homeDark.css"));

        // Aggiungi quello giusto
        String css = (style == Style.DARK) ? "/css/homeDark.css" : "/css/homeLight.css";
        App.getMainScene().getStylesheets().add(getClass().getResource(css).toExternalForm());
    }


}
