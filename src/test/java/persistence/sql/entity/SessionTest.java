package persistence.sql.entity;

import database.DatabaseServer;
import database.H2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.TestJdbcTemplate;
import persistence.sql.ddl.DdlQueryBuilder;
import persistence.sql.ddl.H2Dialect;
import persistence.sql.ddl.Person;
import persistence.sql.dml.DmlQueryBuilder;

import java.sql.Connection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionTest {
    private DatabaseServer server;
    private Connection connection;
    private TestJdbcTemplate jdbcTemplate;
    private DdlQueryBuilder ddlQueryBuilder;
    private DmlQueryBuilder dmlQueryBuilder;
    private Session entityManager;
    private EntityPersister entityPersister;

    @BeforeEach
    void setUp() throws Exception {
        server = new H2();
        server.start();
        connection = server.getConnection();
        jdbcTemplate = new TestJdbcTemplate(connection);
        ddlQueryBuilder = new DdlQueryBuilder(new H2Dialect());
        dmlQueryBuilder = new DmlQueryBuilder();
        createTableAndVerify();
        entityPersister = new EntityPersister(jdbcTemplate, dmlQueryBuilder);
        entityManager = new Session(entityPersister);
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @DisplayName("Person 객체를 저장하고 조회하고 수정하고 삭제한다.")
    @Test
    void scenario() {
        final Person expectedPerson = new Person(1L, "Kent Beck", 64, "beck@example.com");
        entityManager.persist(expectedPerson);

        final Person actualPerson = entityManager.find(Person.class, 1L);
        assertThat(actualPerson.getId()).isEqualTo(expectedPerson.getId());

        final Person personToUpdate = new Person(1L, "Kent Beck", 60, "youngBeck@example.com");
        entityManager.update(personToUpdate);

        final Person updatedPerson = entityManager.find(Person.class, 1L);
        assertThat(updatedPerson.getAge()).isEqualTo(60);

        entityManager.remove(updatedPerson);
        assertRemove();
    }

    private void assertRemove() {
        final List<Person> query = jdbcTemplate.query(dmlQueryBuilder.select(Person.class), resultSet -> {
            resultSet.next();
            return new Person(
                    resultSet.getLong("id"),
                    resultSet.getString("nick_name"),
                    resultSet.getInt("old"),
                    resultSet.getString("email")
            );
        });
        assertThat(query).hasSize(0);
    }

    private void createTableAndVerify() {
        createTable();
        assertTableCreated();
    }

    private void assertTableCreated() {
        assertTrue(jdbcTemplate.doesTableExist(Person.class), "Table was not created.");
    }

    private void createTable() {
        final String createSql = ddlQueryBuilder.create(Person.class);
        jdbcTemplate.execute(createSql);
    }
}
