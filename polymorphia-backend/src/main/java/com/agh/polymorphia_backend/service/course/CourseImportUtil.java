package com.agh.polymorphia_backend.service.course;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CourseImportUtil {
    public static <T> List<T> getNewEntities(List<T> request, List<T> current, Function<T, String> keyExtractor) {
        List<String> currentKeys = current.stream()
                .map(keyExtractor)
                .toList();

        return request.stream()
                .filter(entity -> !currentKeys.contains(keyExtractor.apply(entity)))
                .collect(Collectors.toList());
    }

    public static <Dto> List<Dto> getDeletedEntities(List<Dto> request, List<Dto> current, Function<Dto, String> keyExtractor) {
        List<String> requestKeys = request.stream()
                .map(keyExtractor)
                .toList();

        return current.stream()
                .filter(entity -> !requestKeys.contains(keyExtractor.apply(entity)))
                .collect(Collectors.toList());
    }

    public static <T, K> List<T> getModifiedEntities(List<T> request, List<T> current, Function<T, K> keyExtractor) {
        Map<T, Long> currentIndexMap = enumerateAsMap(current);
        Map<T, Long> requestIndexMap = enumerateAsMap(request);

        Map<K, T> currentMap = current.stream()
                .collect(Collectors.toMap(keyExtractor, item -> item));

        Map<K, Long> currentKeyIndexMap = current.stream()
                .collect(Collectors.toMap(keyExtractor, currentIndexMap::get));

        return request.stream()
                .filter(item -> {
                    K key = keyExtractor.apply(item);
                    if (!currentMap.containsKey(key)) {
                        return false;
                    }
                    boolean contentChanged = !item.equals(currentMap.get(key));
                    boolean orderChanged = !requestIndexMap.get(item).equals(currentKeyIndexMap.get(key));
                    return contentChanged || orderChanged;
                })
                .collect(Collectors.toList());
    }

    public static <T> Map<T, Long> enumerateAsMap(List<T> list) {
        return IntStream.range(0, list.size())
                .boxed()
                .collect(Collectors.toMap(list::get, Integer::longValue));
    }

    public static <T> void handleTypeChangedObjects(
            List<T> newObjects,
            List<T> deletedObjects,
            List<T> modifiedObjects,
            List<T> currentConfig,
            Function<T, String> keyExtractor,
            Function<T, ?> typeExtractor
    ) {
        Map<String, T> currentItemsByKey = currentConfig.stream()
                .collect(Collectors.toMap(keyExtractor, Function.identity()));

        List<T> typeChangedItems = modifiedObjects.stream()
                .filter(modified -> {
                    T current = currentItemsByKey.get(keyExtractor.apply(modified));
                    return !typeExtractor.apply(current).equals(typeExtractor.apply(modified));
                })
                .toList();

        typeChangedItems.forEach(modified -> {
            T current = currentItemsByKey.get(keyExtractor.apply(modified));
            deletedObjects.add(current);
            newObjects.add(modified);
        });

        modifiedObjects.removeAll(typeChangedItems);
    }
}
