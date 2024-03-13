package org.max.home;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductsTest extends AbstractTest {

    @Test
    @Order(1)
    void getProducts_whenValid_shouldReturn() throws SQLException {
        String sql = "SELECT * FROM products";
        Statement stmt = getConnection().createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        System.out.println("Actual product data:");
        while (rs.next()) {
            System.out.println("Menu Name: " + rs.getString(2) + ", Price: " + rs.getString(3));
        }

        final Query query = getSession().createSQLQuery("SELECT * FROM products").addEntity(ProductsEntity.class);
        List<ProductsEntity> products = query.list();

        System.out.println("Actual products from Hibernate:");
        for (ProductsEntity product : products) {
            System.out.println("Menu Name: " + product.getMenuName() + ", Price: " + product.getPrice());
        }

        Assertions.assertEquals(10, products.size());
    }

    @Order(2)
    @ParameterizedTest
    @CsvSource({"GOJIRA ROLL,300.0", "VIVA LAS VEGAS ROLL,450.0", "FUTOMAKI,700.0"})
    void getProductById_whenValid_shouldReturn(String menuName, String expectedPrice) throws SQLException {
        String sql = "SELECT * FROM products WHERE menu_name='" + menuName + "'";
        Statement stmt = getConnection().createStatement();
        String actualPrice = "0.0";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            actualPrice = rs.getString(3);
        }
        Assertions.assertEquals(new BigDecimal(expectedPrice), new BigDecimal(actualPrice));
    }

    @Test
    @Order(3)
    void addProduct_whenValid_shouldSave() {
        ProductsEntity entity = new ProductsEntity();
        entity.setProductId((short) 11);
        entity.setMenuName("SPICY TUNA ROLL");
        entity.setPrice("250.0");
        Session session = getSession();
        session.beginTransaction();
        session.persist(entity);
        session.getTransaction().commit();

        final Query query = getSession()
                .createSQLQuery("SELECT * FROM products WHERE product_id=" + 11).addEntity(ProductsEntity.class);
        ProductsEntity productEntity = (ProductsEntity) query.uniqueResult();
        Assertions.assertNotNull(productEntity);
        Assertions.assertEquals(new BigDecimal("250.0"), new BigDecimal(productEntity.getPrice()));
    }

    @Test
    @Order(4)
    void deleteProduct_whenValid_shouldDelete() {
        final Query query = getSession()
                .createSQLQuery("SELECT * FROM products WHERE product_id=" + 11).addEntity(ProductsEntity.class);
        Optional<ProductsEntity> productEntity = (Optional<ProductsEntity>) query.uniqueResultOptional();
        Assumptions.assumeTrue(productEntity.isPresent());
        Session session = getSession();
        session.beginTransaction();
        session.delete(productEntity.get());
        session.getTransaction().commit();

        final Query queryAfterDelete = getSession()
                .createSQLQuery("SELECT * FROM products WHERE product_id=" + 11).addEntity(ProductsEntity.class);
        Optional<ProductsEntity> productEntityAfterDelete = (Optional<ProductsEntity>) queryAfterDelete.uniqueResultOptional();
        Assertions.assertFalse(productEntityAfterDelete.isPresent());
    }

    @Test
    @Order(5)
    void addProduct_whenNotValid_shouldThrow() {
        ProductsEntity entity = new ProductsEntity();
        Session session = getSession();
        session.beginTransaction();
        session.persist(entity);
        Assertions.assertThrows(PersistenceException.class, () -> session.getTransaction().commit());
    }
}















