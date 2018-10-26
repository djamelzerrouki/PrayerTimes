package com.houarizegai.prayertimes.java.controllers;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.Scanner;

public class PrayerTimesController implements Initializable {

    @FXML
    private Label lblDate, lblTime;

    @FXML
    private JFXComboBox<String> comboWilaya;

    @FXML
    private JFXListView<StackPane> listTimes;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initComboWilaya();

        listTimes.getItems().clear();
        listTimes.getItems().add(getItemList("الفجر", ""));
        listTimes.getItems().add(getItemList("الشروق", ""));
        listTimes.getItems().add(getItemList("الظهر", ""));
        listTimes.getItems().add(getItemList("العصر", ""));
        listTimes.getItems().add(getItemList("المغرب", ""));
        listTimes.getItems().add(getItemList("العشاء", ""));
    }

    private void initComboWilaya() {
        // Add wilaya name to Comnobox
        comboWilaya.getItems().clear();
        String jsonWilayaPath = "F:\\Projects\\JavaProjects\\Javafx\\PrayerTimes\\src\\com\\houarizegai\\prayertimes\\resources\\utils\\wilaya.json";
        JSONArray jsonArray = new JSONArray(readJsonFromFile(jsonWilayaPath));
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject wilayaObj = jsonArray.getJSONObject(i);
            String wilayaName = wilayaObj.getString("nom");
            comboWilaya.getItems().add(wilayaName);
        }

        // Add Event to ComboBox
        comboWilaya.setOnAction(e -> {
            getJsonPrayerTimes(comboWilaya.getSelectionModel().getSelectedItem());
        });
    }

    private StackPane getItemList(String prayerName, String prayerTime) { // create prayer with time
        StackPane prayerItem = null;
        try {
            prayerItem = FXMLLoader.load(getClass().getResource("/com/houarizegai/prayertimes/resources/views/PrayerItem.fxml"));
            ((Label) prayerItem.getChildren().get(0)).setText(prayerName);
            ((Label) prayerItem.getChildren().get(1)).setText(prayerTime);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return prayerItem;
    }

    private String readJsonFromFile(String pathname) { // Read json file and convert it to String
        try {
            File file = new File(pathname);

            StringBuilder fileContents = new StringBuilder((int) file.length());

            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    fileContents.append(scanner.nextLine() + System.lineSeparator());
                }
                return fileContents.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void getJsonPrayerTimes(String wilaya) { // Get prayer times from WebService
        try {
            HttpResponse<JsonNode> jsonResponse
                    = Unirest.get("https://api.pray.zone/v2/times/today.json")
                    .header("accept", "application/json")
                    .queryString("city", wilaya)
                    .asJson();
            JSONObject jsonDate = new JSONObject(jsonResponse)
                    .getJSONObject("results")
                    .getJSONArray("datetime")
                    .getJSONObject(0)
                    .getJSONObject("times");

            // Edit Times of prayer in UI
            ((Label) listTimes.getItems().get(0).getChildren().get(1)).setText(jsonDate.getString("Fajr"));
            ((Label) listTimes.getItems().get(1).getChildren().get(1)).setText(jsonDate.getString("Sunrise"));
            ((Label) listTimes.getItems().get(2).getChildren().get(1)).setText(jsonDate.getString("Dhuhr"));
            ((Label) listTimes.getItems().get(3).getChildren().get(1)).setText(jsonDate.getString("Asr"));
            ((Label) listTimes.getItems().get(4).getChildren().get(1)).setText(jsonDate.getString("Maghrib"));
            ((Label) listTimes.getItems().get(5).getChildren().get(1)).setText(jsonDate.getString("Isha"));

        } catch (UnirestException e) {
            e.printStackTrace();
        }

    }

}
