package nl.jointeffort.testutils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Injects specified fields (based on property names).
 * 
 * @author mlindhout
 */
public final class TestcaseDependencyInjector {

	private static final Logger LOG = LoggerFactory.getLogger(TestcaseDependencyInjector.class);
	
    /**
     * Prevent instantiation.
     */
    private TestcaseDependencyInjector() {
    }

    /**
     * Injects all fields with @Mock annotation from mockHolder(and its parent classes) into dependant(and its parent
     * classes).
     * 
     * @param mockHolder Object containing @Mock objects
     * @param dependant Dependant object
     */
    public static void autoInject(Object mockHolder, Object dependant) {

        Map<String, Object> mockFieldToValue = findMocks(mockHolder);
        for (Map.Entry<String, Object> mockFieldAndValue : mockFieldToValue.entrySet()) {
            try {
                internalInject(dependant, mockFieldAndValue.getKey(), mockFieldAndValue.getValue());
            } catch (IllegalArgumentException exception) {
                LOG.info("Exception swallowed during mock auto injection for attribute "
                    + mockFieldAndValue.getKey());
            }
        }
    }

    /**
     * Injects a named dependency into dependant.
     * 
     * @param dependant Dependant object
     * @param property Member to inject
     * @param dependency Actual (mocked) dependency
     */
    public static void inject(Object dependant, String property, Object dependency) {
        internalInject(dependant, property, dependency);
    }

    /**
     * Does the actual injection of a single property.
     */
    private static void internalInject(Object dependant, String property, Object dependency) {
        try {
            Field field = findField(dependant.getClass(), property);
            field.setAccessible(true);
            field.set(dependant, dependency);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(e);
        } catch (SecurityException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * @param clazz Class to start inspection with
     * @param fieldName name of field to search for
     * @return found field
     * @throws NoSuchFieldException if nothing could be found
     */
    private static Field findField(Class<? extends Object> clazz, String fieldName)
        throws NoSuchFieldException {
        Class<? extends Object> clazzToInspect = clazz;
        Field foundField = null;
        while (clazzToInspect != null) {
            try {
                foundField = clazzToInspect.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException e) {
                // Not found here, try superclass
                clazzToInspect = clazzToInspect.getSuperclass();
            }
        }
        if (foundField == null) {
            throw new NoSuchFieldException("Field [" + fieldName + "] not found in [" + clazz
                + "] or one of its super types.");
        }
        return foundField;
    }

    /**
     * Returns all fields with @Mock annotation from mockHolder.
     * 
     * @param dependant
     * @return Map: key : property name, value: mock object
     */
    private static Map<String, Object> findMocks(Object mockHolder) {
        // find mock objects
        Map<String, Object> mockFieldToValue = new HashMap<String, Object>();
        Class<?> currentClass = mockHolder.getClass();
        while (currentClass != null) {
            for (Field fld : currentClass.getDeclaredFields()) {
                Mock mockAnnot = fld.getAnnotation(Mock.class);
                if (mockAnnot != null) {
                    fld.setAccessible(true);
                    try {
                        mockFieldToValue.put(fld.getName(), fld.get(mockHolder));
                    } catch (IllegalAccessException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return mockFieldToValue;
    }

}
