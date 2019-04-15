package io.aboutcode.stage.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;
import org.junit.Test;

public class ApplicationArgumentParserTest {

   @Test
   public void testEmptyArguments() throws Exception {
      Map<String, List<String>> applicationArguments = ApplicationArgumentParser.parseArguments();
      assertNotNull(applicationArguments);
      assertEquals(0, applicationArguments.size());
   }

   @Test(expected = ArgumentParseException.class)
   public void testTrailingTriggerArgument() throws Exception {
      ApplicationArgumentParser
          .parseArguments("--", "option=something", "--someother that --other=also");
   }

   @Test
   public void testLeadingArguments() throws Exception {
      Map<String, List<String>> applicationArguments = ApplicationArgumentParser
          .parseArguments("-DsomeParameter=something", "--something=false", "--other");
      assertNotNull(applicationArguments);
      assertEquals(2, applicationArguments.size());
      assertNotNull(applicationArguments.get("something"));
      assertEquals(1, applicationArguments.get("something").size());
      assertEquals("false", applicationArguments.get("something").get(0));
      assertNotNull(applicationArguments.get("other"));
      assertEquals(0, applicationArguments.get("other").size());
   }

   @Test
   public void testGeneralArguments() throws Exception {
      Map<String, List<String>> applicationArguments = ApplicationArgumentParser
          .parseArguments("--something=false", "--other");
      assertNotNull(applicationArguments);
      assertEquals(2, applicationArguments.size());
      assertNotNull(applicationArguments.get("something"));
      assertEquals(1, applicationArguments.get("something").size());
      assertEquals("false", applicationArguments.get("something").get(0));
      assertNotNull(applicationArguments.get("other"));
      assertEquals(0, applicationArguments.get("other").size());
   }

   @Test
   public void testQuotedStringArgument() throws Exception {
      Map<String, List<String>> applicationArguments = ApplicationArgumentParser
          .parseArguments("--something=\"A long string\"");
      assertNotNull(applicationArguments);
      assertEquals(1, applicationArguments.size());
      assertNotNull(applicationArguments.get("something"));
      assertEquals(1, applicationArguments.get("something").size());
      assertEquals("A long string", applicationArguments.get("something").get(0));
   }

   @Test
   public void testMultiParameterArgument() throws Exception {
      Map<String, List<String>> applicationArguments = ApplicationArgumentParser
          .parseArguments("--option=something", "someother", "that", "also");
      assertNotNull(applicationArguments);
      assertEquals(1, applicationArguments.size());
      assertNotNull(applicationArguments.get("option"));
      assertEquals(4, applicationArguments.get("option").size());
   }

   @Test
   public void testOptionArgument() throws Exception {
      Map<String, List<String>> applicationArguments = ApplicationArgumentParser
          .parseArguments("--option");
      assertNotNull(applicationArguments);
      assertEquals(1, applicationArguments.size());
      assertNotNull(applicationArguments.get("option"));
      assertEquals(0, applicationArguments.get("option").size());
   }
}