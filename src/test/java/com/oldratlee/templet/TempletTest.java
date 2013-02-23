package com.oldratlee.templet;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Jerry Lee (oldratlee AT gmail DOT com)
 */
@RunWith(Parameterized.class)
public class TempletTest {

    static Map<String, Object> context = new HashMap<String, Object>();

    static {
        context.put("con_true", true);
        context.put("con_false", false);

        List<Integer> coll = new ArrayList<Integer>();
        coll.add(1);
        coll.add(2);
        coll.add(3);
        context.put("coll", coll);

        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v2");
        context.put("map", map);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> prepareData() throws Exception {
        final List<Object[]> retTestData = new ArrayList<Object[]>();

        File directory = new File(TempletTest.class.getResource("./").getFile());
        File[] files = directory.listFiles();
        assertNotNull(files);
        for (File f : files) {
            if(f.getName().endsWith("templet")){
                retTestData.add(new Object[]{f.getName()});
            }
        }
        return retTestData;
    }

    public TempletTest(String fileName) {
        this.fileName = fileName;
    }

    String fileName;

    @Test
    public void test_render() throws IOException {
        System.out.println("Running templet " + fileName);

        InputStream inputStream = TempletTest.class.getResourceAsStream(fileName);
        InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");

        StringWriter writer = new StringWriter();
        Templet.render(reader, context, writer);

        String expected = IOUtils.toString(TempletTest.class.getResourceAsStream(fileName + ".txt"), "UTF-8");
        assertEquals(expected, writer.toString());
    }
}
