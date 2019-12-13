package com.drinks;

import java.util.ArrayList;
import java.util.List;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.definition.type.FactType;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class Drinks extends Application {

    private DrinksView view;
    private Scene primaryScene;
    private KieSession kSession;
    // List of fact handles
    private ArrayList<FactHandle> facts = new ArrayList<>();
    // List with strings showed on the list of answers
    private ArrayList<String> toShow = new ArrayList<>();
    // List with names of facts
    private ArrayList<String> queries = new ArrayList<>();
    // List with user's answers
    private ArrayList<Object> answers = new ArrayList<>();

    public static final void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        int windowSize = 400;
        view = new DrinksView(windowSize);
        primaryScene = new Scene(view.createMainWindow(this), windowSize, windowSize);
        primaryStage.setTitle("What Should I Drink?");
        primaryStage.setScene(primaryScene);
        primaryStage.show();
    }

    public void createDroolsSession() {
        try {
            // Clear lists
            facts.clear();
            toShow.clear(); 
            queries.clear();
            answers.clear();

            // Load up the knowledge base
            KieServices ks = KieServices.Factory.get();
            KieContainer kContainer = ks.getKieClasspathContainer();
            kSession = kContainer.newKieSession("ksession-rules");
            KieRuntimeLogger kLogger = ks.getLoggers().newFileLogger(kSession, "drinks-log");

            // Fire rules
            kSession.setGlobal("drinksApp", this);
            kSession.fireAllRules();
            kLogger.close();
        }
        catch (Throwable t) {
            System.out.println("Couldn't create Drools session");
            t.printStackTrace();
        }
    }

    public void ask(String question, String factName) {
        Parent root = view.createQuestionWindow(this, question, factName, toShow);
        primaryScene.setRoot(root);
    }

    //Creating string of results
    public void presentResults(List<Object> drinks) {
        KieBase kBase = kSession.getKieBase();
        FactType drinkType = kBase.getFactType("com.drinks.rules", "Drink");

        StringBuilder ancientWisdom = new StringBuilder();
        for (int i = 0; i < drinks.size(); i++) {
            Object x = drinks.get(i);
            ancientWisdom.append(drinkType.get(x, "name"));
            if (i < drinks.size() - 1) {
                ancientWisdom.append(" or ");
            }
        }

        Parent root = view.createResultsWindow(this, ancientWisdom.toString(), toShow);
        primaryScene.setRoot(root);
    }

    public void handleAnswer(String question, String factName, boolean isYesAnswer) {
        KieBase kBase = kSession.getKieBase();
        FactType answerEnum = kBase.getFactType("com.drinks.rules", "Answer");
        Class<? extends Enum> answerClass = answerEnum.getFactClass().asSubclass(Enum.class);
        Object yesAnswer = Enum.valueOf(answerClass, "YES");
        Object noAnswer = Enum.valueOf(answerClass, "NO");
        //Creating new fact
        FactType factType = kBase.getFactType("com.drinks.rules", "Fact");
        Object fact;
        try {
            fact = factType.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e) {
            System.out.println("Failed to instantiate fact");
            e.printStackTrace();
            return;
        }
        //Setting fact
        factType.set(fact, "name", factName);
        if (isYesAnswer) {
            factType.set(fact, "answer", yesAnswer);
            answers.add(yesAnswer);
            toShow.add(question + ", Answer: Yes");
        }
        else {
            factType.set(fact, "answer", noAnswer);
            answers.add(noAnswer);
            toShow.add(question + ", Answer: No");
        }
        //Adding fact to session, firing rules
        queries.add(factName);
        facts.add(kSession.insert(fact));
        kSession.fireAllRules();
    }

    public void handleReturnButton(ListView<String> answerList) {
        KieBase kBase = kSession.getKieBase();
        FactType factType = kBase.getFactType("com.drinks.rules", "Fact");
        Object fact;
        try {
            fact = factType.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e) {
            System.out.println("Failed to instantiate fact while returning");
            e.printStackTrace();
            return;
        }

        //System.out.println(String.valueOf(facts.size()) + "  " + String.valueOf(queries.size()) + "  "
                           //+ String.valueOf(answers.size()) + "  " + String.valueOf(toShow.size()));
        //Checking which element in list was selected
        int point = 1, pos;
        if (null != answerList.getSelectionModel().getSelectedItem()) {
            String xstr = answerList.getSelectionModel().getSelectedItem();
            pos = toShow.indexOf(xstr);
            point = facts.size() - pos;
        }
        //Removing facts up to a point specified in return, adding last not removed fact to fire rules
        if (facts.size() > point) {

            for (int i = 0; i < point; i++) {
                toShow.remove(toShow.size() - 1);
                kSession.delete(facts.get(facts.size() - 1));
                answers.remove(answers.size() - 1);
                queries.remove(queries.size() - 1);
                facts.remove(facts.size() - 1);
            }

            kSession.delete(facts.get(facts.size() - 1));
            facts.remove(facts.size() - 1);
            factType.set(fact, "name", queries.get(queries.size() - 1));
            factType.set(fact, "answer", answers.get(answers.size() - 1));

            facts.add(kSession.insert(fact));
            kSession.fireAllRules();
        }
        //Creating new session if return to first question
        else if (facts.size() == point) {
            createDroolsSession();
        }
    }
    //Coming back to start window
    public void handleReturnToStart() {
        Parent root = view.createMainWindow(this);
        primaryScene.setRoot(root);
    }

}
