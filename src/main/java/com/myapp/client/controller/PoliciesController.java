package com.myapp.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.myapp.client.dto.ListItem;
import com.myapp.client.dto.StringDto;
import com.myapp.client.dto.UserPoliciesList;
import com.myapp.client.util.Api;
import com.myapp.client.util.JwtUtils;
import com.myapp.client.util.Router;
import com.myapp.client.util.Session;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.http.HttpResponse;

public class PoliciesController {
    private int nPolicies;
    @FXML
    private HBox pageBar;
    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    @FXML
    private TextFlow txt1;
    @FXML
    private TextFlow txt2;
    @FXML
    private TextFlow txt3;
    @FXML
    private TextFlow txt4;
    @FXML
    private TextFlow txt5;
    @FXML
    private TextFlow txt6;



    public void initialize() {
        Api api = new Api();
        try{
            Long id = JwtUtils.userIdFromJwt(Session.get().getAccessToken());
            System.out.println("id: " + id);
            String s = String.valueOf(id);
            System.out.println("s: " + s);
            StringDto dto = new StringDto(s);
            System.out.println("dto: " + dto);
            HttpResponse<String> res = api.count(dto);

            if(res.statusCode()!=200){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERRORE");
                alert.setContentText("Errore lato server! riprovare piu tardi");
                alert.showAndWait();
                Router.go("home");
            }else{
                StringDto dto2 = mapper.readValue(res.body(), StringDto.class);
                nPolicies = Integer.parseInt(dto2.stringa());
                setPageBar(nPolicies);
                if(nPolicies!=0) build(1);
            }



        }catch(Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERRORE");
            alert.setContentText("Errore! riprovare piu tardi");
            alert.showAndWait();
            Router.go("home");
        }
    }

    private void setPageBar(int pagesNum) {
        double n = Math.ceil(((double)(pagesNum)/6.00));
        System.out.println("n: " + n);
        pageBar.getChildren().clear();
        pageBar.setAlignment(Pos.CENTER);
        //pageBar.getChildren().add(mkSquareBtn("1"));
        for(int i = 0; i < n; i++){
            pageBar.getChildren().add(mkSquareBtn(String.valueOf(i+1)));
        }
    }

    public void build(int pageNum) {
        StringDto s = new StringDto(JwtUtils.userIdFromJwt(Session.get().getAccessToken())+" / "+pageNum);
        Api api = new Api();
        try{

            HttpResponse<String> response = api.getPolicies(s);
            if(response.statusCode()!=200){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERRORE");
                alert.setContentText("Errore nel recupero dei dati lato server! riprovare piu tardi");
                alert.showAndWait();
                Router.go("home");
            }else{
                UserPoliciesList list = mapper.readValue(response.body(), UserPoliciesList.class);
                TextFlow[] tfs = {txt1,txt2,txt3,txt4,txt5,txt6};
                for(TextFlow tf : tfs){
                    tf.getChildren().clear();
                }
                int i = 0;
                for(ListItem l : list.list()){
                    tfs[i].getChildren().add(new Text("\n"));
                    Text t = new Text(l.id()+"");
                    t.setStyle("-fx-font-weight: bold;");
                    tfs[i].getChildren().add(t);
                    tfs[i].getChildren().add(new Text("\n"));
                    tfs[i].getChildren().add(new Text("\n"));
                    tfs[i].getChildren().add(new Text(l.text()));
                    tfs[i].getChildren().add(new Text("\n"));
                    tfs[i].getChildren().add(new Text("\n"));
                    Text t2 = new Text(l.createdAt().toString());
                    t2.setStyle("-fx-font-weight: bold;");
                    tfs[i].getChildren().add(t2);
                    tfs[i].getChildren().add(new Text("\n"));
                    tfs[i].getChildren().add(new Text("\n"));
                    Text t3 = new Text("VALUTAZIONE:\n" +l.good() + "/10");
                    t3.setStyle("-fx-font-weight: bold;");
                    tfs[i].getChildren().add(t3);
                    tfs[i].getChildren().add(new Text("\n"));
                    i++;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERRORE");
            alert.setContentText("Errore nel recupero dei dati!");
            alert.showAndWait();
            Router.go("home");

        }
    }

    @FXML
    private void handleBackHome() {
        Router.go("home");
    }



    // helper
    private Button mkSquareBtn(String text) {
        Button b = new Button(text);
        b.setMnemonicParsing(false);               // keep text literal
        b.setTextOverrun(OverrunStyle.CLIP);       // NEVER show "..."
        b.setPadding(Insets.EMPTY);                // remove extra padding
        b.setPrefSize(48, 48);                     // square size
        b.setMinSize(48, 48);
        b.setMaxSize(48, 48);
        b.setFont(Font.font(16));
        b.setOnAction(e->build(Integer.parseInt(text)));// big enough for 2 digits
        return b;
    }
}
