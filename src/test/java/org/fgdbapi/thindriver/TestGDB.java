package org.fgdbapi.thindriver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.namespace.QName;

import org.fgdbapi.thindriver.swig.FGDBJNIWrapper;
import org.fgdbapi.thindriver.swig.Geodatabase;
import org.fgdbapi.thindriver.swig.Row;
import org.fgdbapi.thindriver.swig.Table;
import org.fgdbapi.thindriver.xml.ArrayOfField;
import org.fgdbapi.thindriver.xml.ArrayOfIndex;
import org.fgdbapi.thindriver.xml.ArrayOfPropertySetProperty;
import org.fgdbapi.thindriver.xml.DEFeatureClass;
import org.fgdbapi.thindriver.xml.DataElement;
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
import org.fgdbapi.thindriver.xml.ObjectFactory;
import org.fgdbapi.thindriver.xml.PropertySet;

import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;

public class TestGDB {

	public static void main(String[] args) throws Exception {

		// System.out.println("cree");

		DEFeatureClass de = constructTestDEFeatureClass();

		String s = serializeElement(de);

		System.out.println(s);

		File f = File.createTempFile("tmp", ".gdb");
		f.delete();
		f.mkdirs();

		Geodatabase g = FGDBJNIWrapper.createGeodatabase(f.getAbsolutePath());

		System.out.println("gdb :" + f.getAbsolutePath());

		// StringBuffer sb = readFile("Test2.xml");
		//
		// System.out.println(sb);
		//
		// g.createTable(sb.toString(), "");

		createTestTable(g);

	}

	/**
	 * @return
	 * @throws IOException
	 */
	protected static StringBuffer readFile(String filename) throws IOException {
		InputStream is = TestGDB.class.getResourceAsStream(filename);
		InputStreamReader r = new InputStreamReader(is);

		StringBuffer sb = new StringBuffer();

		int i;
		while ((i = r.read()) != -1) {
			char c = (char) i;
			// System.out.println("add " + c);
			sb.append(c);
		}
		return sb;
	}

	/**
	 * @param g
	 * @throws JAXBException
	 * @throws PropertyException
	 */
	protected static void createTestTable(Geodatabase g) throws Exception {
		
		DEFeatureClass de = constructTestDEFeatureClass();

		String d = serializeElement(de);

		long start = System.nanoTime();
		Table t = g.createTable(d, "");
		t.setWriteLock();
		t.setLoadOnlyMode(true);
		int cpt = 0;

		while (cpt++ < 1000000) {
			Row row = t.createRowObject();
			t.insertRow(row);
			if ((cpt % 100000) == 0) {
				long time = (System.nanoTime() - start) / 1000000;
				System.out.println("elements par s :"
						+ (cpt * 1.0 / time * 1000));
			}

		}

		long end = System.nanoTime();
		System.out.println("time :" + (end - start) / 1000000 + " ms");

	}

	/**
	 * @param de
	 * @return
	 * @throws JAXBException
	 * @throws PropertyException
	 */
	protected static String serializeElement(DEFeatureClass de)
			throws JAXBException, PropertyException {
		// ////////////////////////////////

		class NP extends NamespacePrefixMapper {

			public String getPreferredPrefix(String arg0, String arg1,
					boolean arg2) {
				if (arg0.equals("http://www.esri.com/schemas/ArcGIS/10.1"))
					return "esri";
				if (arg0.equals("http://www.w3.org/2001/XMLSchema-instance"))
					return "xsi";
				return null;
			}
		}

		JAXBContext isn = JAXBContext.newInstance(ObjectFactory.class
				.getPackage().getName());
		Marshaller m = isn.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper",
				new NP());

		StringWriter sw = new StringWriter();
		m.marshal(new JAXBElement<DataElement>(new QName(
				"http://www.esri.com/schemas/ArcGIS/10.1", "DataElement"),
				DataElement.class, de), sw);

		String d = sw.getBuffer().toString();
		// System.out.println("definition :" + d);
		return d;
	}

	/**
	 * @return
	 */
	protected static DEFeatureClass constructTestDEFeatureClass() {
		DEFeatureClass de = new DEFeatureClass();
		de.setName("macouche");
		de.setFeatureType(EsriFeatureType.ESRI_FT_SIMPLE);
		de.setShapeType(EsriGeometryType.ESRI_GEOMETRY_POINT);
		de.setChildrenExpanded(false);
		de.setShapeFieldName("SHAPE");
		de.setHasM(false);
		de.setHasZ(false);
		de.setOIDFieldName("OBJECTID");
		de.setHasOID(true);
		de.setDatasetType(EsriDatasetType.ESRI_DT_FEATURE_CLASS);
		de.setVersioned(false);
		de.setCLSID("{52353152-891A-11D0-BEC6-00805F7C4268}");
		// de.setExtent(null);
		// de.setConfigurationKeyword("DEFAULT");
		de.setModelName("macouche");
		de.setAliasName("macouche");
		// de.setHasGlobalID(false);

		PropertySet ps = new PropertySet();
		de.setExtensionProperties(ps);
		ps.setPropertyArray(new ArrayOfPropertySetProperty());

		GeographicCoordinateSystem gcs = new GeographicCoordinateSystem();
		gcs.setWKID(4326);
		gcs.setWKT("GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137,298.257223563]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]]");
		gcs.setHighPrecision(true);
		gcs.setXYScale(10000.0);
		gcs.setXOrigin(-200.0);
		gcs.setYOrigin(-100.0);
		gcs.setZOrigin(-1000.0);
		gcs.setMOrigin(0.0);
		gcs.setZScale(100.0);
		gcs.setMScale(100.0);
		gcs.setXYTolerance(0.0001);
		gcs.setLeftLongitude(-180.0);
		de.setSpatialReference(gcs);

		// champs

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
		ArrayOfField a = new ArrayOfField();
		flds.setFieldArray(a);
		de.setFields(flds);

		a.getField().add(o);

		Field s = new Field();
		s.setName("SHAPE");
		s.setType(EsriFieldType.ESRI_FIELD_TYPE_GEOMETRY);
		s.setRequired(true);
		s.setIsNullable(true);
		s.setAliasName("SHAPE");
		s.setModelName("SHAPE");
		s.setEditable(true);

		GeometryDef geom = new GeometryDef();
		geom.setGeometryType(EsriGeometryType.ESRI_GEOMETRY_POINT);
		geom.setSpatialReference(gcs);
		geom.setGridSize0((double) 0);
		// geom.setHasM(false);
		// geom.setHasZ(false);
		s.setGeometryDef(geom);

		a.getField().add(s);

		Index oid = new Index();
		oid.setName("OIDINDEX");

		Fields sfields = new Fields();
		ArrayOfField sarrayOfField = new ArrayOfField();
		sfields.setFieldArray(sarrayOfField);
		sarrayOfField.getField().add(o);

		oid.setFields(sfields);
		oid.setIsUnique(true);
		oid.setIsAscending(true);

		Indexes indexes = new Indexes();
		de.setIndexes(indexes);
		ArrayOfIndex arrayOfIndex = new ArrayOfIndex();
		indexes.setIndexArray(arrayOfIndex);
		arrayOfIndex.getIndex().add(oid);

		Index sindex = new Index();
		sindex.setName("FDO_SHAPE");
		Fields shapeIndexFields = new Fields();
		ArrayOfField shapeIndexArrayOfFields = new ArrayOfField();
		shapeIndexArrayOfFields.getField().add(s);
		shapeIndexFields.setFieldArray(shapeIndexArrayOfFields);
		sindex.setFields(shapeIndexFields);
		sindex.setIsAscending(true);
		sindex.setIsUnique(false);

		arrayOfIndex.getIndex().add(sindex);

		de.setAliasName("macouche");
		// de.setHasM(false);
		// de.setHasZ(false);
		de.setHasSpatialIndex(true);
		de.setCatalogPath("\\macouche");
		// de.setCanVersion(false);

		// de.setGlobalIDFieldName("");
		// de.setRasterFieldName("");
		// de.setExtensionProperties(ps);
		// de.setControllerMemberships(new ArrayOfControllerMembership());
		de.setModelName(de.getName());
		// de.setAreaFieldName("");
		// de.setLengthFieldName("");
		// de.setEXTCLSID("");
		return de;
	}

}
