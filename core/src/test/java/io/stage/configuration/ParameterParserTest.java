package io.aboutcode.stage.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class ParameterParserTest {
   private static List<String> l(String... values) {
      return Stream.of(values).collect(Collectors.toList());
   }

   private static void applyAll(List<ConfigurationParameter> configurationParameters,
                                Map<String, List<String>> input) {
      for (ConfigurationParameter configurationParameter : configurationParameters) {
         List<String> current = input.get(configurationParameter.getName());
         configurationParameter.apply(current != null, current);
      }
   }

   @Test
   public void test_simple_string() {
      DummyOne result = new DummyOne();
      List<ConfigurationParameter> configurationParameters = ParameterParser
          .parseParameterClass(null, result);

      assertNotNull(configurationParameters);
      assertEquals(1, configurationParameters.size());

      ConfigurationParameter configurationParameter = configurationParameters.iterator().next();

      assertEquals("test", configurationParameter.getName());
      assertEquals("A test field", configurationParameter.getDescription());
      assertTrue(configurationParameter.isMandatory());
      assertEquals("String", configurationParameter.getTypeName());
      configurationParameter.apply(true, l("TEST"));
      assertEquals("TEST", result.fieldOne);
   }

   @Test
   public void test_parameter_name() {
      DummyOne result = new DummyOne();
      List<ConfigurationParameter> configurationParameters = ParameterParser
          .parseParameterClass("test", result);

      assertNotNull(configurationParameters);
      assertEquals(1, configurationParameters.size());

      ConfigurationParameter configurationParameter = configurationParameters.iterator().next();

      assertEquals("test-test", configurationParameter.getName());
      assertEquals("A test field", configurationParameter.getDescription());
      assertTrue(configurationParameter.isMandatory());
      assertEquals("String", configurationParameter.getTypeName());
      configurationParameter.apply(true, l("TEST"));
      assertEquals("TEST", result.fieldOne);
   }

   @Test
   public void test_simple_types() {
      DummyThree targetObject = new DummyThree();
      List<ConfigurationParameter> configurationParameters = ParameterParser
          .parseParameterClass(null, targetObject);

      Map<String, List<String>> input = new HashMap<>();
      input.put("int", l("10"));
      input.put("integer", l("10"));
      input.put("long", l("1000000000"));
      input.put("Long", l("1000000000"));
      input.put("char", l("a"));
      input.put("character", l("a"));
      input.put("byte", l("123"));
      input.put("Byte", l("123"));
      input.put("short", l("244"));
      input.put("Short", l("244"));
      input.put("double", l("123.123"));
      input.put("Double", l("123.123"));
      input.put("float", l("3.4"));
      input.put("Float", l("3.4"));
      input.put("object", l("OBJECT"));
      input.put("string", l("STRING"));

      applyAll(configurationParameters, input);

      assertEquals(10, targetObject.intField);
      assertEquals(10, targetObject.integerField.intValue());
      assertEquals(1000000000, targetObject.longField);
      assertEquals(1000000000, targetObject.LongField.longValue());
      assertEquals('a', targetObject.charField);
      assertEquals('a', targetObject.characterField.charValue());
      assertEquals(123, targetObject.byteField);
      assertEquals(123, targetObject.ByteField.byteValue());
      assertEquals(244, targetObject.shortField);
      assertEquals(244, targetObject.ShortField.shortValue());
      assertEquals(123.123, targetObject.doubleField, 0.001);
      assertEquals(123.123, targetObject.DoubleField, 0.001);
      assertEquals(3.4, targetObject.floatField, 0.001);
      assertEquals(3.4, targetObject.FloatField, 0.001);
      assertEquals("OBJECT", targetObject.objectField);
      assertEquals("STRING", targetObject.stringField);
   }

   @Test
   public void test_defaults() {
      DummyThree targetObject = new DummyThree();
      List<ConfigurationParameter> configurationParameters = ParameterParser
          .parseParameterClass(null, targetObject);

      Map<String, List<String>> input = new HashMap<>();

      applyAll(configurationParameters, input);

      assertEquals(1, targetObject.intField);
      assertEquals(1, targetObject.integerField.intValue());
      assertEquals(500, targetObject.longField);
      assertEquals(500, targetObject.LongField.longValue());
      assertEquals('z', targetObject.charField);
      assertEquals('z', targetObject.characterField.charValue());
      assertEquals(12, targetObject.byteField);
      assertEquals(12, targetObject.ByteField.byteValue());
      assertEquals(123, targetObject.shortField);
      assertEquals(123, targetObject.ShortField.shortValue());
      assertEquals(1.1, targetObject.doubleField, 0.001);
      assertEquals(1.1, targetObject.DoubleField, 0.001);
      assertEquals(0.4, targetObject.floatField, 0.001);
      assertEquals(0.4, targetObject.FloatField, 0.001);
      assertEquals("object", targetObject.objectField);
      assertEquals("string", targetObject.stringField);
   }

   private static class DummyOne {
      @Parameter(name = "test", description = "A test field")
      private String fieldOne;
   }

   private static class DummyTwo {
      @Parameter(name = "test", description = "A test field")
      private int fieldOne;
   }

   private static class DummyThree {
      @Parameter(name = "int", description = "A test field")
      private int intField = 1;
      @Parameter(name = "integer", description = "A test field")
      private Integer integerField = 1;
      @Parameter(name = "long", description = "A test field")
      private long longField = 500;
      @Parameter(name = "Long", description = "A test field")
      private Long LongField = 500L;
      @Parameter(name = "char", description = "A test field")
      private char charField = 'z';
      @Parameter(name = "character", description = "A test field")
      private Character characterField = 'z';
      @Parameter(name = "byte", description = "A test field")
      private byte byteField = 12;
      @Parameter(name = "Byte", description = "A test field")
      private Byte ByteField = 12;
      @Parameter(name = "short", description = "A test field")
      private short shortField = 123;
      @Parameter(name = "Short", description = "A test field")
      private Short ShortField = 123;
      @Parameter(name = "double", description = "A test field")
      private double doubleField = 1.1;
      @Parameter(name = "Double", description = "A test field")
      private Double DoubleField = 1.1;
      @Parameter(name = "float", description = "A test field")
      private float floatField = 0.4f;
      @Parameter(name = "Float", description = "A test field")
      private Float FloatField = 0.4f;
      @Parameter(name = "object", description = "A test field")
      private Object objectField = "object";
      @Parameter(name = "string", description = "A test field")
      private String stringField = "string";
   }
}