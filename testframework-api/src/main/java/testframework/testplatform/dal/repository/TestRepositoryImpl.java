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

package testframework.testplatform.dal.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import testframework.testplatform.dal.entities.Test;
import testframework.testplatform.dal.entities.Test_;
import testframework.testplatform.dal.exceptions.EntityValidationException;
import testframework.testplatform.dal.filter.TestFilter;
import testframework.testplatform.dal.validation.ConstraintValidator;
import testframework.testplatform.dal.validation.ConstraintValidatorImpl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.LinkedList;
import java.util.List;

@Repository
public class TestRepositoryImpl implements TestRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private ConstraintValidator validator;


    @Autowired
    public TestRepositoryImpl(EntityManager entityManager, ConstraintValidatorImpl validator) {
        if (entityManager == null || validator == null) {
            throw new IllegalArgumentException("entitymanager or validator is null");
        }
        this.entityManager = entityManager;
        this.validator = validator;
    }


    public void create(Test entity) {
        try {
            validator.validate(entity);
            entityManager.persist(entity);
            entityManager.flush();
        } catch (javax.persistence.PersistenceException ex) {
            throw new EntityValidationException("Failed to create entity, check inner exception: " + ex.getCause() + ex.getMessage(), ex);
        }
    }

    public void update(Test entity) {
        try {
            validator.validate(entity);
            entityManager.merge(entity);
            entityManager.flush();
        } catch (javax.persistence.PersistenceException ex) {
            throw new EntityValidationException("Failed to update entity, check inner exception", ex);
        }
    }

    public void delete(Test entity) {
        try {
            validator.validate(entity);
            entityManager.remove(entity);
            entityManager.flush();
        } catch (javax.persistence.PersistenceException ex) {
            throw new EntityValidationException("Failed to delete entity, check inner exception", ex);
        }
    }

    public Test getById(long id) {
        return entityManager.find(Test.class, id);
    }

    public List<Test> find(TestFilter filter) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Test> criteria = builder.createQuery(Test.class);
        Root<Test> root = criteria.from(Test.class);
        criteria.select(root);

        List<Predicate> validPredicates = new LinkedList<>();
        if (filter.getId() != null) {
            Predicate id = builder.equal(root.get(Test_.id), filter.getId());
            validPredicates.add(id);
        }
        criteria.where(builder.and(validPredicates.toArray(new Predicate[validPredicates.size()])));
        return entityManager.createQuery(criteria.select(root)).getResultList();
    }

}
