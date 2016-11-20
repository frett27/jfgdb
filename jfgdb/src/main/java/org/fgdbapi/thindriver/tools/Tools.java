package org.fgdbapi.thindriver.tools;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;

public class Tools {

  /**
   * this method copy field by field values
   *
   * @param source
   * @param deFeatureClass
   */
  public static void copy(Object source, Object destination) throws Exception {

    assert destination != null;
    assert source != null;

    BeanInfo destbi = Introspector.getBeanInfo(destination.getClass());
    BeanInfo sourcebi = Introspector.getBeanInfo(source.getClass());

    // index source properties
    HashMap<String, PropertyDescriptor> sourceip = new HashMap<>();
    for (PropertyDescriptor p : sourcebi.getPropertyDescriptors()) {
      sourceip.put(p.getName(), p);
    }

    PropertyDescriptor[] destPropertyDescriptors = destbi.getPropertyDescriptors();
    for (PropertyDescriptor pd : destPropertyDescriptors) {
      Class<?> propType = pd.getPropertyType();
      String propertyName = pd.getName();

      // has source a proper getter and are the properties type equivalent
      // ??
      System.out.println("evaluating " + propertyName);
      if (sourceip.containsKey(propertyName)) {
        PropertyDescriptor spd = sourceip.get(propertyName);
        if (pd.getPropertyType().isAssignableFrom(spd.getPropertyType())) {
          // set value

          Method wm = pd.getWriteMethod();
          if (wm == null) {
            System.out.println("no write method for property " + propertyName);
            continue;
          }
          Method rm = spd.getReadMethod();
          if (rm == null) {
            System.out.println("no read method for property " + propertyName);
            continue;
          }
          wm.invoke(destination, rm.invoke(source));
          System.out.println("property " + propertyName + " setted");
        }
      }
    }
  }
}
