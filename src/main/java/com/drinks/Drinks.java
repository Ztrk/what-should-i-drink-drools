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
    private int winSize=400;
    
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
    ArrayList<Object> answers=new ArrayList<>();
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
        buttonYes.setTranslateY(140);
        buttonNo.setTranslateY(140);
        buttonReturn.setTranslateY(140);
        message.setTranslateY(50);
        message2.setTranslateY(-170);
        answerList.setTranslateY(-50);
        answerList.setMaxWidth(winSize);
        answerList.setMaxHeight(160);
        
        Stage thisStage=new Stage();
        //Events when firing buttons: yes and no
        buttonYes.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	queries.add(factName);
            	factType.set(fact, "answer", yesAnswer);
            	toShow.add(question+", Answer: Yes");
            	facts.add(kSession.insert(fact));
            	answers.add(yesAnswer);
            	thisStage.close();
            }
        });
        
        buttonNo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	queries.add(factName);
            	toShow.add(question+", Answer: No");
            	facts.add(kSession.insert(fact));
            	answers.add(noAnswer);
            	thisStage.close();
            }
        });
        
        buttonReturn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	System.out.println(String.valueOf(facts.size())+"  "+String.valueOf(queries.size())+"  "+String.valueOf(answers.size())+"  "+String.valueOf(toShow.size()));
            	int point=1, pos;
            	if (null!=answerList.getSelectionModel().getSelectedItem()) {
            		String xstr=answerList.getSelectionModel().getSelectedItem();
            		pos=toShow.indexOf(xstr);
            		point=facts.size()-pos;
            	}
            	if (facts.size()>point) {
            	
	            	for (int i=0;i<point;i++) {
		            	toShow.remove(toShow.size()-1);
		            	kSession.delete(facts.get(facts.size()-1));
		                answers.remove(answers.size()-1);
		                queries.remove(queries.size()-1);
		                facts.remove(facts.size()-1);
	            	}
	                
	                kSession.delete(facts.get(facts.size()-1));
	                facts.remove(facts.size()-1);
	            	factType.set(fct, "name", queries.get(queries.size()-1));
	                factType.set(fct, "answer", answers.get(answers.size()-1));
	                
	                facts.add(kSession.insert(fct));
	            	thisStage.close();
            	}
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
        Scene skene=new Scene(layout, winSize, winSize);
        thisStage.setTitle("Question");
        thisStage.setScene(skene);
        //Waiting for player's (?) move
        thisStage.showAndWait();
    }
    
    public void presentResults(List<Object> drinks) throws InstantiationException, IllegalAccessException {
    	// Basic preprocessing of facts and rules
        KieBase kBase = kSession.getKieBase();
        FactType drinkType = kBase.getFactType("com.drinks.rules", "Drink");

        FactType factType = kBase.getFactType("com.drinks.rules", "Fact");
        Object fct = factType.newInstance();
        //Creating buttons, message and list that will be displayed
        ListView <String> answerList=new ListView<String>();
        Button buttonReturn=new Button("Return");
        Text message2 = new Text("Answers up to now. If You want to return to the last question, just press Return. If You want to return to even earlier question, click on it on the list and then"
        		+ " press Return");
        
        String ancientWisdom="";
        for (int i=0;i<drinks.size();i++) {
        	Object x=drinks.get(i);
        	ancientWisdom+=drinkType.get(x, "name");
        	if (i<drinks.size()-1) {
        		ancientWisdom+=" or ";
        	}
        }
        
        Text message = new Text("So, we've come to it! At last I know, that You should probably drink "+ancientWisdom+". If it doesn't suit You, You can always return!");
        //Constructing list of previous answers
        for (String x: toShow) {
        	answerList.getItems().add(x);
        }
        
        //Changing positions, where things will be displayed
        buttonReturn.setTranslateY(140);
        message.setTranslateY(70);
        message2.setTranslateY(-160);
        answerList.setTranslateY(-50);
        message.setTranslateX(20);
        message2.setTranslateX(20);
        answerList.setMaxWidth(winSize);
        answerList.setMaxHeight(160);
        
        message.setWrappingWidth(winSize-20);
        message2.setWrappingWidth(winSize-20);
        
        Stage thisStage=new Stage();
                
        buttonReturn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	System.out.println(String.valueOf(facts.size())+"  "+String.valueOf(queries.size())+"  "+String.valueOf(answers.size())+"  "+String.valueOf(toShow.size()));
            	int point=1, pos;
            	if (null!=answerList.getSelectionModel().getSelectedItem()) {
            		String xstr=answerList.getSelectionModel().getSelectedItem();
            		pos=toShow.indexOf(xstr);
            		point=facts.size()-pos;
            	}
            	if (facts.size()>point) {
            	
	            	for (int i=0;i<point;i++) {
		            	toShow.remove(toShow.size()-1);
		            	kSession.delete(facts.get(facts.size()-1));
		                answers.remove(answers.size()-1);
		                queries.remove(queries.size()-1);
		                facts.remove(facts.size()-1);
	            	}
	                
	                kSession.delete(facts.get(facts.size()-1));
	                facts.remove(facts.size()-1);
	            	factType.set(fct, "name", queries.get(queries.size()-1));
	                factType.set(fct, "answer", answers.get(answers.size()-1));
	                
	                facts.add(kSession.insert(fct));
	            	thisStage.close();
            	}
            }
        });
        
        
        //Creating window
        StackPane layout=new StackPane();
        layout.getChildren().add(buttonReturn);
        layout.getChildren().add(message);
        layout.getChildren().add(message2);
        layout.getChildren().add(answerList);
        Scene skene=new Scene(layout, winSize, winSize);
        thisStage.setTitle("Question");
        thisStage.setScene(skene);
        //Waiting for player's (?) move
        thisStage.showAndWait();
    }

}
