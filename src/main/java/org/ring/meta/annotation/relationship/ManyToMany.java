package org.ring.meta.annotation.relationship;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by quanle on 5/13/2017.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ManyToMany
{
    Class<?> entity();

    String joinTable();

    String foreignKey();

    String referencedColumn();
}
/*
select prj.*
from project prj, prj_dev pd
where pd.dev_id = {id} and pd.prj_id = prj.id
 */

/*
FK của dev là prj_id, là column thuộc dev, tham chiếu đến id của prj
foreignKey của dev là dev_id, là column thuộc prj, tham chiếu đến id của dev
* */