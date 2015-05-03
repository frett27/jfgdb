package org.fgdbapi.thindriver.samples;

import org.fgdbapi.thindriver.TableHelper;
import org.fgdbapi.thindriver.swig.FGDBJNIWrapper;
import org.fgdbapi.thindriver.swig.Geodatabase;
import org.fgdbapi.thindriver.swig.Table;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Created by use on 13/12/2014.
 */
public class TestTables {

    private Geodatabase g;

    @Before
    public void setup() throws Exception {

        File tempFolder = File.createTempFile("test", "folder");
        tempFolder.delete();
        tempFolder.mkdirs();

        g = FGDBJNIWrapper.createGeodatabase(new File(tempFolder, "fgdb.gdb").getAbsolutePath());

    }

    @Test
    public void testCreateTable() throws Exception {

        String tableDefinition = TableHelper.newTable("matable").addStringField("c1", 40).addIntegerField("i1").buildAsString();

        Table table = g.createTable(tableDefinition, "");
        g.closeTable(table);

        FGDBJNIWrapper.closeGeodatabase2(g);


    }


}
