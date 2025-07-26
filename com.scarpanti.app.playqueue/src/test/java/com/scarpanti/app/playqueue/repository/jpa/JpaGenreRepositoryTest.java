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
import com.scarpanti.app.playqueue.model.Genre;

public class JpaGenreRepositoryTest {

	private static final String ROCK_NAME = "Rock";
	private static final String ROCK_DESCRIPTION = "Rock music";
	private static final String JAZZ_NAME = "Jazz";
	private static final String JAZZ_DESCRIPTION = "Jazz music";
	private static final String CLASSICAL_NAME = "Classical";
	private static final String CLASSICAL_DESCRIPTION = "Classical music";

	@ClassRule
	public static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

	private EntityManagerFactory emf;
	private EntityManager em;
	private JpaGenreRepository genreRepository;

	@Before
	public void setUp() {
		Map<String, String> properties = new HashMap<>();
		properties.put("javax.persistence.jdbc.url", mysql.getJdbcUrl());
		properties.put("javax.persistence.jdbc.user", mysql.getUsername());
		properties.put("javax.persistence.jdbc.password", mysql.getPassword());
		emf = Persistence.createEntityManagerFactory("playqueue-test", properties);
		em = emf.createEntityManager();
		genreRepository = new JpaGenreRepository(em);
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
	public void testGetAllGenresWhenEmpty() {
		List<Genre> genres = genreRepository.getAllGenres();

		assertThat(genres).isEmpty();
	}

	@Test
	public void testGetAllGenresWithOneGenre() {
		insertGenreInDB(new GenreEntity(ROCK_NAME, ROCK_DESCRIPTION));

		List<Genre> genres = genreRepository.getAllGenres();

		assertThat(genres).containsExactly(new Genre(ROCK_NAME, ROCK_DESCRIPTION));
	}

	@Test
	public void testGetAllGenresWithMultipleGenresOrderedByName() {
		insertGenreInDB(new GenreEntity(ROCK_NAME, ROCK_DESCRIPTION));
		insertGenreInDB(new GenreEntity(JAZZ_NAME, JAZZ_DESCRIPTION));
		insertGenreInDB(new GenreEntity(CLASSICAL_NAME, CLASSICAL_DESCRIPTION));

		List<Genre> genres = genreRepository.getAllGenres();

		assertThat(genres).containsExactly(new Genre(CLASSICAL_NAME, CLASSICAL_DESCRIPTION),
				new Genre(JAZZ_NAME, JAZZ_DESCRIPTION), new Genre(ROCK_NAME, ROCK_DESCRIPTION));
	}

	private void insertGenreInDB(GenreEntity genre) {
		em.getTransaction().begin();
		em.persist(genre);
		em.getTransaction().commit();
		em.clear();
	}
}