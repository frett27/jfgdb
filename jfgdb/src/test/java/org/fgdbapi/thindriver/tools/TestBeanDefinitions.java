package org.fgdbapi.thindriver.tools;

import org.fgdbapi.thindriver.TableHelper;
import org.fgdbapi.thindriver.xml.DEFeatureClass;
import org.fgdbapi.thindriver.xml.DETable;
import org.junit.Test;

public class TestBeanDefinitions {

	@Test
	public void testBeanCopy() throws Exception {

		TableHelper th = TableHelper.newTable("myfeatureclass");
		DETable de = th.build();

		DEFeatureClass defeatureclass = new DEFeatureClass();

		Tools.copy(de, defeatureclass);
		
		System.out.println(defeatureclass);
		
	}

}
