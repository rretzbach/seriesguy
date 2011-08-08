package de.rretzbach.seriesguy.screens;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.rretzbach.seriesguy.Application;
import de.rretzbach.seriesguy.model.SearchEngine;
import de.rretzbach.seriesguy.model.Series;
import de.rretzbach.seriesguy.services.MarshallingDataService;

/**
 * Add, edit or remove series
 * 
 * @author rretzbach
 * 
 */
@Component
// TODO Save images relatively only
// TODO Show image path as normal text property
// FIXME dropping images on mac
// FIXME dropping image urls on mac
public class SeriesDialog extends JDialog {

	/**
	 * Handles image url text drops by downloading the image and assigning it to
	 * the series
	 * 
	 * @author rretzbach
	 * 
	 */
	public final class ImageDropHandler extends TransferHandler {
		private static final long serialVersionUID = -7238569708459551744L;

		@Override
		public int getSourceActions(JComponent c) {
			return MOVE;
		}

		@Override
		// TODO check for url as text
		// TODO should also import local images
		public boolean canImport(TransferSupport support) {
			if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				return false;
			}

			return true;
		}

		@Override
		public boolean importData(TransferSupport support) {
			if (!canImport(support)) {
				return false;
			}

			Transferable t = support.getTransferable();
			String data = null;
			try {
				data = (String) t.getTransferData(DataFlavor.stringFlavor);
				String imagePath2 = downloadImage(data);
				File file = new File(imagePath2);
				imagePath = FilenameUtils.getName(file.getAbsolutePath());
				((JLabel) support.getComponent()).setIcon(new ImageIcon(ImageIO
						.read(file)));

			} catch (Exception e) {
				// TODO error should be presented to the user
				// that applies to more cases
				LOG.debug("String data is {}", data);
				LOG.error("Error while dropping data onto image", e);
			}

			return true;
		}

		/**
		 * Creates images directory if it doesn't exist
		 * 
		 * @param url
		 * @return
		 */
		protected String downloadImage(String url) {
			try {

				URL url2 = new URI(url).toURL();
				String output = buildLocalImagePath(url2, Application
						.getImagedir().getAbsolutePath(), series.getName());

				FileUtils.copyURLToFile(url2, new File(output));

				return output;
			} catch (Exception e) {
				LOG.error("Error while downloading image", e);
			}

			return null;
		}

		public String buildLocalImagePath(URL url, String imageDirectoryPath,
				String seriesName) throws IOException {
			String name = url.getFile();
			String ext = "";
			if (name.matches("(?i).*jpe?g")) {
				ext = "jpg";
			} else if (name.matches("(?i).*png")) {
				ext = "png";
			} else if (name.matches("(?i).*gif")) {
				ext = "gif";
			}

			String output = new File(imageDirectoryPath, seriesName + "." + ext)
					.getAbsolutePath();
			return output;
		}
	}

	private static final long serialVersionUID = 8152825070548741280L;
	private static Logger LOG = LoggerFactory.getLogger(SeriesDialog.class);

	@Inject
	protected MarshallingDataService dataService;

	public void setDataService(MarshallingDataService dataService) {
		this.dataService = dataService;
	}

	protected Series series;
	protected String imagePath;
	protected Map<String, JComponent> formElements;
	protected JLabel titleLabel;
	protected JComboBox searchEngineComboBox;
	protected JLabel picLabel;

	@PostConstruct
	protected void init() throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, IOException {
		setModal(true);
		setLayout(new MigLayout("fillx, wrap 2", "[label]rel[grow, fill]"));
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setMinimumSize(new Dimension(640, 480));

		formElements = new HashMap<String, JComponent>();

		add(buildTitle(), "span 2, growx 0, align center, gapy paragraph");
		addImagePathProperty("Image:", "imagePath");
		addTextProperty("Name:", "name", "gapy paragraph");
		addTextProperty("Season:", "season");
		addTextProperty("Last seen episode:", "lastSeenEpisode");
		addTextProperty("Episode format:", "episodeFormat");
		addTextProperty("Search text:", "searchText");
		addTextProperty("Match pattern:", "regex");
		addSearchEngineComboBox("Search engine", "searchEngine");
		addBooleanProperty("Finished:", "finished");
		add(buildSaveButton(), "tag apply, span 2, split 2");
		add(buildCancelButton(), "tag cancel");

		pack();
	}

	protected JLabel buildTitle() {
		titleLabel = new JLabel(getTitle());
		Font font = titleLabel.getFont();
		Font deriveFont = font.deriveFont(20.0f);
		titleLabel.setFont(deriveFont);
		return titleLabel;
	}

	private JButton buildSaveButton() {
		JButton button = new JButton("Save");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onSave();
			}
		});
		return button;
	}

	// TODO validate *before* setting the values
	protected void onSave() {
		try {
			for (Entry<String, JComponent> entry : formElements.entrySet()) {
				String property = entry.getKey();
				JComponent comp = entry.getValue();
				LOG.trace("Write property {}", property);
				if (comp instanceof JComboBox) {
					PropertyUtils.setProperty(series, property,
							((JComboBox) comp).getSelectedItem());
				} else if (comp instanceof JCheckBox) {
					PropertyUtils.setProperty(series, property,
							((JCheckBox) comp).isSelected());
				} else if (comp instanceof JTextField) {
					if (PropertyUtils.getProperty(series, property) instanceof Integer) {
						String text = ((JTextField) comp).getText();
						int parseInt = Integer.parseInt(text);
						PropertyUtils.setProperty(series, property, parseInt);
					} else {
						PropertyUtils.setProperty(series, property,
								((JTextField) comp).getText());
					}
				}
			}

			series.setImagePath(imagePath);

			series.validate();

			setVisible(false);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(),
					"Validation failed", JOptionPane.WARNING_MESSAGE);
			LOG.error("Error while writing series properties", e);
		}

	}

	private JButton buildCancelButton() {
		JButton button = new JButton("Cancel");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		return button;
	}

	protected void onCancel() {
		setVisible(false);
	}

	public void updateValues(Series series, List<SearchEngine> searchEngines)
			throws Exception {
		this.series = series;

		setTitle(series.getName());
		buildTitle().setText(getTitle());

		for (Entry<String, JComponent> entry : formElements.entrySet()) {
			String property = entry.getKey();
			JComponent comp = entry.getValue();
			if (comp instanceof JComboBox) {
				LOG.debug("comp is {}",
						PropertyUtils.getProperty(series, property));
				((JComboBox) comp).setSelectedItem(PropertyUtils.getProperty(
						series, property));
			} else if (comp instanceof JCheckBox) {
				((JCheckBox) comp).setSelected((Boolean) PropertyUtils
						.getProperty(series, property));
			} else if (comp instanceof JTextField) {
				LOG.debug("Tried to get {} from {}", property, series);
				((JTextField) comp).setText(BeanUtils.getProperty(series,
						property));
			}
		}

		if (series.getImagePath() != null) {
			imagePath = series.getImagePath();
			picLabel.setIcon(new ImageIcon(imagePath));
			picLabel.setAlignmentY(0.5f);
		} else {
			picLabel.setIcon(new ImageIcon(getClass().getClassLoader()
					.getResource("icons/help-icon.png")));

		}

		updateSearchEngineListModel(searchEngines);

		repaint();

		pack();
	}

	protected void addTextProperty(String label, String property,
			String layoutConstraints) {
		JComponent component = buildTextProperty(label, property);
		add(component, layoutConstraints);
	}

	protected void addTextProperty(String label, String property) {
		JComponent component = buildTextProperty(label, property);
		add(component);
	}

	protected JComponent buildTextProperty(String label, String property) {
		add(new JLabel(label));

		JTextField textField = new JTextField();
		formElements.put(property, textField);
		return textField;
	}

	protected void addBooleanProperty(String label, String property)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		add(new JLabel(label));

		JCheckBox checkbox = new JCheckBox();
		formElements.put(property, checkbox);
		add(checkbox, "growx 0, align center");
	}

	protected void addSearchEngineComboBox(String label, String property) {
		add(new JLabel(label));

		searchEngineComboBox = new JComboBox();

		formElements.put(property, searchEngineComboBox);
		add(searchEngineComboBox);
	}

	protected void addImagePathProperty(String label, String property)
			throws IOException {
		add(new JLabel(label));

		picLabel = new JLabel();
		picLabel.setHorizontalAlignment(SwingConstants.CENTER);
		TransferHandler newHandler = new ImageDropHandler();
		picLabel.setTransferHandler(newHandler);
		add(picLabel);
	}

	public void updateSearchEngineListModel(List<SearchEngine> searchEngines) {
		DefaultComboBoxModel model = prepareSearchEngineListModel(searchEngines);
		searchEngineComboBox.setModel(model);
	}

	public DefaultComboBoxModel prepareSearchEngineListModel(
			List<SearchEngine> searchEngines) {
		DefaultComboBoxModel model = new DefaultComboBoxModel();

		for (SearchEngine searchEngine : searchEngines) {
			model.addElement(searchEngine);
		}
		return model;
	}

}
