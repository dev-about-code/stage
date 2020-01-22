package io.aboutcode.stage.web.serialization;

import static org.junit.Assert.assertEquals;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.annotations.SerializedName;
import io.aboutcode.stage.web.serialization.JsonWebSerialization.TypeAdapter;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import org.junit.Test;

public class JsonWebSerializationTest {

    private static Double twoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    @Test
    public void testNoAdapter() {
        JsonWebSerialization serialization = new JsonWebSerialization();

        TestClass data = new TestClass("<WOW>", 123456.7890);
        String serialized = serialization.serialize(data);
        assertEquals("{\"name\":\"<WOW>\",\"value\":123456.789}", serialized);
        TestClass deserialized = serialization.deserialize(serialized, TestClass.class);
        assertEquals(data.name, deserialized.name);
        assertEquals(data.value, deserialized.value, 0.001);
    }

    @Test
    public void testWithAdapter() {
        JsonWebSerialization serialization = new JsonWebSerialization(new TestAdapter());

        TestClass data = new TestClass("<WOW>", 123456.7890);
        String serialized = serialization.serialize(data);
        assertEquals("{\"name\":\"<WOW>\",\"value\":1234567.89}", serialized);
        TestClass deserialized = serialization.deserialize(serialized, TestClass.class);
        assertEquals(data.name, deserialized.name);
        assertEquals(data.value, deserialized.value, 0.001);
    }

    private static class TestClass {
        @SerializedName("name")
        private String name;
        @SerializedName("value")
        private Double value;

        private TestClass(String name, double value) {
            this.name = name;
            this.value = value;
        }
    }

    private static class TestAdapter implements TypeAdapter<Double> {
        @Override
        public Type getType() {
            return Double.class;
        }

        @Override
        public Double deserialize(JsonElement jsonElement, Type type,
                                  JsonDeserializationContext jsonDeserializationContext)
                throws JsonParseException {
            BigDecimal raw = jsonElement.getAsBigDecimal();
            return Optional.ofNullable(raw)
                           .map(value -> value.divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                                              .doubleValue())
                           .orElse(null);
        }

        @Override
        public JsonElement serialize(Double s, Type type,
                                     JsonSerializationContext jsonSerializationContext) {
            return Optional.ofNullable(s)
                           .map(value -> new JsonPrimitive(twoDecimals(s * 10.0)))
                           .orElse(new JsonPrimitive(Double.NaN));
        }
    }
}