package de.rretzbach.seriesguy.screens;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.rretzbach.seriesguy.model.QueryResult;
import de.rretzbach.seriesguy.model.Series;
import de.rretzbach.seriesguy.services.DataService;

/**
 * Queries search engines for new episodes and displays the result. Doubleclick
 * opens the search url in default browser.
 * 
 * @author rretzbach
 * 
 */
@Component
public class ResultDialog extends JDialog {

    /**
     * Opens the clicked query result in the default browser
     * 
     * @author rretzbach
     * 
     */
    private final class DoubleClickListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent me) {
            if (me.getClickCount() > 1) {
                QueryResult selectedResult = (QueryResult) ((JList) me
                        .getSource()).getSelectedValue();
                String queryURL;
                try {
                    queryURL = selectedResult.getSeries().getSearchEngineURL(
                            selectedResult.getOffset());
                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        if (desktop.isSupported(Desktop.Action.BROWSE)) {
                            desktop.browse(new URI(queryURL));
                        }
                    }
                } catch (Exception e) {
                    LOG.debug("Series is {}", selectedResult.getSeries());
                    LOG.error(
                            "Error while trying to open a search engine url from series",
                            e);
                }
            }
        }
    }

    /**
     * Queries search engines and adds results to the list whenever a match was
     * found
     * 
     * @author rretzbach
     * 
     */
    private final class WebResultWorker extends SwingWorker<Void, QueryResult> {

        public String fetchURL(String url) throws IOException,
                ClientProtocolException {
            // log.trace("Fetching site " + url);
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(url);
            String page = httpclient.execute(httpget,
                    new ResponseHandler<String>() {

                        @Override
                        public String handleResponse(HttpResponse arg0)
                                throws ClientProtocolException, IOException {
                            HttpEntity entity = arg0.getEntity();
                            if (entity != null) {
                                return EntityUtils.toString(entity);
                            }
                            return null;
                        }

                    });

            // log.trace("Fetched site " + url);
            return page;
        }

        @Override
        protected Void doInBackground() throws Exception {
            List<Series> series = dataService.findAllSeries();
            for (Series s : series) {
                if (isCancelled()) {
                    break;
                }

                Boolean seriesShouldBeChecked = !s.getFinished();
                if (!seriesShouldBeChecked) {
                    continue;
                }

                int offset = 0;
                boolean expectsNextEpisode = true;

                while (expectsNextEpisode) {
                    statusBar.setText(s.getName() + " "
                            + (s.getLastSeenEpisode() + offset));

                    if (isCancelled()) {
                        break;
                    }

                    offset += 1;

                    String url = s.getSearchEngineURL(offset);
                    String page = fetchURL(url);

                    boolean pageCouldBeRetrieved = page != null;
                    if (!pageCouldBeRetrieved) {
                        LOG.trace("Page could not be retrieved for url {}", url);
                        expectsNextEpisode = false;
                        continue;
                    }

                    Pattern pattern = s.getRegex(offset, 25);
                    LOG.debug("Use pattern {}", pattern);

                    Matcher matcher = pattern.matcher(page);
                    boolean contentMatched = false;
                    while (matcher.find()) {
                        if (isCancelled()) {
                            break;
                        }

                        contentMatched = true;
                        String line = matcher.group();
                        publish(new QueryResult(s, offset, line));
                    }

                    if (!contentMatched) {
                        expectsNextEpisode = false;
                    }
                }
            }

            return null;
        }

        @Override
        protected void process(List<QueryResult> chunks) {
            for (QueryResult result : chunks) {
                resultListModel.addElement(result);
            }
        }

        @Override
        protected void done() {
            final String msg = "finished looking for new episodes";
            statusBar.setText(msg);
            LOG.trace(msg);
        }
    }

    private static final long serialVersionUID = 8152825070548741280L;
    private static Logger LOG = LoggerFactory.getLogger(ResultDialog.class);

    @Inject
    protected DataService dataService;

    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }

    private JLabel titleLabel;
    protected DefaultListModel resultListModel;
    protected JLabel statusBar;
    protected SwingWorker<Void, QueryResult> swingWorker;

    @PostConstruct
    protected void init() throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        setIconImage(new ImageIcon(getClass().getClassLoader().getResource(
                "icons/tv.png")).getImage());

        setModal(true);
        setLayout(new MigLayout("fill, wrap 1", "[grow, fill]"));
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setPreferredSize(new Dimension(640, 320));
        setTitle("Search results");

        add(buildTitleLabel(), "growx 0, growy 0, align center, gapy paragraph");
        add(buildList(), "grow, push, gapy paragraph");
        add(buildCancelButton(), "tag cancel, align right, growx 0, growy 0");
        add(buildStatusBar(), "dock south");

        pack();
    }

    protected JLabel buildStatusBar() {
        statusBar = new JLabel();
        return statusBar;
    }

    protected JScrollPane buildList() {
        JList list = new JList();
        list.addMouseListener(new DoubleClickListener());
        resultListModel = new DefaultListModel();
        list.setModel(resultListModel);
        return new JScrollPane(list);
    }

    protected JComponent buildTitleLabel() {
        titleLabel = new JLabel(getTitle());
        Font font = titleLabel.getFont();
        Font deriveFont = font.deriveFont(20.0f);
        titleLabel.setFont(deriveFont);
        return titleLabel;
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
        swingWorker.cancel(true);
        swingWorker = null;
        setVisible(false);
    }

    public void startSearch() {
        resultListModel.clear();
        statusBar.setText("Start looking for newer episodes");
        swingWorker = new WebResultWorker();
        swingWorker.execute();
    }
}
