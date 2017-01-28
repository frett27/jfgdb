package org.fgdbapi.java;

import java.io.File;
import java.util.Arrays;

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

			//System.out.println(Arrays.asList(result));
		}
		} finally {
			System.out.println(System.currentTimeMillis() - startTime);
		}
	}

}
