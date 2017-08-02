package org.fgdbapi.thindriver.descriptionobjects;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.fgdbapi.thindriver.TableHelper;
import org.fgdbapi.thindriver.swig.FGDBJNIWrapper;
import org.fgdbapi.thindriver.swig.Geodatabase;
import org.fgdbapi.thindriver.swig.Table;
import org.fgdbapi.thindriver.xml.EsriGeometryType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TestUsingDescription {

  private Geodatabase g;
  private File gdbfile;

  @Before
  public void setup() throws Exception {

    File tempFolder = File.createTempFile("test", "folder");
    tempFolder.delete();
    tempFolder.mkdirs();

    gdbfile = new File(tempFolder, "fgdb.gdb");
    g = FGDBJNIWrapper.createGeodatabase(gdbfile.getAbsolutePath());

    System.out.println("database :" + gdbfile);

    String definition =
        TableHelper.newFeatureClass(
                "mytable",
                EsriGeometryType.ESRI_GEOMETRY_POLYGON,
                TableHelper.constructW84SpatialReference())
            .addIntegerField("integerfield")
            .buildAsString();

    Table table = g.createTable(definition, "");

    System.out.println("table is created");

    g.closeTable(table);

    // this flush the table creation
    FGDBJNIWrapper.CloseGeodatabase(g);

    System.out.println("reopen the geodatabase");
    g = FGDBJNIWrapper.openGeodatabase(gdbfile.getAbsolutePath());
  }

  @After
  public void tearDown() throws Exception {
    gdbfile.delete();
  }

  @Test
  public void testReadFieldInfo() throws Exception {

    // open the table
    Table table = g.openTable("\\mytable");

    ///////////////////////////////////////////////
    System.out.println("explore the fields infos using XML, here is the XML");

    String tableDefinition = table.getDefinition();
    System.out.println(tableDefinition);

    // parse the XML

    DocumentBuilderFactory docf = DocumentBuilderFactory.newInstance();
    DocumentBuilder docb = docf.newDocumentBuilder();
    Document doc = docb.parse(new ByteArrayInputStream(tableDefinition.getBytes("UTF-8")));

    XPathFactory xp = XPathFactory.newInstance();
    NodeList res =
        (NodeList)
            xp.newXPath()
                .evaluate(
                    "//Field[./Name/text()='SHAPE']/GeometryDef/SpatialReference/WKID",
                    doc.getDocumentElement(),
                    XPathConstants.NODESET);
    assert res.getLength() > 0;
    Node n = res.item(0);

    Node firstChild = n.getFirstChild();
    assert firstChild instanceof Node;
    System.out.println("WKID : " + firstChild.getTextContent());


  }
}
