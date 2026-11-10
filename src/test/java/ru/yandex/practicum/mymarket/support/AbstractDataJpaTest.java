package ru.yandex.practicum.mymarket.support;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

/** Единая конфигурация слоя данных для всех тестов репозиториев. */
@DataJpaTest
public abstract class AbstractDataJpaTest {

    @PersistenceContext
    protected EntityManager entityManager;

    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
