package io.aboutcode.stage.configuration;

import io.aboutcode.stage.dispatch.Dispatcher;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>This analyses a specified configuration object through introspection and returns a list of
 * {@link ConfigurationParameter}s extracted from its members that are annotated with {@link
 * Parameter}</p>
 */
public final class ParameterParser {
   private static final String TRAILING_DASHES = "-+$";
   private static final Dispatcher<Class, Supplier<Collection>> COLLECTION_CREATOR =
       Dispatcher.<Class, Supplier<Collection>>of(Set.class, HashSet::new)
           .with(List.class, ArrayList::new)
           .with(Collection.class, ArrayList::new);

   /**
    * Returns a list of {@link ConfigurationParameter}s for the specified configuration object's
    * annotated members.
    *
    * @param parameterPrefix The prefix to use for all parameters
    * @param targetObject    The object to analyze
    * @param <ResultT>       The type of the configuration object
    *
    * @return A list of extracted {@link ConfigurationParameter}s
    */
   public static <ResultT> List<ConfigurationParameter> parseParameterClass(String parameterPrefix,
                                                                            ResultT targetObject) {
      return Stream.of(targetObject.getClass().getDeclaredFields())
                   .filter(ParameterParser::isParameterAnnotated)
                   .map(field -> asConfigurationParameter(parameterPrefix, field, targetObject))
                   .filter(Optional::isPresent)
                   .map(Optional::get)
                   .collect(Collectors.toList());
   }

   private static String getTypeName(Class type) {
      return type.getSimpleName();
   }

   private static ConfigurationParameter array(Parameter parameter,
                                               FieldAnalysis fieldAnalysis,
                                               InputConverter inputConverter) {
      return new ArrayConfigurationParameter(
          withPrefix(fieldAnalysis.getParameterPrefix(), parameter.name()),
          parameter.description(),
          parameter.mandatory(),
          getTypeName(fieldAnalysis.getField().getClass()),
          fieldAnalysis.getTargetObject(),
          fieldAnalysis.getSpecificClass(),
          fieldAnalysis.getField(),
          fieldAnalysis.getDefaultValue(),
          inputConverter
      );
   }

   private static ConfigurationParameter singleValue(Parameter parameter,
                                                     FieldAnalysis fieldAnalysis,
                                                     InputConverter inputConverter) {
      return new DefaultConfigurationParameter(
          withPrefix(fieldAnalysis.getParameterPrefix(), parameter.name()),
          parameter.description(),
          parameter.mandatory(),
          getTypeName(fieldAnalysis.getSpecificClass()),
          fieldAnalysis.getTargetObject(),
          fieldAnalysis.getField(),
          fieldAnalysis.getDefaultValue(),
          inputConverter
      );
   }

   private static ConfigurationParameter collection(Parameter parameter,
                                                    FieldAnalysis fieldAnalysis,
                                                    InputConverter inputConverter) {
      return new CollectionConfigurationParameter(
          withPrefix(fieldAnalysis.getParameterPrefix(), parameter.name()),
          parameter.description(),
          parameter.mandatory(),
          getTypeName(fieldAnalysis.getSpecificClass()),
          fieldAnalysis.getTargetObject(),
          fieldAnalysis.getField(),
          fieldAnalysis.getDefaultValue(),
          inputConverter,
          fieldAnalysis.getCollectionClass(),
          () -> instantiateCollection(fieldAnalysis)
      );
   }

   private static String withPrefix(String parameterPrefix, String name) {
      String result = name;
      if (Objects.nonNull(parameterPrefix) && !parameterPrefix.trim().isEmpty()) {
         result = String.format("%s-%s", parameterPrefix.replaceAll(TRAILING_DASHES, ""), name);
      }

      return result;
   }

   private static Optional<ConfigurationParameter> asConfigurationParameter(
       String parameterPrefix,
       Field field,
       Object targetObject) {
      Parameter annotation = getParameterAnnotation(field);

      FieldAnalysis fieldAnalysis = FieldAnalysis.of(parameterPrefix, field, targetObject);

      InputConverter<Object> inputConverter;
      if (annotation.inputConverter() == InputConverter.class) {
         //noinspection unchecked
         Optional<InputConverter<Object>> converter = SingleValueInputConverters
             .getConverter(fieldAnalysis.getSpecificClass());

         // todo: unclear why orElseThrow is not working - fix
         if (converter.isPresent()) {
            inputConverter = converter.get();
         } else {
            throw new IllegalArgumentException(
                String.format(
                    "Could not find default input converter for type '%s' of field '%s' and parameter '%s'",
                    fieldAnalysis.getSpecificClass().getSimpleName(),
                    field.getName(),
                    annotation.name())
            );
         }
      } else {
         inputConverter = instantiateFrom(annotation.inputConverter());
      }

      ConfigurationParameter result;
      switch (fieldAnalysis.getFieldType()) {
         case ARRAY:
            result = array(annotation, fieldAnalysis, inputConverter);
            break;
         case COLLECTION:
            result = collection(annotation, fieldAnalysis, inputConverter);
            break;
         case SINGLE_VALUE:
            result = singleValue(annotation, fieldAnalysis, inputConverter);
            break;
         default:
            result = null;
      }

      //noinspection ConstantConditions
      return Optional.ofNullable(result);
   }

   private static boolean isParameterAnnotated(Field field) {
      return getParameterAnnotation(field) != null;
   }

   private static Parameter getParameterAnnotation(Field field) {
      return field.getAnnotation(Parameter.class);
   }

   private static InputConverter<Object> instantiateFrom(Class<InputConverter> type) {
      try {
         //noinspection unchecked
         return (InputConverter<Object>) type.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
         throw new IllegalArgumentException(
             "Input converter does not specify a no-argument constructor");
      }
   }

   private static Object convert(InputConverter inputConverter, String value, String typeName,
                                 String parameterName) {
      try {
         return inputConverter.convert(value);
      } catch (Exception e) {
         throw new IllegalArgumentException(
             String.format(
                 "Could not convert value '%s' to type '%s' for parameter '%s' because: %s",
                 value,
                 typeName,
                 parameterName,
                 e.getMessage()));
      }
   }

   private static Collection instantiateCollection(FieldAnalysis fieldAnalysis) {
      return Optional.ofNullable(
          COLLECTION_CREATOR.dispatch(fieldAnalysis.getCollectionClass())
                            .orElse(() -> {
                               try {
                                  return (Collection) fieldAnalysis.getCollectionClass()
                                                                   .newInstance();
                               } catch (InstantiationException | IllegalAccessException e) {
                                  return null;
                               }
                            }).get()
      ).orElseThrow(() -> new IllegalArgumentException(
          String.format("Could not create instance of collection type '%s' for field '%s'",
                        fieldAnalysis.getCollectionClass().getSimpleName(),
                        fieldAnalysis.getField().getName())
      ));
   }

   private enum FieldType {
      SINGLE_VALUE,
      ARRAY,
      COLLECTION
   }

   private static class CollectionConfigurationParameter extends ConfigurationParameter {
      private final Object targetObject;
      private final Supplier<Collection> collectionSupplier;
      private final Field field;
      private final Object defaultValue;
      private final InputConverter inputConverter;
      private Class collectionType;

      private CollectionConfigurationParameter(String name,
                                               String description,
                                               boolean mandatory,
                                               String typeName,
                                               Object targetObject,
                                               Field field,
                                               Object defaultValue,
                                               InputConverter inputConverter,
                                               Class collectionType,
                                               Supplier<Collection> collectionSupplier) {
         super(name, description, mandatory, typeName);
         this.targetObject = targetObject;
         this.collectionSupplier = collectionSupplier;
         this.field = field;
         this.defaultValue = defaultValue;
         this.inputConverter = inputConverter;
         this.collectionType = collectionType;
      }

      @Override
      public void apply(boolean isParameterPresent, List<String> values)
          throws IllegalArgumentException {
         Collection collection = (Collection) defaultValue;

         if (isParameterPresent) {
            collection = collectionSupplier.get();
            for (String value : values) {
               //noinspection unchecked
               collection.add(
                   convert(inputConverter, value, collectionType.getSimpleName(), getName())
               );
            }
         }
         try {
            field.setAccessible(true);
            field.set(targetObject, collection);
         } catch (Exception e) {
            throw new IllegalArgumentException(
                String.format(
                    "Could not assign collection '%s' to type '%s' for parameter '%s' because: %s",
                    String.join(",", values),
                    getTypeName(),
                    getName(),
                    e.getMessage()));
         }
      }
   }

   private static class ArrayConfigurationParameter extends ConfigurationParameter {
      private final Object targetObject;
      private final Class arrayType;
      private final Field field;
      private final Object defaultValue;
      private final InputConverter inputConverter;

      private ArrayConfigurationParameter(String name,
                                          String description,
                                          boolean mandatory,
                                          String typeName,
                                          Object targetObject,
                                          Class arrayType,
                                          Field field,
                                          Object defaultValue,
                                          InputConverter inputConverter) {
         super(name, description, mandatory, typeName);
         this.targetObject = targetObject;
         this.arrayType = arrayType;
         this.field = field;
         this.defaultValue = defaultValue;
         this.inputConverter = inputConverter;
      }

      @Override
      public void apply(boolean isParameterPresent, List<String> values)
          throws IllegalArgumentException {
         Object array = defaultValue;
         if (isParameterPresent) {
            array = Array.newInstance(arrayType, values.size());
            int pos = 0;
            for (String value : values) {
               Array.set(array, pos++,
                         convert(inputConverter, value, arrayType.getSimpleName(), getName()));
            }
         }
         try {
            field.set(targetObject, array);
         } catch (Exception e) {
            throw new IllegalArgumentException(
                String.format(
                    "Could not assign array '%s' to type '%s' for parameter '%s' because: %s",
                    String.join(",", values),
                    getTypeName(),
                    getName(),
                    e.getMessage()));
         }
      }
   }

   private static final class DefaultConfigurationParameter extends
       SingleValueConfigurationParameter {
      private final Object targetObject;
      private final Field field;
      private final Object defaultValue;
      private final InputConverter inputConverter;

      private DefaultConfigurationParameter(String name,
                                            String description,
                                            boolean mandatory,
                                            String typeName,
                                            Object targetObject,
                                            Field field,
                                            Object defaultValue,
                                            InputConverter inputConverter) {
         super(name, description, mandatory, typeName);
         this.targetObject = targetObject;
         this.field = field;
         this.defaultValue = defaultValue;
         this.inputConverter = inputConverter;
      }

      @Override
      public void apply(boolean isParameterPresent, String value)
          throws IllegalArgumentException {
         try {
            field.set(targetObject,
                      !isParameterPresent ? defaultValue :
                      convert(inputConverter, value, getTypeName(), getName()));
         } catch (Exception e) {
            throw new IllegalArgumentException(
                String.format(
                    "Could not set property '%s' of configuration for object of type '%s' because: %s",
                    field.getName(),
                    targetObject.getClass().getSimpleName(),
                    e.getMessage()));
         }
      }
   }

   private static class FieldAnalysis {

      private final FieldType fieldType;
      private final Class collectionClass;
      private final Class specificClass;
      private final Field field;
      private final Object defaultValue;
      private final Object targetObject;
      private String parameterPrefix;

      private FieldAnalysis(String parameterPrefix, FieldType fieldType, Class collectionClass,
                            Class specificClass,
                            Field field, Object defaultValue, Object targetObject) {
         this.parameterPrefix = parameterPrefix;
         this.fieldType = fieldType;
         this.collectionClass = collectionClass;
         this.specificClass = specificClass;
         this.field = field;
         this.defaultValue = defaultValue;
         this.targetObject = targetObject;
      }

      public static FieldAnalysis of(String parameterPrefix,
                                     Field field,
                                     Object targetObject) {
         Class<?> type = field.getType();
         Object defaultValue;
         try {
            field.setAccessible(true);
            defaultValue = field.get(targetObject);
         } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(
                String.format("Could not extract default value from field '%s' because: %s",
                              field.getName(),
                              e.getMessage())
            );
         }

         if (Collection.class.isAssignableFrom(type)) {
            Class specificClass = (Class) ((ParameterizedType) field.getGenericType())
                .getActualTypeArguments()[0];
            return new FieldAnalysis(parameterPrefix,
                                     FieldType.COLLECTION,
                                     type,
                                     specificClass,
                                     field,
                                     defaultValue,
                                     targetObject);
         }

         Class<?> arrayComponentType = type.getComponentType();
         if (arrayComponentType != null) {
            return new FieldAnalysis(
                parameterPrefix,
                FieldType.ARRAY,
                null,
                arrayComponentType,
                field,
                defaultValue,
                targetObject);
         }

         return new FieldAnalysis(
             parameterPrefix,
             FieldType.SINGLE_VALUE,
             null,
             type,
             field,
             defaultValue,
             targetObject);
      }

      String getParameterPrefix() {
         return parameterPrefix;
      }

      FieldType getFieldType() {
         return fieldType;
      }

      Class getCollectionClass() {
         return collectionClass;
      }

      Class getSpecificClass() {
         return specificClass;
      }

      Field getField() {
         return field;
      }

      Object getDefaultValue() {
         return defaultValue;
      }

      Object getTargetObject() {
         return targetObject;
      }
   }
}
