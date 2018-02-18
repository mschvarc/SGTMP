/*
 * Copyright 2017 Martin Schvarcbacher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package testframework.testplatform.mapper;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


//Source: https://github.com/mschvarc/PB138-Inventory-Management/blob/dfe413c1df56cae262c5d19342c8e66a5df1b225/backend/src/main/java/pb138/service/mapper/AutomapperImpl.java
@Service
public class AutomapperImpl implements Automapper {

    private final Mapper dozer;

    @Autowired
    public AutomapperImpl(DozerBeanMapper dozer) {
        if (dozer == null) {
            throw new IllegalArgumentException("Unsatisfied dependency on dozer bean");
        }
        this.dozer = dozer;
    }


    @Override
    public <T> List<T> mapTo(Collection<?> beanCollection, Class<T> targetClass) {
        if (beanCollection == null) {
            return Collections.emptyList();
        }
        List<T> mappedCollection = new ArrayList<>();
        for (Object object : beanCollection) {
            mappedCollection.add(mapTo(object, targetClass));
        }
        return mappedCollection;
    }


    @Override
    public <T> T mapTo(Object bean, Class<T> targetClass) {
        if (bean == null) {
            return null;
        }
        return dozer.map(bean, targetClass);
    }
}
