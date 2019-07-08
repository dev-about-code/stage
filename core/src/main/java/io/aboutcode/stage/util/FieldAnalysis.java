package io.aboutcode.stage.util;

import io.aboutcode.stage.dispatch.Dispatcher;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public final class FieldAnalysis {
    private static final Dispatcher<Class, Supplier<Collection>> COLLECTION_CREATOR =
            Dispatcher.<Class, Supplier<Collection>>of(Set.class, HashSet::new)
                    .with(List.class, ArrayList::new)
                    .with(Collection.class, ArrayList::new);
    private final FieldType fieldType;
    private final Class collectionClass;
    private final Class specificClass;
    private final Field field;
    private final Object defaultValue;
    private final Object targetObject;

    private FieldAnalysis(FieldType fieldType, Class collectionClass,
                          Class specificClass,
                          Field field, Object defaultValue, Object targetObject) {
        this.fieldType = fieldType;
        this.collectionClass = collectionClass;
        this.specificClass = specificClass;
        this.field = field;
        this.defaultValue = defaultValue;
        this.targetObject = targetObject;
    }

    public static FieldAnalysis of(Field field,
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
            return new FieldAnalysis(FieldType.COLLECTION,
                                     type,
                                     specificClass,
                                     field,
                                     defaultValue,
                                     targetObject);
        }

        Class<?> arrayComponentType = type.getComponentType();
        if (arrayComponentType != null) {
            return new FieldAnalysis(FieldType.ARRAY,
                                     null,
                                     arrayComponentType,
                                     field,
                                     defaultValue,
                                     targetObject);
        }

        return new FieldAnalysis(FieldType.SINGLE_VALUE,
                                 null,
                                 type,
                                 field,
                                 defaultValue,
                                 targetObject);
    }

    public void assign(Object value) throws IllegalAccessException {
        Object finalValue;
        switch (getFieldType()) {
            case ARRAY:
                finalValue = instantiateArray(value);
                break;
            case COLLECTION:
                finalValue = instantiateCollection(value);
                break;
            case SINGLE_VALUE:
                finalValue = value;
                break;
            default:
                finalValue = null;
        }

        setValue(finalValue);
    }

    private void setValue(Object value) throws IllegalAccessException {
        if (!field.getType().isPrimitive() || value != null) {
            field.setAccessible(true);
            field.set(targetObject, value);
        }
    }

    private Object instantiateArray(Object value) {
        int length = Array.getLength(value);
        Object array = Array.newInstance(getSpecificClass(), length);
        for (int i = 0; i < length; i++) {
            Array.set(array, i, Array.get(value, i));
        }
        return array;
    }

    private Collection instantiateCollection(Object value) {
        return Optional.ofNullable(
                COLLECTION_CREATOR.dispatch(getCollectionClass())
                                  .orElse(() -> {
                                      try {
                                          return (Collection) getCollectionClass().newInstance();
                                      } catch (InstantiationException | IllegalAccessException e) {
                                          return null;
                                      }
                                  }).get()
        ).map(targetCollection -> {
            Collection source = (Collection) value;
            //noinspection unchecked
            targetCollection.addAll(source);
            return targetCollection;
        })
                       .orElseThrow(() -> new IllegalArgumentException(
                               String.format(
                                       "Could not create instance of collection type '%s' for field '%s'",
                                       getCollectionClass().getSimpleName(), getField().getName())
                       ));
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    private Class getCollectionClass() {
        return collectionClass;
    }

    public Class getSpecificClass() {
        return specificClass;
    }

    public Field getField() {
        return field;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public Object getTargetObject() {
        return targetObject;
    }

    public enum FieldType {
        SINGLE_VALUE,
        ARRAY,
        COLLECTION
    }
}
