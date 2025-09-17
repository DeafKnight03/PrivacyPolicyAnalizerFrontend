package com.myapp.client.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.client.App;
import com.myapp.client.dto.SaveResultRequest;
import com.myapp.client.dto.StringDto;
import com.myapp.client.entities.CheckListItem;
import com.myapp.client.util.Api;
import com.myapp.client.util.JwtUtils;
import com.myapp.client.util.Router;
import com.myapp.client.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AnalysisController {
    @FXML
    private Label chosenFile;

    @FXML
    private Button analysisSaveBtn;

    @FXML
    private TextFlow resultField;

    private final FileChooser fc = new FileChooser();
    private java.io.File file;
    private String sendingText;
    private String savingJson;

    @FXML
    private void filePicker() {
        Window owner = App.getMainScene().getWindow();
        fc.setTitle("Choose a file");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("text files", "*.txt"));

        file = fc.showOpenDialog(owner);

        if (file != null) {
            chosenFile.setText(file.getName());

            // Se vuoi visualizzare il percorso completo del file:
            // chosenFile.setText(file.getAbsolutePath());
        } else {
            // Opzionale: gestire il caso in cui l'utente annulla
            chosenFile.setText("Nessun file selezionato");
        }

        resultField.getChildren().setAll(new Text("")); // reset stile caret
        resultField.getChildren().clear();
        analysisSaveBtn.setText("ANALIZZA");
        analysisSaveBtn.setOnAction(handle -> handleAnalysis());
        savingJson = "";
        sendingText = "";

    }

    @FXML
    private void handleAnalysis() {
        String content = "";
        if (file == null) {
            content = "Nessun file selezionato";
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERRORE");
            alert.setContentText(content);
            alert.showAndWait();
        } else {
            try {

                String filePath = file.getAbsolutePath();
                sendingText = Files.readString(Path.of(filePath));
                Api api = new Api();
                StringDto dto = new StringDto(sendingText);
                CompletableFuture<HttpResponse<String>> fut = api.analyze1Async(dto);
                fut.thenApply(response -> {
                    try{
                        if (response.statusCode() != 200) {
                            javafx.application.Platform.runLater(() -> {
                                String content2 = "Errore lato server! riprovare piu tardi\n" + (response.body() != null ? response.body() : "");
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("ERRORE");
                                alert.setContentText(content2);
                                alert.showAndWait();
                                throw new RuntimeException(content2);
                            });
                            return null;
                        } else {
                            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                            StringDto json = mapper.readValue(response.body(), StringDto.class);//THIS IS THE PROBLEM
                            String content2 = json.stringa();
                            System.out.println(content2);

                            return mapper.readValue(content2, new TypeReference<List<CheckListItem>>() { });

                        }
                    }catch(Exception e){
                        javafx.application.Platform.runLater(() -> {
                            //e.printStackTrace();
                            String content2 = "Errore inaspettato! Riprovare";
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("ERRORE");
                            alert.setContentText(content2);
                            alert.showAndWait();
                        });
                        return null;
                    }


                }).thenAccept(list -> {
                    javafx.application.Platform.runLater(() -> {
                        if (list != null) {
                            System.out.println("\nfatto!\n");
                            setResult(list);
                            analysisSaveBtn.setText("Salva risultato");
                            analysisSaveBtn.setOnAction(handle -> handleSaveResult());
                        }
                    });


                }).exceptionally(ex -> {
                    javafx.application.Platform.runLater(() -> {
                        //ex.printStackTrace();
                        String content2 = "Errore inaspettato! Riprovare";
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("ERRORE");
                        alert.setContentText(content2);
                        alert.showAndWait();
                    });

                    return null;
                });

            } catch (Exception e) {
                //e.printStackTrace();
                content = "File non valido";
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERRORE");
                alert.setContentText(content);
                alert.showAndWait();
            }

        }
    }

    private void setResult(List<CheckListItem> list) {
        savingJson = "";
        resultField.getChildren().setAll(new Text("")); // reset stile caret
        resultField.getChildren().clear();
        resultField.getChildren().add(new Text("\n"));
        for (CheckListItem item : list) {
            String content = item.toString();
            for (String line : content.split("\\R", -1)) {   // preserva linee vuote
                Text t = new Text(line + "\n");
                t.getStyleClass().add("flow-line");          // opzionale per CSS
                resultField.getChildren().add(t);
                savingJson += line + "\n";
            }
            resultField.getChildren().add(new Text("\n"));
            savingJson += "\n";
        }



    }

    @FXML
    private void handleBackHome() {
        Router.go("home");
    }


    @FXML
    private void handleSaveResult() {
        Long id = JwtUtils.userIdFromJwt(Session.get().getAccessToken());
        SaveResultRequest req = new SaveResultRequest(sendingText, savingJson,id);
        Api api = new Api();
        try{
            HttpResponse<Void> r = api.save(req);
            if(r.statusCode() == 200){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Informazione");
                alert.setContentText("Risultato salvato con successo!");
                alert.showAndWait();
                resultField.getChildren().setAll(new Text("")); // reset stile caret
                resultField.getChildren().clear();
                analysisSaveBtn.setText("ANALIZZA");
                analysisSaveBtn.setOnAction(handle -> handleAnalysis());
                file = null;
                chosenFile.setText("Nessun file selezionato");
                savingJson = "";
                sendingText = "";
                Router.go("home");
            }else{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERRORE");
                alert.setContentText("Errore nel salvataggio del risultato");
                alert.showAndWait();
            }
        } catch (Exception e) {
            //
        }

    }



}
