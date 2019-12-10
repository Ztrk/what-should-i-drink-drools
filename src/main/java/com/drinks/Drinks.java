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
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class Drinks extends Application {

    private DrinksView view;
    private KieSession kSession;
    private ArrayList<FactHandle> facts = new ArrayList<>();
    private ArrayList<String> toShow = new ArrayList<>();
    private ArrayList<String> queries = new ArrayList<>();
    private ArrayList<Object> answers = new ArrayList<>();

    public static final void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        view = new DrinksView();
        view.createMainWindow(this, primaryStage);
    }

    public void createDroolsSession() {
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
            System.out.println("Couldn't create Drools session");
            t.printStackTrace();
        }
    }

    public void ask(String question, String factName) {
        Stage thisStage = view.createQuestionWindow(this, question, factName, toShow);
        thisStage.showAndWait();
    }

    public void presentResults(List<Object> drinks) {
        // Basic preprocessing of facts and rules
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

        Stage thisStage = view.createResultsWindow(this, ancientWisdom.toString(), toShow);
        thisStage.showAndWait();
    }

    public void handleAnswer(String question, String factName, boolean isYesAnswer, Stage thisStage) {
        KieBase kBase = kSession.getKieBase();
        FactType answerEnum = kBase.getFactType("com.drinks.rules", "Answer");
        Class<? extends Enum> answerClass = answerEnum.getFactClass().asSubclass(Enum.class);
        Object yesAnswer = Enum.valueOf(answerClass, "YES");
        Object noAnswer = Enum.valueOf(answerClass, "NO");

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

        queries.add(factName);
        facts.add(kSession.insert(fact));
        thisStage.close();
    }

    public void handleReturnButton(ListView<String> answerList, Stage thisStage) {
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

        System.out.println(String.valueOf(facts.size()) + "  " + String.valueOf(queries.size()) + "  "
                           + String.valueOf(answers.size()) + "  " + String.valueOf(toShow.size()));
        int point = 1, pos;
        if (null != answerList.getSelectionModel().getSelectedItem()) {
            String xstr = answerList.getSelectionModel().getSelectedItem();
            pos = toShow.indexOf(xstr);
            point = facts.size() - pos;
        }
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
            thisStage.close();
        }
    }

}
