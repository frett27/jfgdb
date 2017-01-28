package org.fgdbapi.java;

import java.io.File;
import java.io.StringWriter;
import java.util.Arrays;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.fgdbapi.thindriver.xml.Field;
import org.fgdbapi.thindriver.xml.ObjectFactory;
import org.junit.Test;

public class TestFGDBBaseObject {

	@Test
	public void test() throws Exception {
		new FGDBTable(new File("C:\\temp\\streets.gdb\\a00000009.gdbtable"));
	}

	@Test
	public void test2() throws Exception {
		FGDBTablex tx = new FGDBTablex(new File("C:\\temp\\streets.gdb\\a00000009.gdbtablx"));
		System.out.println(tx.getCorrectedRow(0));
		System.out.println(tx.getCorrectedRow(1024));
	}

	@Test
	public void test3() throws Exception {

		FGDBTable t = new FGDBTable(new File("C:\\temp\\streets.gdb\\a00000009.gdbtable"));

		FGDBTablex tx = new FGDBTablex(new File("C:\\temp\\streets.gdb\\a00000009.gdbtablx"));

		long startTime = System.currentTimeMillis();
		try {
			int start = 0;
			for (int i = start; i < start + t.getRowNumber(); i++) {

				int row = tx.getCorrectedRow(i);
				long offset = tx.getRecordOffset(row);
				if (offset == 0)
					throw new Exception("deleted");

				Object[] result = t.readRow(offset, row);

				// System.out.println(Arrays.asList(result));
			}
		} finally {
			System.out.println(System.currentTimeMillis() - startTime);
		}
	}

	@Test
	public void test4() throws Exception {
		for (int j = 1; j < 10; j++) {
			File fileTable = new File("C:\\temp\\streets.gdb\\a0000000" + j + ".gdbtable");
			if (!fileTable.exists())
				continue;
			System.out.println("handling :" + fileTable);
			FGDBTable t = new FGDBTable(fileTable);

			System.out.println(t.getFields());
			for (Field f : t.getFields()) {
				JAXBContext isn = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
				Marshaller m = isn.createMarshaller();
				m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

				StringWriter sw = new StringWriter();
				m.marshal(new JAXBElement<Field>(new QName("http://www.esri.com/schemas/ArcGIS/10.1", "Field"),
						Field.class, f), sw);

				String rawResult = sw.getBuffer().toString();
				System.out.println(rawResult);
			}
			int rn = t.getRowNumber();
			for (int i = 0; i < rn; i++) {
				Object[] result = t.readRow(i);
				System.out.println(Arrays.asList(result));
			}
		}
	}

}
