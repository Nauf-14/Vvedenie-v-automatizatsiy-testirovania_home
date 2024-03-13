package org.max.home;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.jupiter.api.*;

import javax.persistence.PersistenceException;
import java.util.List;
import java.util.Optional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CourierInfoEntityTest extends AbstractTest {

    @Test
    @Order(1)
    void getCouriers_whenValid_shouldReturnCorrectSize() {
        // Given
        final Query query = getSession().createQuery("FROM CourierInfoEntity", CourierInfoEntity.class);

        // When
        List<CourierInfoEntity> courierList = query.list();

        // Then
        Assertions.assertEquals(4, courierList.size());
    }

    @Test
    @Order(2)
    void getCourierById_whenValid_shouldReturnCorrectCourier() {
        // Given
        final Query query = getSession().createQuery("FROM CourierInfoEntity WHERE courierId = :courierId", CourierInfoEntity.class);
        query.setParameter("courierId", (short) 1);

        // When
        Optional<CourierInfoEntity> courierEntityOptional = query.uniqueResultOptional();

        // Then
        Assertions.assertTrue(courierEntityOptional.isPresent());
        CourierInfoEntity courierEntity = courierEntityOptional.get();
        Assertions.assertEquals("John", courierEntity.getFirstName());
        Assertions.assertEquals("Rython", courierEntity.getLastName());
        Assertions.assertEquals("+ 7 960 655 0954", courierEntity.getPhoneNumber());
        Assertions.assertEquals("foot", courierEntity.getDeliveryType());
    }

    @Test
    @Order(3)
    void addCourier_whenValid_shouldSave() {
        // Given
        CourierInfoEntity courierEntity = new CourierInfoEntity();
        courierEntity.setFirstName("Test");
        courierEntity.setLastName("Courier");
        courierEntity.setPhoneNumber("+7 123 456 7890");
        courierEntity.setDeliveryType("bike");

        // When
        Session session = getSession();
        session.beginTransaction();
        session.persist(courierEntity);
        session.getTransaction().commit();

        // Then
        final Query query = getSession().createQuery("FROM CourierInfoEntity WHERE courierId = :courierId", CourierInfoEntity.class);
        query.setParameter("courierId", courierEntity.getCourierId());

        CourierInfoEntity savedCourierEntity = (CourierInfoEntity) query.uniqueResult();
        Assertions.assertNotNull(savedCourierEntity);
        Assertions.assertEquals("Test", savedCourierEntity.getFirstName());
        Assertions.assertEquals("Courier", savedCourierEntity.getLastName());
        Assertions.assertEquals("+7 123 456 7890", savedCourierEntity.getPhoneNumber());
        Assertions.assertEquals("bike", savedCourierEntity.getDeliveryType());
    }

    @Test
    @Order(4)
    void deleteCourier_whenValid_shouldDelete() {
        // Given
        final Query query = getSession().createQuery("FROM CourierInfoEntity WHERE courierId = :courierId", CourierInfoEntity.class);
        query.setParameter("courierId", (short) 5);

        Optional<CourierInfoEntity> courierEntityOptional = query.uniqueResultOptional();
        Assumptions.assumeTrue(courierEntityOptional.isPresent());

        // When
        Session session = getSession();
        session.beginTransaction();
        session.delete(courierEntityOptional.get());
        session.getTransaction().commit();

        // Then
        final Query queryAfterDelete = getSession().createQuery("FROM CourierInfoEntity WHERE courierId = :courierId", CourierInfoEntity.class);
        queryAfterDelete.setParameter("courierId", (short) 5);

        Optional<CourierInfoEntity> courierEntityAfterDelete = queryAfterDelete.uniqueResultOptional();
        Assertions.assertFalse(courierEntityAfterDelete.isPresent());
    }

    @Test
    @Order(5)
    void addCourier_whenNotValid_shouldThrow() {
        // Given
        CourierInfoEntity courierEntity = new CourierInfoEntity();

        // When
        Session session = getSession();
        session.beginTransaction();
        session.persist(courierEntity);

        // Then
        Assertions.assertThrows(PersistenceException.class, () -> session.getTransaction().commit());
    }
}

