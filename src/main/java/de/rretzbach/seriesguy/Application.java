package de.rretzbach.seriesguy;

import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

// TODO FEATURE implement comfortable keyboard navigation
// TODO FEATURE implement dashboard screen
public class Application {

    protected MainFrame mainFrame;

    private static final Logger LOG = LoggerFactory
            .getLogger(Application.class);

    public Application() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager
                            .getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    LOG.error("Error while setting platform look and feel", e);
                }
                initApplicationContext();
            }
        });
    }

    public static void main(String[] args) {
        new Application();
    }

    protected void initApplicationContext() {
        ApplicationContext factory = new AnnotationConfigApplicationContext(
                "de.rretzbach.seriesguy");

        mainFrame = factory.getBean(MainFrame.class);
        mainFrame.setVisible(true);
    }

}
