package br.com.cdsoft.repository;

import br.com.cdsoft.domain.Fruit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class FruitRepository {

    @Inject
    private EntityManager entityManager;


    public Optional<List<Fruit>> findAll() {
        return Optional.ofNullable(entityManager.createQuery("from " + Fruit.class.getSimpleName(), Fruit.class).getResultList());
    }

    @Transactional
    public Fruit persist(final Fruit fruit) {
        entityManager.merge(fruit);
        return fruit;
    }

}
