package com.drinks;

import java.util.List;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class DrinksView {

    private int winSize;
    
    public DrinksView(int windowSize) {
        winSize = windowSize;
    }

    public Parent createMainWindow(Drinks controller) {

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setVgap(10);
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setHalignment(HPos.CENTER);
        gridPane.getColumnConstraints().add(columnConstraints);

        Label welcomeText = new Label("Welcome to our application. To start press Start button.");
        welcomeText.setWrapText(true);
        gridPane.add(welcomeText, 0, 0);
        Button startButton = new Button("Start");

        startButton.setOnAction(event -> controller.createDroolsSession());
        gridPane.add(startButton, 0, 1);
        return gridPane;
    }

    public Parent createQuestionWindow(Drinks controller, String question, String factName, List<String> previousAnswers) {
        ListView<String> answerList = new ListView<String>();
        Button buttonYes = new Button("Yes");
        Button buttonNo = new Button("No");
        Button buttonReturn = new Button("Return");
        Text message = new Text(question);
        Text message2 = new Text("Answers up to now:");
        // Constructing list of previous answers
        for (String s : previousAnswers) {
            answerList.getItems().add(s);
        }

        buttonYes.setOnAction(event -> controller.handleAnswer(question, factName, true));
        buttonNo.setOnAction(event -> controller.handleAnswer(question, factName, false));
        buttonReturn.setOnAction(event -> controller.handleReturnButton(answerList));

        // Creating window
        GridPane layout = new GridPane();
        layout.setAlignment(Pos.CENTER);
        layout.setVgap(10);
        layout.setHgap(10);
        layout.setPadding(new Insets(20));

        layout.add(message2, 0, 0);
        layout.add(answerList, 0, 1);
        layout.add(message, 0, 2);

        HBox buttonsBox = new HBox(10, buttonYes, buttonNo, buttonReturn);
        buttonsBox.setAlignment(Pos.BASELINE_RIGHT);
        layout.add(buttonsBox, 0, 3);

        return layout;
    }

    public Parent createResultsWindow(Drinks controller, String results, List<String> previousAnswers) {
        // Creating buttons, message and list that will be displayed
        ListView<String> answerList = new ListView<String>();
        Button buttonReturn = new Button("Return");
        Text message2 = new Text("Answers up to now. If you want to return to the last question, just press Return."
                                  + " If you want to return to even earlier question, click on it on the list and then press Return");

        Text message = new Text("So, we've come to it! At last I know, that you should probably drink " + results
                                + ". If it doesn't suit you, you can always return!");
        // Constructing list of previous answers
        for (String s : previousAnswers) {
            answerList.getItems().add(s);
        }

        message.setWrappingWidth(winSize - 20);
        message2.setWrappingWidth(winSize - 20);

        buttonReturn.setOnAction(event -> controller.handleReturnButton(answerList));

        // Creating window
        GridPane layout = new GridPane();
        layout.setAlignment(Pos.CENTER);
        layout.setVgap(10);
        layout.setHgap(10);
        layout.setPadding(new Insets(20));

        layout.add(message2, 0, 0);
        layout.add(answerList, 0, 1);
        layout.add(message, 0, 2);
        layout.add(buttonReturn, 0, 3);
        GridPane.setHalignment(buttonReturn, HPos.RIGHT);

        return layout;
    }
}
