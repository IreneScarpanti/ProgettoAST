package com.scarpanti.app.playqueue.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.MySQLContainer;

import com.scarpanti.app.playqueue.entity.GenreEntity;
import com.scarpanti.app.playqueue.entity.PlayQueueItemEntity;
import com.scarpanti.app.playqueue.entity.SongEntity;
import com.scarpanti.app.playqueue.model.Genre;
import com.scarpanti.app.playqueue.model.Song;

public class JpaPlayQueueRepositoryTest {

	private static final String ROCK_NAME = "Rock";
	private static final String ROCK_DESCRIPTION = "Rock music";

	private static final String BOHEMIAN_RHAPSODY_TITLE = "Bohemian Rhapsody";
	private static final String QUEEN_ARTIST = "Queen";
	private static final int BOHEMIAN_RHAPSODY_DURATION = 354;

	private static final String STAIRWAY_TO_HEAVEN_TITLE = "Stairway To Heaven";
	private static final String LED_ZEPPELIN_ARTIST = "Led Zeppelin";
	private static final int STAIRWAY_TO_HEAVEN_DURATION = 482;

	@ClassRule
	public static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

	private EntityManagerFactory emf;
	private EntityManager em;
	private JpaPlayQueueRepository playQueueRepository;

	private GenreEntity rockEntity;
	private SongEntity bohemianRhapsodyEntity;
	private SongEntity stairwayToHeavenEntity;

	@Before
	public void setUp() {
		Map<String, String> properties = new HashMap<>();
		properties.put("javax.persistence.jdbc.url", mysql.getJdbcUrl());
		properties.put("javax.persistence.jdbc.user", mysql.getUsername());
		properties.put("javax.persistence.jdbc.password", mysql.getPassword());
		properties.put("javax.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
		properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
		properties.put("hibernate.hbm2ddl.auto", "create-drop");
		properties.put("hibernate.show_sql", "true");

		emf = Persistence.createEntityManagerFactory("playqueue-test", properties);
		em = emf.createEntityManager();
		playQueueRepository = new JpaPlayQueueRepository(em);

		setupBaseEntities();

	}

	@After
	public void tearDown() {
		if (em != null) {
			em.close();
		}
		if (emf != null) {
			emf.close();
		}
	}

	@Test
	public void testGetAllSongsWhenEmpty() {
		Map<Long, Song> songs = playQueueRepository.getAllSongs();

		assertThat(songs).isEmpty();
	}

	@Test
	public void testGetAllSongsWhenThereAreSongs() {
		insertPlayQueueItemDirectly(bohemianRhapsodyEntity);
		insertPlayQueueItemDirectly(stairwayToHeavenEntity);

		Map<Long, Song> songs = playQueueRepository.getAllSongs();

		Song bohemianRhapsody = createSongFromEntity(bohemianRhapsodyEntity);
		Song stairwayToHeaven = createSongFromEntity(stairwayToHeavenEntity);

		assertThat(songs).hasSize(2);
		assertThat(songs.keySet()).containsExactly(1L, 2L);
		assertThat(songs.values()).containsExactly(bohemianRhapsody, stairwayToHeaven);
	}

	@Test
	public void testEnqueue() {
		Song bohemianRhapsody = createSongFromEntity(bohemianRhapsodyEntity);

		em.getTransaction().begin();
		playQueueRepository.enqueue(bohemianRhapsody);
		em.getTransaction().commit();

		List<PlayQueueItemEntity> queueItems = em
				.createQuery("SELECT pq FROM PlayQueueItemEntity pq", PlayQueueItemEntity.class).getResultList();

		assertThat(queueItems).hasSize(1);
		assertThat(queueItems.get(0).getSong().getId()).isEqualTo(bohemianRhapsody.getId());
		assertThat(queueItems.get(0).getQueueId()).isEqualTo(1L);
	}

	@Test
	public void testEnqueueMaintainsFIFOOrder() {
		Song bohemianRhapsody = createSongFromEntity(bohemianRhapsodyEntity);
		Song stairwayToHeaven = createSongFromEntity(stairwayToHeavenEntity);

		em.getTransaction().begin();
		playQueueRepository.enqueue(bohemianRhapsody);
		playQueueRepository.enqueue(stairwayToHeaven);
		em.getTransaction().commit();

		List<PlayQueueItemEntity> queueItems = em
				.createQuery("SELECT pq FROM PlayQueueItemEntity pq ORDER BY pq.queueId", PlayQueueItemEntity.class)
				.getResultList();

		assertThat(queueItems).hasSize(2);
		assertThat(queueItems.get(0).getQueueId()).isEqualTo(1L);
		assertThat(queueItems.get(1).getQueueId()).isEqualTo(2L);
	}

	@Test
	public void testEnqueueSameSongTwice() {
		Song bohemianRhapsody = createSongFromEntity(bohemianRhapsodyEntity);

		em.getTransaction().begin();
		playQueueRepository.enqueue(bohemianRhapsody);
		playQueueRepository.enqueue(bohemianRhapsody);
		em.getTransaction().commit();

		List<PlayQueueItemEntity> queueItems = em
				.createQuery("SELECT pq FROM PlayQueueItemEntity pq ORDER BY pq.queueId", PlayQueueItemEntity.class)
				.getResultList();

		assertThat(queueItems).hasSize(2);
		assertThat(queueItems.get(0).getQueueId()).isEqualTo(1L);
		assertThat(queueItems.get(1).getQueueId()).isEqualTo(2L);
		assertThat(queueItems.get(0).getSong().getId()).isEqualTo(bohemianRhapsody.getId());
		assertThat(queueItems.get(1).getSong().getId()).isEqualTo(bohemianRhapsody.getId());
	}

	@Test
	public void testDequeue() {
		insertPlayQueueItemDirectly(bohemianRhapsodyEntity);
		insertPlayQueueItemDirectly(stairwayToHeavenEntity);

		em.getTransaction().begin();
		playQueueRepository.dequeue();
		em.getTransaction().commit();

		List<PlayQueueItemEntity> queueItems = em
				.createQuery("SELECT pq FROM PlayQueueItemEntity pq ORDER BY pq.queueId", PlayQueueItemEntity.class)
				.getResultList();

		assertThat(queueItems).hasSize(1);
		assertThat(queueItems.get(0).getQueueId()).isEqualTo(2L);
		assertThat(queueItems.get(0).getSong().getId()).isEqualTo(stairwayToHeavenEntity.getId());
	}

	@Test
	public void testDequeueWhenEmpty() {
		em.getTransaction().begin();
		playQueueRepository.dequeue();
		em.getTransaction().commit();

		List<PlayQueueItemEntity> queueItems = em
				.createQuery("SELECT pq FROM PlayQueueItemEntity pq", PlayQueueItemEntity.class).getResultList();

		assertThat(queueItems).isEmpty();
	}

	@Test
	public void testRemove() {
		insertPlayQueueItemDirectly(bohemianRhapsodyEntity);
		insertPlayQueueItemDirectly(stairwayToHeavenEntity);

		em.getTransaction().begin();
		playQueueRepository.remove(1L);
		em.getTransaction().commit();

		List<PlayQueueItemEntity> queueItems = em
				.createQuery("SELECT pq FROM PlayQueueItemEntity pq ORDER BY pq.queueId", PlayQueueItemEntity.class)
				.getResultList();

		assertThat(queueItems).hasSize(1);
		assertThat(queueItems.get(0).getQueueId()).isEqualTo(2L);
		assertThat(queueItems.get(0).getSong().getId()).isEqualTo(stairwayToHeavenEntity.getId());
	}

	private void setupBaseEntities() {
		em.getTransaction().begin();

		rockEntity = new GenreEntity(ROCK_NAME, ROCK_DESCRIPTION);
		em.persist(rockEntity);

		bohemianRhapsodyEntity = new SongEntity(BOHEMIAN_RHAPSODY_TITLE, QUEEN_ARTIST, BOHEMIAN_RHAPSODY_DURATION,
				rockEntity);
		stairwayToHeavenEntity = new SongEntity(STAIRWAY_TO_HEAVEN_TITLE, LED_ZEPPELIN_ARTIST,
				STAIRWAY_TO_HEAVEN_DURATION, rockEntity);
		em.persist(bohemianRhapsodyEntity);
		em.persist(stairwayToHeavenEntity);

		em.getTransaction().commit();
		em.clear();
	}

	private void insertPlayQueueItemDirectly(SongEntity songEntity) {
		em.getTransaction().begin();
		PlayQueueItemEntity queueItem = new PlayQueueItemEntity(songEntity);
		em.persist(queueItem);
		em.getTransaction().commit();
		em.clear();
	}

	private Song createSongFromEntity(SongEntity entity) {
		return new Song(entity.getId(), entity.getTitle(), entity.getArtist(), entity.getDuration(),
				new Genre(ROCK_NAME, ROCK_DESCRIPTION));
	}

}