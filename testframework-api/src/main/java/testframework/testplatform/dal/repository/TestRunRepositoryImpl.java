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
import testframework.testplatform.dal.entities.TestRun;
import testframework.testplatform.dal.entities.TestRunStatus;
import testframework.testplatform.dal.entities.TestRun_;
import testframework.testplatform.dal.entities.Test_;
import testframework.testplatform.dal.exceptions.EntityValidationException;
import testframework.testplatform.dal.filter.TestRunFilter;
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
public class TestRunRepositoryImpl implements TestRunRepository {

    @PersistenceContext
    private final EntityManager entityManager;
    private final ConstraintValidator validator;

    @Autowired
    public TestRunRepositoryImpl(EntityManager entityManager, ConstraintValidatorImpl validator) {
        if (entityManager == null || validator == null) {
            throw new IllegalArgumentException("entitymanager or validator is null");
        }
        this.entityManager = entityManager;
        this.validator = validator;
    }


    public void create(TestRun entity) {
        try {
            validator.validate(entity);
            entityManager.persist(entity);
            entityManager.flush();
        } catch (javax.persistence.PersistenceException ex) {
            throw new EntityValidationException("Failed to create entity, check inner exception: " + ex.getCause() + ex.getMessage(), ex);
        }
    }

    public void update(TestRun entity) {
        try {
            validator.validate(entity);
            entityManager.merge(entity);
            entityManager.flush();
        } catch (javax.persistence.PersistenceException ex) {
            throw new EntityValidationException("Failed to update entity, check inner exception", ex);
        }
    }

    public void delete(TestRun entity) {
        try {
            validator.validate(entity);
            entityManager.remove(entity);
            entityManager.flush();
        } catch (javax.persistence.PersistenceException ex) {
            throw new EntityValidationException("Failed to delete entity, check inner exception", ex);
        }
    }

    public TestRun getById(long id) {
        return entityManager.find(TestRun.class, id);
    }


    public List<TestRun> find(TestRunFilter filter) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TestRun> criteria = builder.createQuery(TestRun.class);
        Root<TestRun> root = criteria.from(TestRun.class);
        criteria.select(root);

        List<Predicate> validPredicates = new LinkedList<>();
        if (filter.getId() != null) {
            Predicate id = builder.equal(root.get(TestRun_.id), filter.getId());
            validPredicates.add(id);
        }
        if (filter.getTestId() != null) {
            Predicate test = builder.equal(root.get(TestRun_.test).get(Test_.id), filter.getTestId());
            validPredicates.add(test);
        }
        if (filter.getTestRunStates() != null) {
            List<Predicate> orEnumPredicates = new LinkedList<>();
            for (TestRunStatus testRunStatus : filter.getTestRunStates()) {
                Predicate status = builder.equal(root.get(TestRun_.testRunStatus), testRunStatus);
                orEnumPredicates.add(status);
            }
            //cannot be 2 states at once, use OR for search
            validPredicates.add(builder.or(orEnumPredicates.toArray(new Predicate[orEnumPredicates.size()])));
        }

        criteria.where(builder.and(validPredicates.toArray(new Predicate[validPredicates.size()])));
        return entityManager.createQuery(criteria.select(root)).getResultList();
    }

}
