package com.myapp.client;
import com.myapp.client.util.Router;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.JMetroStyleClass;
import jfxtras.styles.jmetro.Style;

public class App extends Application {

    private static JMetro jMetro;
    private static Scene scene;

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));

        // Base background per JMetro (fondamentale)
        root.getStyleClass().add(JMetroStyleClass.BACKGROUND);

        Router.init(scene = new Scene(root));
        var theme = Style.LIGHT;
        // 1) Applica JMetro prima dei tuoi CSS
        jMetro = new JMetro(theme);
        jMetro.setScene(scene);

        // 2) Carica il CSS dedicato al tema corrente
        //applyThemeStylesheets(theme);

        stage.setScene(scene);
        stage.setTitle("App");
        stage.show();

        // Esempio: toggla tema dopo 2 secondi
        // new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2))
        //     .setOnFinished(e -> switchTheme());
        // PT.play();

    }

    public static JMetro getJMetro() {
        return jMetro;
    }

    public void applyThemeStylesheets(Style style) {
        // Rimuovi eventuali CSS precedenti del tema
        scene.getStylesheets().removeIf(s ->
                s.endsWith("/css/homeLight.css") || s.endsWith("/css/homeDark.css"));

        // Aggiungi quello giusto
        String css = (style == Style.DARK) ? "/css/homeDark.css" : "/css/homeLight.css";
        scene.getStylesheets().add(getClass().getResource(css).toExternalForm());
    }

    public static Scene getMainScene() {
        return scene;
    }
    // Call this from a toggle button / menu item / settings
    public void switchTheme() {
        Style newStyle = (jMetro.getStyle() == Style.DARK) ? Style.LIGHT : Style.DARK;
        jMetro.setStyle(newStyle);          // cambia tema JMetro
        applyThemeStylesheets(newStyle);    // cambia CSS dedicato
    }

    public static void main(String[] args) {
        launch(args);
    }
}
