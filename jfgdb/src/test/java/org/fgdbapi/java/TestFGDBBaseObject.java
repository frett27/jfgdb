package org.fgdbapi.java;

import java.io.File;

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

}
