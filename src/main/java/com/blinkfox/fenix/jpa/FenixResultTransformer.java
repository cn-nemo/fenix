package com.blinkfox.fenix.jpa;

import com.blinkfox.fenix.exception.FenixException;

import java.beans.PropertyDescriptor;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.BlobType;
import org.hibernate.type.descriptor.java.DataHelper;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.convert.JodaTimeConverters;
import org.springframework.util.Assert;

/**
 * 自定义查询结果的转换器.
 *
 * @param <T> 要转换的范型 T
 * @author blinkfox on 2019-10-08.
 */
@Slf4j
public class FenixResultTransformer<T> implements ResultTransformer {

    private static final DefaultConversionService conversionService = new DefaultConversionService();

    /**
     * 要转换类型的 class 实例.
     */
    private Class<T> resultClass;

    /**
     * 字段间的映射关系 Map.
     */
    private Map<String, PropertyDescriptor> fieldMap;

    static {
        // 添加一些默认的 ConversionService.
        Collection<Converter<?, ?>> convertersToRegister = JodaTimeConverters.getConvertersToRegister();
        for (Converter<?, ?> converter : convertersToRegister) {
            conversionService.addConverter(converter);
        }
        conversionService.addConverter(ClobToStringConverter.INSTANCE);
        conversionService.addConverter(BlobToStringConverter.INSTANCE);
    }

    /**
     * 基于要转换的 {@code resultClass} 类型的构造方法.
     *
     * @param resultClass 转换类型的 class
     */
    public FenixResultTransformer(Class<T> resultClass) {
        Assert.notNull(resultClass, "【Fenix 异常】resultClass cannot be null.");
        this.resultClass = resultClass;
    }

    /**
     * 将每行的“元组”数据转换为对应的 {@link #resultClass} 的结果类型.
     *
     * @param tuple   元组数据
     * @param aliases 元组数据的别名数组
     * @return 转换后的 {@link #resultClass} 的结果类型
     */
    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {
        T resultObject;
        try {
            resultObject = this.resultClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new FenixException("实例化【】类出错，请检查该类是否包含可公开访问的无参构造方法！", e);
        }

        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(resultObject);
        beanWrapper.setConversionService(conversionService);

        for (int i = 0, len = aliases.length; i < len; i++) {
            String column = aliases[i];
            PropertyDescriptor propertyDescriptor = this.fieldMap.get(column.replaceAll(" ", "").toLowerCase());
            if (propertyDescriptor == null) {
                continue;
            }

            try {
                beanWrapper.setPropertyValue(propertyDescriptor.getName(), tuple[i]);
            } catch (NotWritablePropertyException | TypeMismatchException e) {
                throw new FenixException("【Fenix 异常】设置字段【" + column + "】的值到属性【"
                        + propertyDescriptor.getName() + "】中出错，请检查该字段或属性是否存在或者可公开访问！", e);
            }
        }
        return resultObject;
    }

    /**
     * 直接返回集合本身即可.
     *
     * @param list 集合.
     * @return 集合
     */
    @Override
    public List transformList(List list) {
        return list;
    }

    /**
     * Clob 转换为 String 的转换器类.
     *
     * @author blinkfox 2019-10-08.
     */
    private enum ClobToStringConverter implements Converter<Clob, String> {

        /**
         * 单实例.
         */
        INSTANCE;

        @Override
        public String convert(Clob source) {
            return DataHelper.extractString(source);
        }
    }

    /**
     * Blob 转换为 String 的转换器类.
     *
     * @author blinkfox 2019-10-08.
     */
    private enum BlobToStringConverter implements Converter<Blob, String> {

        /**
         * 单实例.
         */
        INSTANCE;

        @Override
        public String convert(Blob source) {
            return BlobType.INSTANCE.toString(source);
        }
    }

}
