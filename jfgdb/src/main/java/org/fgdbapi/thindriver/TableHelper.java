package org.fgdbapi.thindriver;

import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import org.fgdbapi.thindriver.tools.Tools;
import org.fgdbapi.thindriver.xml.ArrayOfControllerMembership;
import org.fgdbapi.thindriver.xml.ArrayOfField;
import org.fgdbapi.thindriver.xml.ArrayOfIndex;
import org.fgdbapi.thindriver.xml.ArrayOfPropertySetProperty;
import org.fgdbapi.thindriver.xml.DEFeatureClass;
import org.fgdbapi.thindriver.xml.DETable;
import org.fgdbapi.thindriver.xml.DataElement;
import org.fgdbapi.thindriver.xml.EnvelopeN;
import org.fgdbapi.thindriver.xml.EsriDatasetType;
import org.fgdbapi.thindriver.xml.EsriFeatureType;
import org.fgdbapi.thindriver.xml.EsriFieldType;
import org.fgdbapi.thindriver.xml.EsriGeometryType;
import org.fgdbapi.thindriver.xml.Field;
import org.fgdbapi.thindriver.xml.Fields;
import org.fgdbapi.thindriver.xml.GeographicCoordinateSystem;
import org.fgdbapi.thindriver.xml.GeometryDef;
import org.fgdbapi.thindriver.xml.Index;
import org.fgdbapi.thindriver.xml.Indexes;
import org.fgdbapi.thindriver.xml.Names;
import org.fgdbapi.thindriver.xml.ObjectFactory;
import org.fgdbapi.thindriver.xml.PropertySet;
import org.fgdbapi.thindriver.xml.SpatialReference;

/**
 * Table / Featureclass Description helper, for simplify the structure creation
 *
 * @author pfreydiere
 */
public class TableHelper {

  /** inner dataElement handled by this table helper */
  private DETable dataElement;

  /** name of the table */
  private String name;

  /**
   * statically create a tablehelper for creating a new table
   *
   * @param name
   * @return
   */
  public static TableHelper newTable(String name) {

    TableHelper th = new TableHelper(name);

    Field o = new Field();
    o.setName("OBJECTID");
    o.setType(EsriFieldType.ESRI_FIELD_TYPE_OID);
    o.setLength(4);
    o.setRequired(true);
    o.setIsNullable(false);
    o.setEditable(false);
    o.setAliasName(o.getName());
    o.setModelName(o.getName());

    Fields flds = new Fields();

    flds.setFieldArray(new ArrayOfField());
    flds.getFieldArray().getField().add(o); // adding objectid

    Index oid = new Index();
    oid.setName("OIDINDEX");

    Fields sfields = new Fields();
    ArrayOfField indexsarrayOfField = new ArrayOfField();
    sfields.setFieldArray(indexsarrayOfField);
    indexsarrayOfField.getField().add(o);

    oid.setFields(sfields);
    oid.setIsUnique(true);
    oid.setIsAscending(true);

    ArrayOfIndex arrayOfIndex = new ArrayOfIndex();
    arrayOfIndex.getIndex().add(oid);

    th.dataElement = new DETable();

    Indexes indices = new Indexes();
    indices.setIndexArray(arrayOfIndex);
    th.dataElement.setIndexes(indices);

    // th.dataElement.setCatalogPath("/FC=" + name);
    th.dataElement.setCatalogPath("\\" + name);
    th.dataElement.setName(name);
    th.dataElement.setChildrenExpanded(false);
    th.dataElement.setOIDFieldName("OBJECTID");
    th.dataElement.setHasOID(true);
    th.dataElement.setDatasetType(EsriDatasetType.ESRI_DT_TABLE);
    th.dataElement.setVersioned(false);
    th.dataElement.setControllerMemberships(new ArrayOfControllerMembership());

    // to be adjusted
    th.dataElement.setCLSID("{7A566981-C114-11D2-8A28-006097AFF44E}");

    th.dataElement.setModelName(name);
    th.dataElement.setAliasName(name);

    th.dataElement.setFields(flds);

    return th;
  }

  /**
   * create a new FeatureClass description
   *
   * @param name table name
   * @param geomType the geometry type
   * @param gcs the coordinate system definition
   * @return the table helper
   */
  public static TableHelper newFeatureClass(
      String name, EsriGeometryType geomType, SpatialReference gcs) throws Exception {

    assert name != null && !name.isEmpty();
    assert gcs != null;

    TableHelper th = newTable(name);

    DEFeatureClass fc = new DEFeatureClass();

    // grab all the informations from the DETable to the DEFeatureClass
    Tools.copy(th.dataElement, fc);

    th.dataElement = fc; // replace the dataElement

    Field s = new Field();
    s.setName("SHAPE");
    s.setType(EsriFieldType.ESRI_FIELD_TYPE_GEOMETRY);
    s.setRequired(true);
    s.setIsNullable(true);
    s.setAliasName("SHAPE");
    s.setModelName("SHAPE");
    s.setEditable(true);

    GeometryDef geom = new GeometryDef();
    geom.setGeometryType(geomType);
    geom.setSpatialReference(gcs);
    geom.setGridSize0((double) 0);

    geom.setHasM(false);
    geom.setHasZ(false);

    s.setGeometryDef(geom);

    Fields flds = th.dataElement.getFields();
    flds.getFieldArray().getField().add(s);

    // Index sindex = new Index();
    // sindex.setName("FDO_SHAPE");
    // Fields shapeIndexFields = flds;
    // ArrayOfField shapeIndexArrayOfFields = new ArrayOfField();
    // shapeIndexArrayOfFields.getField().add(s);
    // shapeIndexFields.setFieldArray(shapeIndexArrayOfFields);
    // sindex.setFields(shapeIndexFields);
    // sindex.setIsAscending(true);
    // sindex.setIsUnique(false);
    //
    // fc.getIndexes().getIndexArray().getIndex().add(sindex);

    EnvelopeN e = new EnvelopeN();
    e.setXMin(-180);
    e.setXMax(180);
    e.setYMin(-90);
    e.setYMax(90);
    fc.setExtent(e);

    fc.setFeatureType(EsriFeatureType.ESRI_FT_SIMPLE);
    fc.setShapeType(geomType);

    fc.setRelationshipClassNames(new Names());
    fc.setShapeFieldName("SHAPE");
    fc.setHasM(false);
    fc.setHasZ(false);
    fc.setDatasetType(EsriDatasetType.ESRI_DT_FEATURE_CLASS);
    fc.setCLSID("{52353152-891A-11D0-BEC6-00805F7C4268}"); // simple
    // features

    fc.setHasSpatialIndex(false);

    fc.setSpatialReference(gcs);

    return th;
  }

  /**
   * construct the XML definition of the table
   *
   * @return the XML DataElement definition
   */
  public DETable build() {
    PropertySet ps = new PropertySet();
    dataElement.setExtensionProperties(ps);
    ps.setPropertyArray(new ArrayOfPropertySetProperty());
    return dataElement;
  }

  /**
   * construct the XML definition in a string
   *
   * @return the serialized string
   * @throws Exception
   */
  public String buildAsString() throws Exception {
    return serializeElement(build());
  }

  /**
   * get the table / featureclass name
   *
   * @return the name of the table / feature class
   */
  public String getName() {
    return this.name;
  }

  // ////////////////////////////////
  /**
   * @param de data element table
   * @return the serialized string of the xml
   * @throws Exception
   */
  protected static String serializeElement(DETable de) throws Exception {

    // FGDB API don't like the non "esri" prefix
    class NP extends NamespacePrefixMapper {
      @Override
      public String getPreferredPrefix(String arg0, String arg1, boolean arg2) {
        if (arg0.equals("http://www.esri.com/schemas/ArcGIS/10.1")) return "esri";
        if (arg0.equals("http://www.w3.org/2001/XMLSchema-instance")) return "xsi";
        return null;
      }
    }

    JAXBContext isn = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
    Marshaller m = isn.createMarshaller();
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", new NP());

    StringWriter sw = new StringWriter();
    m.marshal(
        new JAXBElement<DataElement>(
            new QName("http://www.esri.com/schemas/ArcGIS/10.1", "DataElement"),
            DataElement.class,
            de),
        sw);

    String rawResult = sw.getBuffer().toString();

    // event for empty values, we must put the xsi:type
    // this should not be here ... but not taken into consideration in the
    // Jaxb implementation

    rawResult =
        rawResult.replace(
            "<ExtensionProperties", "<ExtensionProperties xsi:type=\"esri:PropertySet\"");
    rawResult =
        rawResult.replace(
            "<PropertyArray", "<PropertyArray xsi:type=\"esri:ArrayOfPropertySetProperty\"");
    rawResult =
        rawResult.replace(
            "<ControllerMemberships",
            "<ControllerMemberships xsi:type=\"esri:ArrayOfControllerMembership\"");
    rawResult = rawResult.replace("<Fields", "<Fields xsi:type=\"esri:Fields\"");
    rawResult = rawResult.replace("<FieldArray", "<FieldArray xsi:type=\"esri:ArrayOfField\"");
    rawResult = rawResult.replace("<Field ", "<Field xsi:type=\"esri:Field\" ");
    rawResult = rawResult.replace("<Field>", "<Field xsi:type=\"esri:Field\">");

    rawResult = rawResult.replace("<GeometryDef", "<GeometryDef xsi:type=\"esri:GeometryDef\"");
    rawResult = rawResult.replace("<Indexes", "<Indexes xsi:type=\"esri:Indexes\"");
    rawResult = rawResult.replace("<IndexArray", "<IndexArray xsi:type=\"esri:ArrayOfIndex\"");
    rawResult = rawResult.replace("<Index>", "<Index xsi:type=\"esri:Index\">");

    rawResult =
        rawResult.replace(
            "<RelationshipClassNames", "<RelationshipClassNames xsi:type=\"esri:Names\"");

    return rawResult;
  }

  TableHelper(String name) {
    this.name = name;
  }

  /**
   * add a field to the table
   *
   * @param f the new field
   * @return the table helper with the new field included
   */
  public TableHelper addField(Field f) {
    dataElement.getFields().getFieldArray().getField().add(f);
    return this;
  }

  /**
   * add a default long field
   *
   * @param name the integer field name
   * @return the table helper
   */
  public TableHelper addLongField(String name) {

    Field f = new Field();
    f.setName(name);
    f.setType(EsriFieldType.ESRI_FIELD_TYPE_INTEGER);
    f.setLength(8);
    f.setEditable(true);
    f.setIsNullable(true);

    return addField(f);
  }

  /**
   * add a default integer field
   *
   * @param name the integer field name
   * @return the table helper
   */
  public TableHelper addIntegerField(String name) {
    return addIntegerField(name, 4);
  }

  /**
   * add a default integer field
   *
   * @param name the integer field name
   * @return the table helper
   */
  public TableHelper addIntegerField(String name, int size) {

    Field f = new Field();
    f.setName(name);
    f.setType(EsriFieldType.ESRI_FIELD_TYPE_INTEGER);
    f.setLength(size);
    f.setEditable(true);
    f.setIsNullable(true);

    return addField(f);
  }

  /**
   * Add a default string field on the table
   *
   * @param name
   * @param length
   * @return the table helper
   */
  public TableHelper addStringField(String name, int length) {

    Field f = new Field();
    f.setName(name);
    f.setType(EsriFieldType.ESRI_FIELD_TYPE_STRING);
    f.setLength(length);
    f.setEditable(true);
    f.setIsNullable(true);

    return addField(f);
  }

  /**
   * add a double field
   *
   * @param name
   * @return the table helper
   */
  public TableHelper addDoubleField(String name) {
    Field f = new Field();
    f.setName(name);
    f.setType(EsriFieldType.ESRI_FIELD_TYPE_DOUBLE);
    f.setLength(8);
    f.setEditable(true);
    f.setIsNullable(true);
    return addField(f);
  }

  /**
   * construct the W84 system reference
   *
   * @return the WGS84 geographic coordinate system
   */
  public static GeographicCoordinateSystem constructW84SpatialReference() {

    GeographicCoordinateSystem gcs = new GeographicCoordinateSystem();
    gcs.setWKID(4326);
    gcs.setWKT(
        "GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137,298.257223563]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]]");
    gcs.setHighPrecision(true);
    gcs.setXYScale(10000000000.0);
    gcs.setXOrigin(-200.0);
    gcs.setYOrigin(-100.0);
    gcs.setZOrigin(-1000.0);
    gcs.setMOrigin(0.0);
    gcs.setZScale(100.0);
    gcs.setMScale(100.0);
    gcs.setXYTolerance(0.0000000001);
    gcs.setLeftLongitude(-180.0);
    return gcs;
  }
}
