package boogi.apiserver.global.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OneToManyUpdateOptimizer {

    public static <T, V> List<V> entityToBeDeleted(List<V> previousEntity,
                                                   Function<V, T> entityConverter,
                                                   List<T> requestInput) {
        List<V> result = new ArrayList<>();
        for (V entity : previousEntity) {
            T converted = entityConverter.apply(entity);
            if (!requestInput.contains(converted)) {
                result.add(entity);
            }
        }
        return result;
    }

    public static <T> List<T> inputsToBeInserted(List<T> previousEntity, List<T> requestInput) {
        return getExclusiveElement(requestInput, previousEntity);
    }

    public static <T, V> List<T> inputsToBeInserted(List<V> previousEntity,
                                                    Function<V, T> entityConverter,
                                                    List<T> requestInput) {
        List<T> convertedPreviousEntity = getConvertedList(previousEntity, entityConverter);
        return inputsToBeInserted(convertedPreviousEntity, requestInput);
    }

    private static <T> List<T> getExclusiveElement(List<T> target, List<T> excluded) {
        List<T> result = new ArrayList<>(target);
        result.removeAll(excluded);
        return result;
    }

    private static <T, V> List<T> getConvertedList(List<V> previousEntity, Function<V, T> converter) {
        return previousEntity.stream()
                .map(converter)
                .collect(Collectors.toList());
    }
}
