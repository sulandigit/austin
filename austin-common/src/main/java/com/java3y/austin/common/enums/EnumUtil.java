package com.java3y.austin.common.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author kyw7
 * 枚举工具类（获取枚举的描述、获取枚举的code、获取枚举的code列表）
 */
public class EnumUtil {

    private EnumUtil() {
    }

    // 优化：添加缓存以提高性能，避免重复的Stream操作
    private static final Map<String, Object> ENUM_CACHE = new ConcurrentHashMap<>();

    public static <T extends PowerfulEnum> String getDescriptionByCode(Integer code, Class<T> enumClass) {
        if (code == null || enumClass == null) {
            return "";
        }
        String cacheKey = enumClass.getName() + "_desc_" + code;
        return (String) ENUM_CACHE.computeIfAbsent(cacheKey, k -> 
            Arrays.stream(enumClass.getEnumConstants())
                    .filter(e -> Objects.equals(e.getCode(), code))
                    .findFirst()
                    .map(PowerfulEnum::getDescription)
                    .orElse(""));
    }

    @SuppressWarnings("unchecked")
    public static <T extends PowerfulEnum> T getEnumByCode(Integer code, Class<T> enumClass) {
        if (code == null || enumClass == null) {
            return null;
        }
        String cacheKey = enumClass.getName() + "_enum_" + code;
        return (T) ENUM_CACHE.computeIfAbsent(cacheKey, k ->
            Arrays.stream(enumClass.getEnumConstants())
                    .filter(e -> Objects.equals(e.getCode(), code))
                    .findFirst()
                    .orElse(null));
    }

    @SuppressWarnings("unchecked")
    public static <T extends PowerfulEnum> List<Integer> getCodeList(Class<T> enumClass) {
        if (enumClass == null) {
            return java.util.Collections.emptyList();
        }
        String cacheKey = enumClass.getName() + "_codeList";
        return (List<Integer>) ENUM_CACHE.computeIfAbsent(cacheKey, k ->
            Arrays.stream(enumClass.getEnumConstants())
                    .map(PowerfulEnum::getCode)
                    .collect(Collectors.toList()));
    }
}
