package io.aboutcode.stage.util;

import io.aboutcode.stage.dispatch.Dispatcher;
import java.lang.reflect.Array;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public final class TypeInformation {
    private static final Dispatcher<Class, Supplier<Collection>> COLLECTION_CREATOR =
            Dispatcher.<Class, Supplier<Collection>>of(Set.class, HashSet::new)
                    .with(List.class, ArrayList::new)
                    .with(Collection.class, ArrayList::new);
    private final Class<?> type;
    private final Class<?> specificType;
    private final Multiplicity multiplicity;

    private TypeInformation(Class<?> type, Class<?> specificType,
                            Multiplicity multiplicity) {
        this.type = type;
        this.specificType = specificType;
        this.multiplicity = multiplicity;
    }

    public static TypeInformation from(Parameter parameter) {
        Class<?> type = parameter.getType();

        if (Collection.class.isAssignableFrom(type)) {
            Class specificClass = (Class) ((ParameterizedType) parameter.getParameterizedType())
                    .getActualTypeArguments()[0];
            return new TypeInformation(type, specificClass, Multiplicity.COLLECTION);
        }

        Class<?> arrayComponentType = type.getComponentType();
        if (arrayComponentType != null) {
            return new TypeInformation(type, arrayComponentType, Multiplicity.ARRAY);
        }

        return new TypeInformation(type, null, Multiplicity.SINGLE_VALUE);
    }

    private boolean isSingleValue() {
        return multiplicity == Multiplicity.SINGLE_VALUE;
    }

    public boolean isPrimitive() {
        return isSingleValue() && type.isPrimitive();
    }

    public Object convert(List<String> input) {
        if (input == null) {
            return null;
        }

        Class<?> converterType;
        if (isSingleValue()) {
            converterType = type;
        } else {
            converterType = specificType;
        }

        InputConverter<?> converter = DefaultTypeConverters
                .getConverter(converterType)
                .orElseThrow(() -> new IllegalArgumentException(
                                     String.format("Could find converter for type '%s'",
                                                   converterType.getSimpleName()))
                );

        if (isSingleValue()) {
            String value = input.isEmpty() ? null : input.iterator().next();
            return converter.convert(value);
        }

        if (multiplicity == Multiplicity.ARRAY) {
            return instantiateArray(input, converter);
        }

        return instantiateCollection(input, converter);
    }

    private Object instantiateArray(List<String> input,
                                    InputConverter<?> converter) {

        int length = input.size();
        Object array = Array.newInstance(specificType, length);
        int index = 0;
        for (String value : input) {
            Array.set(array, index++, converter.convert(value));
        }
        return array;
    }

    private Collection instantiateCollection(List<String> input,
                                             InputConverter<?> converter) {
        return Optional.ofNullable(
                COLLECTION_CREATOR.dispatch(type)
                                  .orElse(() -> {
                                      try {
                                          return (Collection) type.newInstance();
                                      } catch (InstantiationException | IllegalAccessException e) {
                                          return null;
                                      }
                                  }).get()
        ).map(targetCollection -> {
            for (String value : input) {
                //noinspection unchecked
                targetCollection.add(converter.convert(value));
            }
            return targetCollection;
        }).orElseThrow(() -> new IllegalArgumentException(
                String.format(
                        "Could not create instance of collection type '%s' for generic type '%s'",
                        type.getSimpleName(), specificType.getSimpleName())
        ));
    }

    private enum Multiplicity {
        COLLECTION,
        ARRAY,
        SINGLE_VALUE
    }
}
