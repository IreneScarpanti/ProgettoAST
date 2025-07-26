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
import com.scarpanti.app.playqueue.entity.SongEntity;
import com.scarpanti.app.playqueue.model.Genre;
import com.scarpanti.app.playqueue.model.Song;

public class JpaSongRepositoryTest {

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

	private static final String TAKE_FIVE_TITLE = "Take five";
	private static final String DAVE_BRUBECK_ARTIST = "Dave Brubeck";
	private static final int TAKE_FIVE_DURATION = 324;

	@ClassRule
	public static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

	private EntityManagerFactory emf;
	private EntityManager em;
	private JpaSongRepository songRepository;

	@Before
	public void setUp() {
		Map<String, String> properties = new HashMap<>();
		properties.put("javax.persistence.jdbc.url", mysql.getJdbcUrl());
		properties.put("javax.persistence.jdbc.user", mysql.getUsername());
		properties.put("javax.persistence.jdbc.password", mysql.getPassword());

		emf = Persistence.createEntityManagerFactory("playqueue-test", properties);
		em = emf.createEntityManager();
		songRepository = new JpaSongRepository(em);
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
	public void testGetSongsByGenreWhenThereIsNoSong() {
		Genre rock = new Genre(ROCK_NAME, ROCK_DESCRIPTION);

		List<Song> songs = songRepository.getSongsByGenre(rock);

		assertThat(songs).isEmpty();
	}

	@Test
	public void testGetSongsByGenreWithOneSong() {
		GenreEntity rockEntity = insertGenreInDB(new GenreEntity(ROCK_NAME, ROCK_DESCRIPTION));
		insertSongInDB(new SongEntity(BOHEMIAN_RHAPSODY_TITLE, QUEEN_ARTIST, BOHEMIAN_RHAPSODY_DURATION, rockEntity));

		Genre rock = new Genre(ROCK_NAME, ROCK_DESCRIPTION);
		List<Song> songs = songRepository.getSongsByGenre(rock);

		Song expectedSong = new Song(1L, BOHEMIAN_RHAPSODY_TITLE, QUEEN_ARTIST, BOHEMIAN_RHAPSODY_DURATION, rock);
		assertThat(songs).containsExactly(expectedSong);
	}

	@Test
	public void testGetSongsByGenreWithMultipleSongsFromDifferentGenres() {
		GenreEntity rockEntity = insertGenreInDB(new GenreEntity(ROCK_NAME, ROCK_DESCRIPTION));
		GenreEntity jazzEntity = insertGenreInDB(new GenreEntity(JAZZ_NAME, JAZZ_DESCRIPTION));

		insertSongInDB(new SongEntity(BOHEMIAN_RHAPSODY_TITLE, QUEEN_ARTIST, BOHEMIAN_RHAPSODY_DURATION, rockEntity));
		insertSongInDB(
				new SongEntity(STAIRWAY_TO_HEAVEN_TITLE, LED_ZEPPELIN_ARTIST, STAIRWAY_TO_HEAVEN_DURATION, rockEntity));
		insertSongInDB(new SongEntity(TAKE_FIVE_TITLE, DAVE_BRUBECK_ARTIST, TAKE_FIVE_DURATION, jazzEntity));

		Genre rock = new Genre(ROCK_NAME, ROCK_DESCRIPTION);
		List<Song> songs = songRepository.getSongsByGenre(rock);

		assertThat(songs).hasSize(2);
		assertThat(songs.get(0).getTitle()).isEqualTo(BOHEMIAN_RHAPSODY_TITLE);
		assertThat(songs.get(1).getTitle()).isEqualTo(STAIRWAY_TO_HEAVEN_TITLE);
		assertThat(songs).allMatch(song -> song.getGenre().equals(rock));
	}

	private GenreEntity insertGenreInDB(GenreEntity genre) {
		em.getTransaction().begin();
		em.persist(genre);
		em.getTransaction().commit();
		em.clear();
		return genre;
	}

	private SongEntity insertSongInDB(SongEntity song) {
		em.getTransaction().begin();
		em.persist(song);
		em.getTransaction().commit();
		em.clear();
		return song;
	}
}