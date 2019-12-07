package com.drinks;

import java.util.List;
import java.util.Optional;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.definition.type.FactType;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * This is a sample class to launch a rule.
 */
public class Drinks extends Application {
    
    private KieSession kSession;

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
    
    public void ask(String question, String factName) throws InstantiationException, IllegalAccessException {
        KieBase kBase = kSession.getKieBase();
        FactType answerEnum = kBase.getFactType("com.drinks.rules", "Answer");
        Class<? extends Enum> answerClass = answerEnum.getFactClass().asSubclass(Enum.class);
        Object yesAnswer = Enum.valueOf(answerClass, "YES");
        Object noAnswer = Enum.valueOf(answerClass, "NO");

        FactType factType = kBase.getFactType("com.drinks.rules", "Fact");
        Object fact = factType.newInstance();
        factType.set(fact, "name", factName);
        factType.set(fact, "answer", noAnswer);

        Alert alert = new Alert(AlertType.CONFIRMATION, question, ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            factType.set(fact, "answer", yesAnswer);
        }
        kSession.insert(fact);
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
