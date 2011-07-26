package com.gmail.rretzbach.seriesguy;

import java.net.URL;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.gmail.rretzbach.seriesguy.screens.SeriesDialog;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
    /**
     * Create the test case
     * 
     * @param testName
     *            name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    public void testImageExtensionsShouldBeCaseInsensitive() throws Exception {
        SeriesDialog seriesDialog = new SeriesDialog();
        SeriesDialog.ImageDropHandler imageDropHandler = seriesDialog.new ImageDropHandler();

        {
            String url = "http://host.domain.tld/myimage.PNG";
            String expected = "C:\\temp\\Untitled.png";
            String actual = imageDropHandler.buildLocalImagePath(new URL(url),
                    "C:\\temp\\", "Untitled");
            Assert.assertEquals(expected, actual);
        }
        {
            String url = "http://host.domain.tld/myimage.giF";
            String expected = "C:\\temp\\untitled.gif";
            String actual = imageDropHandler.buildLocalImagePath(new URL(url),
                    "C:\\temp\\", "untitled");
            Assert.assertEquals(expected, actual);
        }
        {
            String url = "http://host.domain.tld/myimage.jpeg";
            String expected = "C:\\temp\\Untitled.jpg";
            String actual = imageDropHandler.buildLocalImagePath(new URL(url),
                    "C:\\temp\\", "Untitled");
            Assert.assertEquals(expected, actual);
        }

    }
}
