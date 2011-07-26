package com.gmail.rretzbach.seriesguy;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gmail.rretzbach.seriesguy.model.Series;
import com.gmail.rretzbach.seriesguy.screens.ResultDialog;
import com.gmail.rretzbach.seriesguy.screens.SearchEnginesDialog;
import com.gmail.rretzbach.seriesguy.screens.SeriesDialog;
import com.gmail.rretzbach.seriesguy.services.DataService;

/**
 * Displays series in a growing grid and contains the main menu
 * 
 * @author rretzbach
 * 
 */
// TODO show that file was saved successfully
// TODO display which file was loaded
public class MainFrame extends JFrame {

    private static Logger LOG = LoggerFactory.getLogger(MainFrame.class);

    private static final long serialVersionUID = -8217370989713363969L;

    protected final class SeriesRenderer implements ListCellRenderer {

        protected SeriesRenderer() {
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            cacheButton(value, index);

            updateSelectionPresentation(index, isSelected);

            return cachedButtons.get(index);
        }

        public void cacheButton(Object value, int index) {
            if (notYetCached(index)) {
                JButton button = buildButton((Series) value);
                cachedButtons.add(index, button);
            }
        }

        public void updateSelectionPresentation(int index, boolean isSelected) {
            JButton button = cachedButtons.get(index);
            button.setSelected(isSelected);
        }

        private boolean notYetCached(int index) {
            return cachedButtons.size() <= index;
        }

        public JButton buildButton(Series series) {

            String imagePath = series.getImagePath();

            if (imagePath == null) {
                imagePath = this.getClass().getClassLoader()
                        .getResource("icons/help-icon.png").getFile();
            }

            Image image = Toolkit.getDefaultToolkit().getImage(imagePath);

            ImageIcon ti = new ImageIcon(image);
            int pref = 150;
            int w = ti.getIconWidth();
            int h = ti.getIconHeight();
            float d = Math.max(w, h);
            float f = pref / d;
            int nw = (int) (w * f);
            int nh = (int) (h * f);

            ImageIcon icon = new ImageIcon(image.getScaledInstance(nw, nh,
                    Image.SCALE_SMOOTH));

            JButton button = new JButton(series.toString(), icon);
            button.setPreferredSize(new Dimension(200, 200));
            button.setHorizontalTextPosition(SwingConstants.CENTER);
            button.setVerticalTextPosition(SwingConstants.BOTTOM);

            MouseListener popupListener = new PopupListener(
                    buildSeriesContextMenu());
            button.addMouseListener(popupListener);
            return button;
        }

    }

    protected class PopupListener extends MouseAdapter {

        protected final JPopupMenu popup;

        protected PopupListener(JPopupMenu popup) {
            this.popup = popup;
        }

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }

    }

    protected DataService dataService;

    protected SeriesDialog editSeriesDialog;

    protected SearchEnginesDialog editSearchEnginesDialog;

    protected ResultDialog queryResultDialog;

    public MainFrame() {
        super();
    }

    public void setEditSearchEnginesDialog(
            SearchEnginesDialog editSearchEnginesDialog) {
        this.editSearchEnginesDialog = editSearchEnginesDialog;
    }

    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }

    public void setEditSeriesDialog(SeriesDialog editSeriesPanel) {
        this.editSeriesDialog = editSeriesPanel;
    }

    public void setQueryResultDialog(ResultDialog queryResultDialog) {
        this.queryResultDialog = queryResultDialog;
    }

    protected JList seriesList;

    protected List<JButton> cachedButtons;

    protected JCheckBoxMenuItem disableEpisodeMenuItem;

    protected JMenuItem removeSeriesMenuItem;

    protected JMenuItem editSeriesMenuItem;

    protected DefaultListModel seriesListModel;

    protected void init() {
        setTitle("SeriesGuy 1.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setPreferredSize(new Dimension(800, 600));

        setLayout(new MigLayout("fill"));

        setJMenuBar(buildMainMenu());

        add(new JScrollPane(buildSeriesList()), "grow");

        pack();

        placeOnScreenCenter();
    }

    protected void placeOnScreenCenter() {
        double screenHeight = Toolkit.getDefaultToolkit().getScreenSize()
                .getHeight();
        double screenWidth = Toolkit.getDefaultToolkit().getScreenSize()
                .getWidth();

        setLocation(
                Double.valueOf(screenWidth / 2 - getWidth() / 2).intValue(),
                Double.valueOf(screenHeight / 2 - getHeight() / 2).intValue());
    }

    protected JMenuBar buildMainMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem loadFile = new JMenuItem("Load", KeyEvent.VK_O);
        loadFile.setDisplayedMnemonicIndex(1);
        loadFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                ActionEvent.CTRL_MASK));
        loadFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onLoad();
            }
        });
        fileMenu.add(loadFile);
        JMenuItem chooseFile = new JMenuItem("Open location...", KeyEvent.VK_L);
        chooseFile.setDisplayedMnemonicIndex(5);
        chooseFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
        chooseFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onChoose();
                onLoad();
            }
        });
        fileMenu.add(chooseFile);
        JMenuItem saveFile = new JMenuItem("Save", KeyEvent.VK_S);
        saveFile.setDisplayedMnemonicIndex(0);
        saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                ActionEvent.CTRL_MASK));
        saveFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSave();
            }
        });
        fileMenu.add(saveFile);
        fileMenu.addSeparator();
        JMenuItem exitApp = new JMenuItem("Exit", KeyEvent.VK_X);
        exitApp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                ActionEvent.CTRL_MASK));
        exitApp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onExit();
            }
        });
        fileMenu.add(exitApp);
        menuBar.add(fileMenu);

        JMenu seriesMenu = new JMenu("Series");
        JMenuItem newSeries = new JMenuItem("New", KeyEvent.VK_N);
        newSeries.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                ActionEvent.CTRL_MASK));
        newSeries.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onNewSeries();
            }
        });
        seriesMenu.add(newSeries);
        JMenuItem editSeries = new JMenuItem("Edit", KeyEvent.VK_E);
        editSeries.setDisplayedMnemonicIndex(1);
        editSeries.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onShowSeries();
            }
        });
        seriesMenu.add(editSeries);
        JMenuItem removeSeries = new JMenuItem("Delete", KeyEvent.VK_D);
        removeSeries.setDisplayedMnemonicIndex(0);
        removeSeries.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onRemoveSeries();
            }
        });

        seriesMenu.add(removeSeries);
        seriesMenu.addSeparator();
        JMenuItem markSeenSeries = new JMenuItem("Seen episode");
        markSeenSeries.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onMarkEpisodeSeen();
            }
        });
        seriesMenu.add(markSeenSeries);
        JMenuItem disableSeries = new JMenuItem("Finish series");
        disableSeries.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onToggleFinishedSeries();
            }
        });
        seriesMenu.add(disableSeries);
        menuBar.add(seriesMenu);

        JMenu searchEnginesMenu = new JMenu("Search engines");
        JMenuItem showSearchEngines = new JMenuItem("Show/Edit search engines");
        showSearchEngines.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onShowSearchEngines();
            }
        });
        searchEnginesMenu.add(showSearchEngines);
        searchEnginesMenu.addSeparator();
        JMenuItem queryWeb = new JMenuItem("Query for new episodes");
        queryWeb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onQueryNextEpisodes();
            }
        });
        searchEnginesMenu.add(queryWeb);
        menuBar.add(searchEnginesMenu);

        return menuBar;
    }

    protected void onChoose() {
        dataService.chooseSaveLocation();
    }

    public JList buildSeriesList() {
        cachedButtons = new ArrayList<JButton>();

        seriesList = new JList();

        seriesList.setDoubleBuffered(true);
        seriesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        seriesList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        seriesList.setVisibleRowCount(-1);

        seriesList.setCellRenderer(new SeriesRenderer());

        seriesListModel = new DefaultListModel();
        seriesList.setModel(seriesListModel);

        seriesList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                seriesList.setComponentPopupMenu(buildSeriesContextMenu());
            }
        });

        return seriesList;
    }

    protected void refreshSeriesListModel() {
        try {
            List<Series> series = dataService.findAllSeries();
            editSeriesDialog.updateSearchEngineListModel();
            seriesListModel.clear();
            for (Series s : series) {
                seriesListModel.addElement(s);
            }
        } catch (IOException e) {
            LOG.error("loading series failed", e);
        }
    }

    public JPopupMenu buildSeriesContextMenu() {
        boolean seriesSelected = getCurrentlySelectedSeries() != null;

        JPopupMenu popup = new JPopupMenu();
        editSeriesMenuItem = new JMenuItem("Edit");
        editSeriesMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onShowSeries();
            }
        });
        editSeriesMenuItem.setEnabled(seriesSelected);
        popup.add(editSeriesMenuItem);

        removeSeriesMenuItem = new JMenuItem("Remove");
        removeSeriesMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onRemoveSeries();
            }
        });
        popup.add(removeSeriesMenuItem);
        removeSeriesMenuItem.setEnabled(seriesSelected);

        JMenuItem seenEpisodeMenuItem = new JMenuItem("Seen");
        seenEpisodeMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onMarkEpisodeSeen();
            }
        });
        popup.add(seenEpisodeMenuItem);
        seenEpisodeMenuItem.setEnabled(seriesSelected);

        disableEpisodeMenuItem = new JCheckBoxMenuItem("Finished");
        if (seriesSelected) {
            disableEpisodeMenuItem.setSelected(getCurrentlySelectedSeries()
                    .getFinished());
        }
        disableEpisodeMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onToggleFinishedSeries();
            }
        });
        disableEpisodeMenuItem.setEnabled(seriesSelected);
        popup.add(disableEpisodeMenuItem);

        return popup;
    }

    protected Series getCurrentlySelectedSeries() {
        return (Series) seriesList.getSelectedValue();
    }

    protected void repaintSeriesList() {
        cachedButtons.clear();
        seriesList.repaint();
    }

    protected void onLoad() {
        refreshSeriesListModel();
    }

    protected void onSave() {
        try {
            dataService.save();
        } catch (IOException e) {
            LOG.error("Error while saving data", e);
        }
    }

    // TODO ask to save pending changes
    protected void onExit() {
        System.exit(0);
    }

    protected void onNewSeries() {
        try {
            Series s = dataService.createSeries();
            refreshSeriesListModel();
            repaintSeriesList();
            seriesList.setSelectedValue(s, true);
        } catch (IOException e) {
            LOG.error("Could not create new series", e);
        }

    }

    protected void onShowSeries() {
        try {
            editSeriesDialog.updateValues(getCurrentlySelectedSeries());
            editSeriesDialog.setVisible(true); // modal block
            repaintSeriesList();
        } catch (Exception e) {
            LOG.error(
                    "Series values could not be updated in the edit series screen",
                    e);
        }
    }

    protected void onRemoveSeries() {
        try {
            Series series = getCurrentlySelectedSeries();
            dataService.deleteSeries(series);
            refreshSeriesListModel();
            repaintSeriesList();
        } catch (IOException e) {
            LOG.error("Could not delete series", e);
        }
    }

    protected void onMarkEpisodeSeen() {
        getCurrentlySelectedSeries().seenCurrentEpisode();
        repaintSeriesList();
    }

    protected void onToggleFinishedSeries() {
        getCurrentlySelectedSeries().setFinished(
                !getCurrentlySelectedSeries().getFinished());
    }

    protected void onQueryNextEpisodes() {
        queryResultDialog.startSearch();
        queryResultDialog.setVisible(true);
    }

    protected void onShowSearchEngines() {
        editSearchEnginesDialog.updateSearchEngineListModel();
        editSearchEnginesDialog.setVisible(true);
    }
}
