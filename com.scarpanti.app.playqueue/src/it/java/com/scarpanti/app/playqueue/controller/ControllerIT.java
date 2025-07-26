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

	private static final String DB_NAME = "playqueue-test";

	private static final String ROCK_NAME = "Rock";
	private static final String ROCK_DESCRIPTION = "Rock music";
	private static final String JAZZ_NAME = "Jazz";
	private static final String JAZZ_DESCRIPTION = "Jazz music";

	private static final String BOHEMIAN_RHAPSODY_TITLE = "Bohemian Rhapsody";
	private static final String QUEEN_ARTIST = "Queen";
	private static final int BOHEMIAN_RHAPSODY_DURATION = 354;

	private static final String STAIRWAY_TO_HEAVEN_TITLE = "Stairway To Heaven";
	private static final String LED_ZEPPELIN_ARTIST = "Led Zeppelin";
	private static final int STAIRWAY_TO_HEAVEN_DURATION = 482;

	private static final String TAKE_FIVE_TITLE = "Take Five";
	private static final String DAVE_BRUBECK_ARTIST = "Dave Brubeck";
	private static final int TAKE_FIVE_DURATION = 324;
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

		entityManagerFactory = Persistence.createEntityManagerFactory(DB_NAME, properties);
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

			GenreEntity rockEntity = new GenreEntity(ROCK_NAME, ROCK_DESCRIPTION);
			GenreEntity jazzEntity = new GenreEntity(JAZZ_NAME, JAZZ_DESCRIPTION);
			em.persist(rockEntity);
			em.persist(jazzEntity);

			SongEntity bohemianRhapsodyEntity = new SongEntity(BOHEMIAN_RHAPSODY_TITLE, QUEEN_ARTIST, 
					BOHEMIAN_RHAPSODY_DURATION, rockEntity);
			SongEntity stairwayToHeavenEntity = new SongEntity(STAIRWAY_TO_HEAVEN_TITLE, LED_ZEPPELIN_ARTIST, 
					STAIRWAY_TO_HEAVEN_DURATION, rockEntity);
			SongEntity takeFiveEntity = new SongEntity(TAKE_FIVE_TITLE, DAVE_BRUBECK_ARTIST, 
					TAKE_FIVE_DURATION, jazzEntity);

			em.persist(bohemianRhapsodyEntity);
			em.persist(stairwayToHeavenEntity);
			em.persist(takeFiveEntity);

			em.getTransaction().commit();

			rockGenre = new Genre(ROCK_NAME, ROCK_DESCRIPTION);
			jazzGenre = new Genre(JAZZ_NAME, JAZZ_DESCRIPTION);

			bohemianRhapsody = new Song(bohemianRhapsodyEntity.getId(), BOHEMIAN_RHAPSODY_TITLE, QUEEN_ARTIST, 
					BOHEMIAN_RHAPSODY_DURATION, rockGenre);
			stairwayToHeaven = new Song(stairwayToHeavenEntity.getId(), STAIRWAY_TO_HEAVEN_TITLE, LED_ZEPPELIN_ARTIST, 
					STAIRWAY_TO_HEAVEN_DURATION, rockGenre);
			takeFive = new Song(takeFiveEntity.getId(), TAKE_FIVE_TITLE, DAVE_BRUBECK_ARTIST, 
					TAKE_FIVE_DURATION, jazzGenre);
		} finally {
			em.close();
		}
	}
}
