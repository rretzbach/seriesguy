package com.gmail.rretzbach.seriesguy.screens;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.gmail.rretzbach.seriesguy.model.SearchEngine;
import com.gmail.rretzbach.seriesguy.services.DataService;

/**
 * Add, edit or remove search engines
 * 
 * @author rretzbach
 * 
 */
@Component
public class SearchEnginesDialog extends JDialog {

	private static final long serialVersionUID = 8994530590146294695L;

	private static Logger LOG = LoggerFactory
			.getLogger(SearchEnginesDialog.class);

	@Inject
	protected DataService dataService;

	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}

	protected Map<String, JTextField> formElements;
	private JLabel titleLabel;

	protected List<SearchEngine> searchEngines;

	protected JTextField nameField;

	protected JTextField urlField;

	protected JList searchEngineList;

	@PostConstruct
	protected void init() throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		setIconImage(new ImageIcon(getClass().getClassLoader().getResource(
				"icons/tv.png")).getImage());

		setModal(true);
		setLayout(new MigLayout("fill, wrap 4",
				"[15%][15%]rel[label]rel[grow, fill]"));
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setPreferredSize(new Dimension(640, 320));

		formElements = new HashMap<String, JTextField>();

		setTitle("Search engines");

		titleLabel = new JLabel(getTitle());
		Font font = titleLabel.getFont();
		Font deriveFont = font.deriveFont(20.0f);
		titleLabel.setFont(deriveFont);
		add(titleLabel, "span 4, growx 0, align center, gapy paragraph");

		searchEngineList = new JList();
		searchEngineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		searchEngineList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				final SearchEngine searchEngine = getCurrentlySelectedSearchEngine();

				boolean searchEngineSelected = searchEngine != null;
				nameField.setEnabled(searchEngineSelected);
				urlField.setEnabled(searchEngineSelected);

				if (searchEngineSelected) {
					nameField.setText(searchEngine.getName());
					urlField.setText(searchEngine.getUrlTemplate());
				}
			}
		});

		add(searchEngineList, "span 2 2, grow");

		add(new JLabel("Name:"));
		nameField = new JTextField();
		nameField.setEnabled(false);
		add(nameField);
		nameField.addKeyListener(new KeyAdapter() {
			public void keyReleased(java.awt.event.KeyEvent e) {
				getCurrentlySelectedSearchEngine().setName(
						((JTextField) e.getSource()).getText());
			}

		});

		add(new JLabel("URL:"));
		urlField = new JTextField();
		urlField.setEnabled(false);
		add(urlField);
		urlField.addKeyListener(new KeyAdapter() {
			public void keyReleased(java.awt.event.KeyEvent e) {
				getCurrentlySelectedSearchEngine().setUrlTemplate(
						((JTextField) e.getSource()).getText());
			};
		});

		JButton newButton = new JButton("New");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onNew();
			}
		});
		add(newButton, "sizegroup button, align center");
		JButton removeButton = new JButton("Remove");
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onRemove();
			}
		});
		add(removeButton, "sizegroup button, align center");

		add(buildSaveButton(),
				"sizegroup button, tag apply, span 4, split 2, newline");
		add(buildCancelButton(), "sizegroup button, tag cancel");

		pack();
	}

	public void updateSearchEngineListModel() {
		searchEngines = new ArrayList<SearchEngine>(
				dataService.findAllSearchEngines());
		DefaultListModel model = new DefaultListModel();
		for (SearchEngine searchEngine : searchEngines) {
			model.addElement(searchEngine);
		}
		searchEngineList.setModel(model);
	}

	protected void onRemove() {
		dataService.deleteSearchEngine(getCurrentlySelectedSearchEngine());
		updateSearchEngineListModel();
	}

	protected void onNew() {
		SearchEngine createSearchEngine = dataService.createSearchEngine();
		updateSearchEngineListModel();
		boolean shouldScroll = true;
		searchEngineList.setSelectedValue(createSearchEngine, shouldScroll);
	}

	protected SearchEngine getCurrentlySelectedSearchEngine() {
		return (SearchEngine) searchEngineList.getSelectedValue();
	};

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

	protected void onSave() {
		try {
			setVisible(false);
		} catch (Exception e) {
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

}
