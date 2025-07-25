package com.scarpanti.app.playqueue.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.MySQLContainer;

import com.scarpanti.app.playqueue.entity.GenreEntity;
import com.scarpanti.app.playqueue.entity.SongEntity;
import com.scarpanti.app.playqueue.model.Genre;
import com.scarpanti.app.playqueue.model.Song;
import com.scarpanti.app.playqueue.transaction.jpa.JpaTransactionManager;
import com.scarpanti.app.playqueue.view.PlayQueueView;

public class ControllerIT {

	@Mock
	private PlayQueueView playQueueView;

	@ClassRule
	public static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

	private EntityManagerFactory entityManagerFactory;
	private JpaTransactionManager transactionManager;
	private AutoCloseable closeable;

	private GenreController genreController;
	private SongController songController;
	private PlayQueueController playQueueController;

	private Genre rockGenre;
	private Genre jazzGenre;
	private Song bohemianRhapsody;
	private Song stairwayToHeaven;
	private Song takeFive;

	@Before
	public void setUp() {
		closeable = MockitoAnnotations.openMocks(this);

		Map<String, String> properties = new HashMap<>();
		properties.put("javax.persistence.jdbc.url", mysql.getJdbcUrl());
		properties.put("javax.persistence.jdbc.user", mysql.getUsername());
		properties.put("javax.persistence.jdbc.password", mysql.getPassword());
		properties.put("javax.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
		properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
		properties.put("hibernate.hbm2ddl.auto", "create-drop");
		properties.put("hibernate.show_sql", "false");

		entityManagerFactory = Persistence.createEntityManagerFactory("playqueue-test", properties);
		transactionManager = new JpaTransactionManager(entityManagerFactory);

		genreController = new GenreController(transactionManager, playQueueView);
		songController = new SongController(transactionManager, playQueueView);
		playQueueController = new PlayQueueController(transactionManager, playQueueView);

		setupTestData();
	}

	@After
	public void tearDown() throws Exception {
		closeable.close();
		if (entityManagerFactory != null)
			entityManagerFactory.close();
	}

	@Test
	public void testLoadGenres() {
		genreController.loadGenres();
		verify(playQueueView).showGenres(Arrays.asList(jazzGenre, rockGenre));
	}

	@Test
	public void testOnGenreSelectedShowsSongs() {
		songController.onGenreSelected(rockGenre);
		verify(playQueueView).showSongs(Arrays.asList(bohemianRhapsody, stairwayToHeaven));
	}

	@Test
	public void testOnSongSelectedAddsToPlayQueue() {
		playQueueController.onSongSelected(bohemianRhapsody);
		verify(playQueueView).showQueue(Map.of(1L, bohemianRhapsody));
		verifyNoMoreInteractions(playQueueView);
	}

	@Test
	public void testOnSongSelectedMultipleSongsMaintainsOrder() {
		playQueueController.onSongSelected(bohemianRhapsody);
		verify(playQueueView).showQueue(Map.of(1L, bohemianRhapsody));

		playQueueController.onSongSelected(stairwayToHeaven);
		Map<Long, Song> queueAfterSecond = new LinkedHashMap<>();
		queueAfterSecond.put(1L, bohemianRhapsody);
		queueAfterSecond.put(2L, stairwayToHeaven);
		verify(playQueueView).showQueue(queueAfterSecond);

		verifyNoMoreInteractions(playQueueView);
	}

	@Test
	public void testOnPlayNextRemovesFirstSongFromQueue() {
		playQueueController.onSongSelected(bohemianRhapsody);
		verify(playQueueView).showQueue(Map.of(1L, bohemianRhapsody));

		playQueueController.onSongSelected(stairwayToHeaven);
		Map<Long, Song> queueAfterSecond = new LinkedHashMap<>();
		queueAfterSecond.put(1L, bohemianRhapsody);
		queueAfterSecond.put(2L, stairwayToHeaven);
		verify(playQueueView).showQueue(queueAfterSecond);

		playQueueController.onPlayNext();

		verify(playQueueView).showQueue(Map.of(2L, stairwayToHeaven));
		verifyNoMoreInteractions(playQueueView);
	}

	@Test
	public void testOnSongRemovedRemovesThatSong() {
		playQueueController.onSongSelected(bohemianRhapsody);
		Map<Long, Song> firstQueue = Map.of(1L, bohemianRhapsody);
		verify(playQueueView).showQueue(firstQueue);

		playQueueController.onSongSelected(stairwayToHeaven);
		Map<Long, Song> secondQueue = new LinkedHashMap<>();
		secondQueue.put(1L, bohemianRhapsody);
		secondQueue.put(2L, stairwayToHeaven);
		verify(playQueueView).showQueue(secondQueue);

		playQueueController.onSongSelected(takeFive);
		Map<Long, Song> thirdQueue = new LinkedHashMap<>();
		thirdQueue.put(1L, bohemianRhapsody);
		thirdQueue.put(2L, stairwayToHeaven);
		thirdQueue.put(3L, takeFive);
		verify(playQueueView).showQueue(thirdQueue);

		playQueueController.onSongRemoved(2L);
		Map<Long, Song> finalQueue = new LinkedHashMap<>();
		finalQueue.put(1L, bohemianRhapsody);
		finalQueue.put(3L, takeFive);
		verify(playQueueView).showQueue(finalQueue);

		verifyNoMoreInteractions(playQueueView);
	}

	@Test
	public void testClearQueueRemovesAllSongs() {
		playQueueController.onSongSelected(bohemianRhapsody);
		verify(playQueueView).showQueue(Map.of(1L, bohemianRhapsody));

		playQueueController.onSongSelected(stairwayToHeaven);
		Map<Long, Song> queueAfterSecond = new LinkedHashMap<>();
		queueAfterSecond.put(1L, bohemianRhapsody);
		queueAfterSecond.put(2L, stairwayToHeaven);
		verify(playQueueView).showQueue(queueAfterSecond);

		playQueueController.clearQueue();
		verify(playQueueView).showQueue(Map.of());

		verifyNoMoreInteractions(playQueueView);
	}

	private void setupTestData() {
		EntityManager em = entityManagerFactory.createEntityManager();
		try {
			em.getTransaction().begin();

			GenreEntity rockEntity = new GenreEntity("Rock", "Rock music");
			GenreEntity jazzEntity = new GenreEntity("Jazz", "Jazz music");
			em.persist(rockEntity);
			em.persist(jazzEntity);

			SongEntity song1 = new SongEntity("Bohemian Rhapsody", "Queen", 354, rockEntity);
			SongEntity song2 = new SongEntity("Stairway To Heaven", "Led Zeppelin", 482, rockEntity);
			SongEntity song3 = new SongEntity("Take Five", "Dave Brubeck", 324, jazzEntity);

			em.persist(song1);
			em.persist(song2);
			em.persist(song3);

			em.getTransaction().commit();

			rockGenre = new Genre("Rock", "Rock music");
			jazzGenre = new Genre("Jazz", "Jazz music");

			bohemianRhapsody = new Song(song1.getId(), "Bohemian Rhapsody", "Queen", 354, rockGenre);
			stairwayToHeaven = new Song(song2.getId(), "Stairway To Heaven", "Led Zeppelin", 482, rockGenre);
			takeFive = new Song(song3.getId(), "Take Five", "Dave Brubeck", 324, jazzGenre);
		} finally {
			em.close();
		}
	}
}
