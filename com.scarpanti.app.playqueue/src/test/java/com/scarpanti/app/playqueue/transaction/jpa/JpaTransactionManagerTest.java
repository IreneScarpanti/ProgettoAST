package com.scarpanti.app.playqueue.transaction.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.MySQLContainer;

import com.scarpanti.app.playqueue.entity.GenreEntity;
import com.scarpanti.app.playqueue.entity.SongEntity;
import com.scarpanti.app.playqueue.model.Genre;
import com.scarpanti.app.playqueue.model.Song;

public class JpaTransactionManagerTest {

	@ClassRule
	public static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

	private EntityManagerFactory entityManagerFactory;
	private JpaTransactionManager transactionManager;
	private Song testSong;

	@Before
	public void setUp() {
		Map<String, String> properties = new HashMap<>();
		properties.put("javax.persistence.jdbc.url", mysql.getJdbcUrl());
		properties.put("javax.persistence.jdbc.user", mysql.getUsername());
		properties.put("javax.persistence.jdbc.password", mysql.getPassword());
		properties.put("javax.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
		properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
		properties.put("hibernate.hbm2ddl.auto", "create-drop");

		entityManagerFactory = Persistence.createEntityManagerFactory("playqueue-test", properties);
		transactionManager = new JpaTransactionManager(entityManagerFactory);
	}

	@After
	public void tearDown() {
		if (entityManagerFactory != null) {
			entityManagerFactory.close();
		}
	}

	@Test
	public void testExecuteTransactionCodeAndReturnResult() {
		String expectedResult = "test result";

		String result = transactionManager.doInTransaction((genreRepo, songRepo, queueRepo) -> {
			return expectedResult;
		});

		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	public void testProvideThreeRepositories() {
		transactionManager.doInTransaction((genreRepo, songRepo, queueRepo) -> {
			assertThat(genreRepo).isNotNull();
			assertThat(songRepo).isNotNull();
			assertThat(queueRepo).isNotNull();
			return null;
		});
	}

	@Test
	public void testWrapAndRethrowExceptions() {
		RuntimeException originalException = new RuntimeException("Original error");

		assertThatThrownBy(() -> transactionManager.doInTransaction((genreRepo, songRepo, queueRepo) -> {
			throw originalException;
		})).isInstanceOf(RuntimeException.class).hasMessage("Transaction failed").hasCause(originalException);
	}

	@Test
	public void testCommitTransactionOnSuccess() {
		setupTestData();

		transactionManager.doInTransaction((genreRepo, songRepo, queueRepo) -> {
			queueRepo.enqueue(testSong);
			return null;
		});

		long count = countQueueItemsDirectly();
		assertThat(count).isEqualTo(1);
	}

	@Test
	public void testRollbackTransactionOnException() {
		setupTestData();
		long initialCount = countQueueItemsDirectly();

		assertThatThrownBy(() -> transactionManager.doInTransaction((genreRepo, songRepo, queueRepo) -> {
			queueRepo.enqueue(testSong);
			throw new RuntimeException("Test exception");
		})).isInstanceOf(RuntimeException.class);

		long finalCount = countQueueItemsDirectly();
		assertThat(finalCount).isEqualTo(initialCount);
	}

	@Test
	public void testHandleExceptionWhenTransactionNotActive() {
		EntityManagerFactory mockEmf = mock(EntityManagerFactory.class);
		EntityManager mockEm = mock(EntityManager.class);
		EntityTransaction mockTransaction = mock(EntityTransaction.class);

		when(mockEmf.createEntityManager()).thenReturn(mockEm);
		when(mockEm.getTransaction()).thenReturn(mockTransaction);
		when(mockTransaction.isActive()).thenReturn(false);

		JpaTransactionManager mockTransactionManager = new JpaTransactionManager(mockEmf);

		assertThatThrownBy(() -> mockTransactionManager.doInTransaction((genreRepo, songRepo, queueRepo) -> {
			throw new RuntimeException("Test exception");
		})).isInstanceOf(RuntimeException.class).hasMessage("Transaction failed");

		org.mockito.Mockito.verify(mockTransaction, org.mockito.Mockito.never()).rollback();
		org.mockito.Mockito.verify(mockEm).close();
	}

	private void setupTestData() {
		EntityManager em = entityManagerFactory.createEntityManager();
		try {
			em.getTransaction().begin();

			GenreEntity rockGenre = new GenreEntity("Rock", "Rock music");
			em.persist(rockGenre);

			SongEntity songEntity = new SongEntity("Test Song", "Test Artist", 180, rockGenre);
			em.persist(songEntity);

			em.getTransaction().commit();

			testSong = new Song(songEntity.getId(), "Test Song", "Test Artist", 180, new Genre("Rock", "Rock music"));
		} finally {
			em.close();
		}
	}

	private long countQueueItemsDirectly() {
		EntityManager em = entityManagerFactory.createEntityManager();
		try {
			return em.createQuery("SELECT COUNT(q) FROM PlayQueueItemEntity q", Long.class).getSingleResult();
		} finally {
			em.close();
		}
	}
}