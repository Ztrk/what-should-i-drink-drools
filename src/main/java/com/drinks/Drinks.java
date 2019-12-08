package com.drinks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.definition.type.FactType;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * This is a sample class to launch a rule.
 */
public class Drinks extends Application {
    
    private KieSession kSession;
    private ArrayList<FactHandle> facts = new ArrayList<>();
    private ArrayList<String> toShow = new ArrayList<>();

    public static final void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage arg0) throws Exception {
        System.out.println("App started");
        try {
            // load up the knowledge base
            KieServices ks = KieServices.Factory.get();
            KieContainer kContainer = ks.getKieClasspathContainer();
            kSession = kContainer.newKieSession("ksession-rules");
            KieRuntimeLogger kLogger = ks.getLoggers().newFileLogger(kSession, "drinks-log");

            // go !
            kSession.setGlobal("drinksApp", this);
            kSession.fireAllRules();
            kLogger.close();
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    
    //ArrayList<FactHandle> facts=new ArrayList<>();
    //ArrayList<String> toShow=new ArrayList<>();
    ArrayList<String> queries=new ArrayList<>();
    ArrayList<Boolean> answers=new ArrayList<>();
    public void ask(String question, String factName) throws InstantiationException, IllegalAccessException {
        // Basic preprocessing of facts and rules
        KieBase kBase = kSession.getKieBase();
        FactType answerEnum = kBase.getFactType("com.drinks.rules", "Answer");
        Class<? extends Enum> answerClass = answerEnum.getFactClass().asSubclass(Enum.class);
        Object yesAnswer = Enum.valueOf(answerClass, "YES");
        Object noAnswer = Enum.valueOf(answerClass, "NO");

        FactType factType = kBase.getFactType("com.drinks.rules", "Fact");
        Object fact = factType.newInstance();
        Object fct = factType.newInstance();
        factType.set(fact, "name", factName);
        factType.set(fact, "answer", noAnswer);       
        //Creating buttons, message and list that will be displayed
        ListView <String> answerList=new ListView<String>();
        Button buttonYes=new Button("Yes");
        Button buttonNo=new Button("No");
        Button buttonReturn=new Button("Return");
        Text message = new Text(question);
        Text message2 = new Text("Answers up to now:");
        //Constructing list of previous answers
        for (String x: toShow) {
        	answerList.getItems().add(x);
        }
        
        //Changing positions, where things will be displayed
        buttonYes.setTranslateX(-60);
        buttonNo.setTranslateX(-20);
        buttonReturn.setTranslateX(30);
        buttonYes.setTranslateY(100);
        buttonNo.setTranslateY(100);
        buttonReturn.setTranslateY(100);
        message.setTranslateY(50);
        message2.setTranslateY(-140);
        answerList.setTranslateY(-50);
        answerList.setMaxWidth(300);
        answerList.setMaxHeight(150);
        
        Stage thisStage=new Stage();
        //Events when firing buttons: yes and no
        buttonYes.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	queries.add(factName);
            	factType.set(fact, "answer", yesAnswer);
            	toShow.add(question+", Answer: Yes");
            	facts.add(kSession.insert(fact));
            	answers.add(true);
            	thisStage.close();
            }
        });
        
        buttonNo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	queries.add(factName);
            	toShow.add(question+", Answer: No");
            	facts.add(kSession.insert(fact));
            	answers.add(false);
            	thisStage.close();
            }
        });
        
        buttonReturn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	toShow.add(question+", Answer: Perhaps");
            	kSession.delete(facts.get(facts.size()-1));
            	kSession.delete(facts.get(facts.size()-2));
            	System.out.println(String.valueOf(facts.size())+"  "+String.valueOf(queries.size())+"  "+String.valueOf(answers.size()));
            	
            	Object vv;
            	if (answers.get(answers.size()-2)==true) vv=yesAnswer;
                else vv=noAnswer;
                factType.set(fct, "name", queries.get(queries.size()-2));
                factType.set(fct, "answer", vv);
                answers.remove(answers.get(answers.size()-1));
                queries.remove(queries.get(queries.size()-1));
                facts.remove(facts.get(facts.size()-1));
                
                System.out.println(String.valueOf(facts.size())+"  "+String.valueOf(queries.size())+"  "+String.valueOf(answers.size()));
            	kSession.insert(fct);
            	thisStage.close();
            }
        });
        //Creating window
        StackPane layout=new StackPane();
        layout.getChildren().add(buttonYes);
        layout.getChildren().add(buttonNo);
        layout.getChildren().add(buttonReturn);
        layout.getChildren().add(message);
        layout.getChildren().add(message2);
        layout.getChildren().add(answerList);
        Scene skene=new Scene(layout, 300, 300);
        thisStage.setTitle("Question");
        thisStage.setScene(skene);
        //Waiting for player's (?) move
        thisStage.showAndWait();
    }
    
    public void presentResults(List<Object> drinks) {
        KieBase kBase = kSession.getKieBase();
        FactType drinkType = kBase.getFactType("com.drinks.rules", "Drink");

        StringBuilder message = new StringBuilder("Drinks for you:\n");
        for (Object drink : drinks) {
            message.append("- ");
            message.append(drinkType.get(drink, "name"));
            message.append("\n");
        }
        Alert alert = new Alert(AlertType.INFORMATION, message.toString());
        alert.showAndWait();
    }

}
