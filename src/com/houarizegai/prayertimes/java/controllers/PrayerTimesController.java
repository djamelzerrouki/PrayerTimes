package com.houarizegai.prayertimes.java.controllers;

import com.houarizegai.prayertimes.java.Launcher;
import com.houarizegai.prayertimes.java.models.PrayerTimes;
import com.houarizegai.prayertimes.java.models.PrayerTimesBuilder;
import com.houarizegai.prayertimes.java.utils.Constants;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.controls.JFXListView;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class PrayerTimesController implements Initializable {

    @FXML
    private StackPane menuBar;

    @FXML
    private JFXHamburger hamburgerMenu;

    @FXML
    private JFXComboBox<String> comboCities;

    @FXML
    private Label lblDate;

    @FXML
    private Label lblTimeH, lblTimeSeparator, lblTimeM, lblTimeSeparator2, lblTimeS;

    @FXML
    private Label lblPrayerFajr, lblPrayerSunrise, lblPrayerDhuhr, lblPrayerAsr, lblPrayerMaghrib, lblPrayerIsha;

    // For Make Stage Drageable
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initDateAndClock();
        initComboCities();

        // Make Tiaret city as default
        comboCities.getSelectionModel().select("Tiaret");
        getJsonPrayerTimes(comboCities.getSelectionModel().getSelectedItem());

        // For make stage Drageable
        menuBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        menuBar.setOnMouseDragged(event -> {
            Launcher.stage.setX(event.getScreenX() - xOffset);
            Launcher.stage.setY(event.getScreenY() - yOffset);
            Launcher.stage.setOpacity(0.7f);
        });
        menuBar.setOnDragDone(e -> {
            Launcher.stage.setOpacity(1.0f);
        });
        menuBar.setOnMouseReleased(e -> {
            Launcher.stage.setOpacity(1.0f);
        });
    }

    private void initDateAndClock() {
        /* initialize clock (date & time) of prayer times */
        KeyFrame clockKeyFrame = new KeyFrame(Duration.ZERO, e -> {
            Date date = new Date();
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            lblDate.setText(dateFormat.format(date));

            dateFormat = new SimpleDateFormat("HH:mm:ss");
            String[] time = dateFormat.format(date).split(":");
            lblTimeS.setText(time[2]);
            lblTimeM.setText(time[1]);
            lblTimeH.setText(time[0]);

            // If new day change the prayer times
            if(dateFormat.equals("00:00:00") && comboCities.getSelectionModel() != null) {
                getJsonPrayerTimes(comboCities.getSelectionModel().getSelectedItem());
            }
        });

        Timeline clock = new Timeline(clockKeyFrame, new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        /* Show and hide animation for separetor of time */
        KeyFrame clockSeparatorKeyFrame = new KeyFrame(Duration.ZERO, e -> {
            if(lblTimeSeparator.isVisible()) {
                lblTimeSeparator.setVisible(false);
                lblTimeSeparator2.setVisible(false);
            } else {
                lblTimeSeparator.setVisible(true);
                lblTimeSeparator2.setVisible(true);
            }
        });

        Timeline clockSeparator = new Timeline(clockSeparatorKeyFrame, new KeyFrame(Duration.millis(500)));
        clockSeparator.setCycleCount(Animation.INDEFINITE);
        clockSeparator.play();

    }

    private void initComboCities() {
        // Add cities names to ComboBox
        comboCities.getItems().clear();
        comboCities.getItems().addAll(Constants.DZ_CITIES);

        // Add Event to ComboBox
        comboCities.setOnAction(e -> {
            getJsonPrayerTimes(comboCities.getSelectionModel().getSelectedItem());
        });
    }

    private void getJsonPrayerTimes(String city) { // Get prayer times from WebService
        city = city // format City
                .replaceAll(" ", "-")
                .replaceAll("é", "e")
                .replaceAll("è", "e")
                .replaceAll("â", "a")
                .replaceAll("'", "-")
                .replaceAll("ï", "i");

        try {
            HttpResponse<JsonNode> jsonResponse
                    = Unirest.get("https://api.pray.zone/v2/times/today.json")
                    .queryString("city", city)
                    .asJson();

            JSONObject jsonRoot = new JSONObject(jsonResponse.getBody().toString());

            if(!jsonRoot.has("results")) {
                System.out.println("City not found !");
                /* Make Empty prayer times */
                PrayerTimes prayerTimes = new PrayerTimesBuilder()
                        .setFajr("hh:mm")
                        .setSunrise("hh:mm")
                        .setDhuhr("hh:mm")
                        .setAsr("hh:mm")
                        .setMaghrib("hh:mm")
                        .setIsha("hh:mm")
                        .build();
                setPrayerTimes(prayerTimes);
                return;
            }

            JSONObject jsonDate = jsonRoot.getJSONObject("results")
                    .getJSONArray("datetime")
                    .getJSONObject(0)
                    .getJSONObject("times");

            /* Edit Times of prayer in UI */
            PrayerTimes prayerTimes = new PrayerTimesBuilder()
                    .setFajr(jsonDate.getString("Fajr"))
                    .setSunrise(jsonDate.getString("Sunrise"))
                    .setDhuhr(jsonDate.getString("Dhuhr"))
                    .setAsr(jsonDate.getString("Asr"))
                    .setMaghrib(jsonDate.getString("Maghrib"))
                    .setIsha(jsonDate.getString("Isha"))
                    .build();
            setPrayerTimes(prayerTimes);

        } catch (UnirestException e) {
            e.printStackTrace();
        }

    }

    private void setPrayerTimes(PrayerTimes prayerTimes) {
        lblPrayerFajr.setText(prayerTimes.getFajr());
        lblPrayerSunrise.setText(prayerTimes.getSunrise());
        lblPrayerDhuhr.setText(prayerTimes.getDhuhr());
        lblPrayerAsr.setText(prayerTimes.getAsr());
        lblPrayerMaghrib.setText(prayerTimes.getMaghrib());
        lblPrayerIsha.setText(prayerTimes.getIsha());
    }

    @FXML
    private void onClose() {
        Platform.exit();
    }

    @FXML
    private void onHide() {
        Launcher.stage.setIconified(true);
    }
}
