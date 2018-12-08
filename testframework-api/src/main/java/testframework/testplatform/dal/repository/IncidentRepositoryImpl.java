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
import testframework.testplatform.dal.entities.Incident;
import testframework.testplatform.dal.entities.Incident_;
import testframework.testplatform.dal.entities.TestRun;
import testframework.testplatform.dal.entities.TestRun_;
import testframework.testplatform.dal.exceptions.EntityValidationException;
import testframework.testplatform.dal.validation.ConstraintValidator;
import testframework.testplatform.dal.validation.ConstraintValidator;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
public class IncidentRepositoryImpl implements IncidentRepository {

    @PersistenceContext
    private final EntityManager entityManager;
    private final ConstraintValidator validator;

    @Autowired
    public IncidentRepositoryImpl(EntityManager entityManager, ConstraintValidator validator) {
        if (entityManager == null || validator == null) {
            throw new IllegalArgumentException("entitymanager or validator is null");
        }
        this.entityManager = entityManager;
        this.validator = validator;
    }


    public void create(Incident entity) {
        try {
            validator.validate(entity);
            entityManager.persist(entity);
            entityManager.flush();
        } catch (javax.persistence.PersistenceException ex) {
            throw new EntityValidationException("Failed to create entity, check inner exception: " + ex.getCause() + ex.getMessage(), ex);
        }
    }

    public void update(Incident entity) {
        try {
            validator.validate(entity);
            entityManager.merge(entity);
            entityManager.flush();
        } catch (javax.persistence.PersistenceException ex) {
            throw new EntityValidationException("Failed to update entity, check inner exception", ex);
        }
    }

    public void delete(Incident entity) {
        try {
            validator.validate(entity);
            entityManager.remove(entity);
            entityManager.flush();
        } catch (javax.persistence.PersistenceException ex) {
            throw new EntityValidationException("Failed to delete entity, check inner exception", ex);
        }
    }

    public Incident getById(long id) {
        return entityManager.find(Incident.class, id);
    }

    public List<Incident> getAllIncidentsForTestRun(TestRun testRun) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Incident> criteria = builder.createQuery(Incident.class);
        Root<Incident> root = criteria.from(Incident.class);
        criteria.select(root);
        Predicate predicate = builder.equal(root.get(Incident_.testRun).get(TestRun_.id), testRun.getId());
        criteria.where(builder.and(predicate));
        return entityManager.createQuery(criteria.select(root)).getResultList();
    }

}
