package com.neurocast.common.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean 转换工具
 */
public final class BeanConvertor {

    private BeanConvertor() {
    }

    /**
     * 对象属性拷贝
     */
    public static <T> T toBean(Object source, Class<T> cls) {
        if (source == null) {
            return null;
        }
        try {
            T target = cls.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new IllegalStateException("Bean 转换失败：" + cls.getName(), e);
        }
    }

    /**
     * 列表属性拷贝
     */
    public static <T> List<T> toList(List<?> list, Class<T> cls) {
        if (list == null) {
            return null;
        }
        List<T> newList = new ArrayList<>(list.size());
        list.forEach(item -> newList.add(toBean(item, cls)));
        return newList;
    }
}
