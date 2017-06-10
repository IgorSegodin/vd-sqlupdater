package de.vdata.sqlupdater.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

/**
 * @author isegodin
 */
public class PropertiesWrapperTest {

    private Properties properties;

    @Before
    public void before() {
        properties = new Properties();
        properties.put("emptyProperty", "");
        properties.put("blankProperty", "    ");
        properties.put("withSpacesProperty", "   some value    ");
        properties.put("normalProperty", "value");
    }

    @Test
    public void testGet() {
        PropertiesWrapper pw = new PropertiesWrapper(properties);
        Assert.assertEquals(null, pw.get("emptyProperty"));
        Assert.assertEquals(null, pw.get("blankProperty"));
        Assert.assertEquals("some value", pw.get("withSpacesProperty"));

        /* test no error */
        pw.get("nonExistentProperty");
    }

    @Test
    public void testGetDefault() {
        PropertiesWrapper pw = new PropertiesWrapper(properties);
        Assert.assertEquals("default", pw.get("emptyProperty", "default"));
        Assert.assertEquals("default", pw.get("blankProperty", "default"));
        Assert.assertEquals("some value", pw.get("withSpacesProperty", "default"));

        Assert.assertEquals("default", pw.get("nonExistentProperty", "default"));
    }

    @Test(expected = RuntimeException.class)
    public void testGetStrictEmpty() {
        PropertiesWrapper pw = new PropertiesWrapper(properties);
        pw.getStrict("emptyProperty");
    }

    @Test(expected = RuntimeException.class)
    public void testGetStrictBlank() {
        PropertiesWrapper pw = new PropertiesWrapper(properties);
        pw.getStrict("blankProperty");
    }

    @Test(expected = RuntimeException.class)
    public void testGetStrictNonExistent() {
        PropertiesWrapper pw = new PropertiesWrapper(properties);
        pw.getStrict("nonExistentProperty");
    }


}
