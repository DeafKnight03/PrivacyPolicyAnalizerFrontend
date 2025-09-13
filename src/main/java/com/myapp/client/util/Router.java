package com.myapp.client.util;

import com.myapp.client.App;
import com.myapp.client.controller.ControllerLogin;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.Style;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class Router {
    private static Scene scene;
    private static final Map<String, Parent> cache = new HashMap<>();

    private Router() {}

    /** Inizializza con la Scene principale (chiamalo da App.start) */
    public static void init(Scene mainScene) {
        scene = mainScene;
    }

    /** Cambia pagina caricando /fxml/{name}.fxml e applica CSS con switch */
    public static void go(String name) {
        if (!Platform.isFxApplicationThread()) { Platform.runLater(() -> go(name)); return; }
        if (scene == null) throw new IllegalStateException("Router.init(scene) non chiamato");

        try {
            Parent view = cache.computeIfAbsent(name, n -> {
                try {
                    return FXMLLoader.load(Router.class.getResource("/fxml/" + n + ".fxml"));
                } catch (IOException e) {
                    throw new RuntimeException("Impossibile caricare /fxml/" + n + ".fxml", e);
                }
            });

            scene.setRoot(view);

            // Applica CSS/layout e adatta la finestra alle pref del nuovo root
            view.applyCss();
            view.layout();
            Stage stage = (Stage) scene.getWindow();
            if (!stage.isMaximized()) stage.sizeToScene();
            stage.centerOnScreen();

            // CSS specifici per pagina (switch)
            applyPageStyles(name);

        } catch (RuntimeException ex) {
            throw ex;
        }
    }

    /** Rimuove i CSS della pagina e aggiunge quello corretto (light/dark) via switch */
    private static void applyPageStyles(String page) {
        var styles = scene.getStylesheets();
        boolean dark = App.getJMetro().getStyle() == Style.DARK;
        switch (page) {
            case "home":
                styles.removeIf(s -> s.endsWith("/css/homeLight.css") || s.endsWith("/css/homeDark.css"));
                String chosen = dark ? "/css/homeDark.css" : "/css/homeLight.css";
                styles.add(Router.class.getResource(chosen).toExternalForm());
                break;
            case "login":
                styles.removeIf(s -> s.endsWith("/css/homeLight.css") || s.endsWith("/css/homeDark.css"));

                break;
            default:
                break;

        }
    }

    /** Facoltativi */
    public static void invalidate(String name) { cache.remove(name); }
    public static void clearCache() { cache.clear(); }
}
