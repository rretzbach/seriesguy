package com.gmail.rretzbach.seriesguy;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Application {

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
                init();
            }
        });
    }

    public static void main(String[] args) {
        new Application();
    }

    protected void init() {
        ApplicationContext factory = new ClassPathXmlApplicationContext(
                "application-context.xml");

        JFrame mainFrame = (JFrame) factory.getBean("mainFrame");
        mainFrame.setVisible(true);
    }

}
