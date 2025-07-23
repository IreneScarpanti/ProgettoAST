package com.scarpanti.app.playqueue.transaction.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.scarpanti.app.playqueue.repository.jpa.JpaGenreRepository;
import com.scarpanti.app.playqueue.repository.jpa.JpaPlayQueueRepository;
import com.scarpanti.app.playqueue.repository.jpa.JpaSongRepository;
import com.scarpanti.app.playqueue.transaction.TransactionCode;
import com.scarpanti.app.playqueue.transaction.TransactionManager;

public class JpaTransactionManager implements TransactionManager {

	private final EntityManagerFactory entityManagerFactory;

	public JpaTransactionManager(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	@Override
	public <T> T doInTransaction(TransactionCode<T> code) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();

		try {
			entityManager.getTransaction().begin();

			JpaGenreRepository genreRepository = new JpaGenreRepository(entityManager);
			JpaSongRepository songRepository = new JpaSongRepository(entityManager);
			JpaPlayQueueRepository playQueueRepository = new JpaPlayQueueRepository(entityManager);

			T result = code.execute(genreRepository, songRepository, playQueueRepository);

			entityManager.getTransaction().commit();
			return result;

		} catch (Exception e) {
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			throw new RuntimeException("Transaction failed", e);
		} finally {
			entityManager.close();
		}
	}
}