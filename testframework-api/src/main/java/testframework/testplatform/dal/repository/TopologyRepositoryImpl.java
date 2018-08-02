package testframework.testplatform.dal.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import testframework.testplatform.dal.entities.TestRun;
import testframework.testplatform.dal.entities.TestRunStatus;
import testframework.testplatform.dal.entities.TestRun_;
import testframework.testplatform.dal.entities.Test_;
import testframework.testplatform.dal.entities.wireentities.Topology;
import testframework.testplatform.dal.exceptions.EntityValidationException;
import testframework.testplatform.dal.validation.ConstraintValidator;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.LinkedList;
import java.util.List;

@Repository
public class TopologyRepositoryImpl implements TopologyRepository {

    @PersistenceContext
    private final EntityManager entityManager;
    private final ConstraintValidator validator;

    @Autowired
    public TopologyRepositoryImpl(ConstraintValidator validator, EntityManager entityManager) {
        this.validator = validator;
        this.entityManager = entityManager;
    }

    @Override
    public void create(Topology entity) {
        try {
            validator.validate(entity);
            entityManager.persist(entity);
            entityManager.flush();
        } catch (javax.persistence.PersistenceException ex) {
            throw new EntityValidationException("Failed to create entity, check inner exception: " + ex.getCause() + ex.getMessage(), ex);
        }
    }

    @Override
    public void update(Topology entity) {
        try {
            validator.validate(entity);
            entityManager.merge(entity);
            entityManager.flush();
        } catch (javax.persistence.PersistenceException ex) {
            throw new EntityValidationException("Failed to update entity, check inner exception", ex);
        }
    }

    @Override
    public void delete(Topology entity) {
        try {
            validator.validate(entity);
            entityManager.remove(entity);
            entityManager.flush();
        } catch (javax.persistence.PersistenceException ex) {
            throw new EntityValidationException("Failed to delete entity, check inner exception", ex);
        }
    }

    @Override
    public Topology getById(long id) {
        return entityManager.find(Topology.class, id);
    }

    @Override
    public List<Topology> findAll() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Topology> criteria = builder.createQuery(Topology.class);
        Root<Topology> root = criteria.from(Topology.class);
        criteria.select(root);
        return entityManager.createQuery(criteria.select(root)).getResultList();
    }
}
