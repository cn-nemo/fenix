package com.blinkfox.fenix.specification.listener.impl;

import com.blinkfox.fenix.specification.annotation.OrLesserOrEqual;
import com.blinkfox.fenix.specification.listener.AbstractListener;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import org.springframework.stereotype.Component;

/**
 * 构建“或者小于等于条件”({@code OR field <= 'xxx'})场景的 Specification 监听器.
 *
 * @author YangWenpeng on 2019-12-17
 * @author blinkfox on 2020-01-14
 * @since v2.2.0
 */
@Component
public class OrLesserOrEqualSpecificationListener extends AbstractListener {

    @SuppressWarnings("unchecked")
    @Override
    protected <Z, X> Predicate buildPredicate(
            CriteriaBuilder criteriaBuilder, From<Z, X> from, String name, Object value, Object annotation) {
        return criteriaBuilder.or(criteriaBuilder.lessThanOrEqualTo(from.get(name), (Comparable) value));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<OrLesserOrEqual> getAnnotation() {
        return OrLesserOrEqual.class;
    }

}
