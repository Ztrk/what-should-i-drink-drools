package com.drinks;

import javax.swing.JOptionPane;

import org.kie.api.KieServices;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * This is a sample class to launch a rule.
 */
public class Drinks extends Application {

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
            KieSession kSession = kContainer.newKieSession("ksession-rules");
            KieRuntimeLogger kLogger = ks.getLoggers().newFileLogger(kSession, "drinks-log");

            // go !
            kSession.fireAllRules();
            kLogger.close();
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static final boolean ask(String question) {
        int answer = JOptionPane.showConfirmDialog(null, question, "Select an Option", JOptionPane.YES_NO_OPTION);
        return answer == 0;
    }

}
