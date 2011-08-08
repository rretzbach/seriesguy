package de.rretzbach.seriesguy;

import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

// TODO FEATURE implement comfortable keyboard navigation
// TODO FEATURE implement dashboard screen
public class Application {

	protected MainFrame mainFrame;

	protected static final String BASEDIR = "seriesguy.basedir";

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

	public static File getBasedir() {
		String prefBasedir = Preferences.userRoot().get(BASEDIR, null);
		return prefBasedir == null ? null : new File(prefBasedir);
	}

	public static File getImagedir() {
		return new File(getBasedir(), "images");
	}

	public static void storeBasedir(File basedir) {
		Preferences userRoot = Preferences.userRoot();
		userRoot.put(BASEDIR, basedir.getAbsolutePath());
	}

}
