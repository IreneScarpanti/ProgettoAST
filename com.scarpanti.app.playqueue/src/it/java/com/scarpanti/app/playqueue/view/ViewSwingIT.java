package com.scarpanti.app.playqueue.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.MySQLContainer;

import com.scarpanti.app.playqueue.controller.GenreController;
import com.scarpanti.app.playqueue.controller.PlayQueueController;
import com.scarpanti.app.playqueue.controller.SongController;
import com.scarpanti.app.playqueue.entity.GenreEntity;
import com.scarpanti.app.playqueue.entity.SongEntity;
import com.scarpanti.app.playqueue.model.Genre;
import com.scarpanti.app.playqueue.model.Song;
import com.scarpanti.app.playqueue.transaction.jpa.JpaTransactionManager;
import com.scarpanti.app.playqueue.view.swing.PlayQueueSwingView;

@RunWith(GUITestRunner.class)
public class ViewSwingIT extends AssertJSwingJUnitTestCase {

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

	@ClassRule
	public static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

	private EntityManagerFactory entityManagerFactory;
	private JpaTransactionManager transactionManager;
	private GenreController genreController;
	private SongController songController;
	private PlayQueueController playQueueController;
	private PlayQueueSwingView playQueueView;
	private FrameFixture window;

	@Override
	protected void onSetUp() throws Exception {
		Map<String, String> properties = new HashMap<>();
		properties.put("javax.persistence.jdbc.url", mysql.getJdbcUrl());
		properties.put("javax.persistence.jdbc.user", mysql.getUsername());
		properties.put("javax.persistence.jdbc.password", mysql.getPassword());

		entityManagerFactory = Persistence.createEntityManagerFactory(DB_NAME, properties);
		transactionManager = new JpaTransactionManager(entityManagerFactory);

		setupTestData();

		GuiActionRunner.execute(() -> {
			playQueueView = new PlayQueueSwingView();
			genreController = new GenreController(transactionManager, playQueueView);
			songController = new SongController(transactionManager, playQueueView);
			playQueueController = new PlayQueueController(transactionManager, playQueueView);

			playQueueView.setGenreController(genreController);
			playQueueView.setSongController(songController);
			playQueueView.setPlayQueueController(playQueueController);

			return playQueueView;
		});

		window = new FrameFixture(robot(), playQueueView);
		window.show();
	}

	@Override
	public void onTearDown() throws Exception {
		if (entityManagerFactory != null) {
			entityManagerFactory.close();
		}
	}

	@Test
	@GUITest
	public void testInitialConfiguration() {
		assertThat(window.list("genreList").contents()).containsExactly("Genre [name=Jazz, description=Jazz music]",
				"Genre [name=Rock, description=Rock music]");

		assertThat(window.list("songList").contents()).isEmpty();
		assertThat(window.list("playQueueList").contents()).isEmpty();
	}

	@Test
	@GUITest
	public void testSelectGenreShowsSongs() {
		window.list("genreList").selectItem(1);

		Genre rockGenre = new Genre(ROCK_NAME, ROCK_DESCRIPTION);
		Song expectedBohemianRhapsody = new Song(1L, BOHEMIAN_RHAPSODY_TITLE, QUEEN_ARTIST, BOHEMIAN_RHAPSODY_DURATION,
				rockGenre);
		Song expectedStairwayToHeaven = new Song(2L, STAIRWAY_TO_HEAVEN_TITLE, LED_ZEPPELIN_ARTIST,
				STAIRWAY_TO_HEAVEN_DURATION, rockGenre);

		assertThat(window.list("songList").contents()).containsExactly(expectedBohemianRhapsody.toString(),
				expectedStairwayToHeaven.toString());

		window.list("genreList").selectItem(0);

		Genre jazzGenre = new Genre(JAZZ_NAME, JAZZ_DESCRIPTION);
		Song expectedTakeFive = new Song(3L, TAKE_FIVE_TITLE, DAVE_BRUBECK_ARTIST, TAKE_FIVE_DURATION, jazzGenre);

		assertThat(window.list("songList").contents()).containsExactly(expectedTakeFive.toString());
	}

	@Test
	@GUITest
	public void testAddSongToPlayQueue() {
		window.list("genreList").selectItem(1);
		window.list("songList").selectItem(0);
		window.button("addToPlayQueueButton").click();

		String firstSong = window.list("playQueueList").contents()[0];

		window.list("songList").selectItem(1);
		window.button("addToPlayQueueButton").click();

		String secondSong = window.list("playQueueList").contents()[1];

		assertThat(window.list("playQueueList").contents()).containsExactly(firstSong, secondSong);
		assertThat(countQueueItemsInDatabase()).isEqualTo(2);
	}

	@Test
	@GUITest
	public void testPlayNextRemovesFirstSongInQueue() {
		window.list("genreList").selectItem(1);
		window.list("songList").selectItem(0);
		window.button("addToPlayQueueButton").click();
		String firstSong = window.list("playQueueList").contents()[0];

		window.list("songList").selectItem(1);
		window.button("addToPlayQueueButton").click();
		String secondSong = window.list("playQueueList").contents()[1];

		assertThat(window.list("playQueueList").contents()).containsExactly(firstSong, secondSong);

		window.button("playNextButton").click();

		assertThat(window.list("playQueueList").contents()).containsExactly(secondSong);
		assertThat(countQueueItemsInDatabase()).isEqualTo(1);
	}

	@Test
	@GUITest
	public void testRemoveSelectedSong() {
		window.list("genreList").selectItem(1);
		window.list("songList").selectItem(0);
		window.button("addToPlayQueueButton").click();
		String firstSong = window.list("playQueueList").contents()[0];

		window.list("genreList").selectItem(0);
		window.list("songList").selectItem(0);
		window.button("addToPlayQueueButton").click();
		String secondSong = window.list("playQueueList").contents()[1];

		window.list("genreList").selectItem(1);
		window.list("songList").selectItem(1);
		window.button("addToPlayQueueButton").click();
		String thirdSong = window.list("playQueueList").contents()[2];

		assertThat(window.list("playQueueList").contents()).containsExactly(firstSong, secondSong, thirdSong);

		window.list("playQueueList").selectItem(1);
		window.button("removeSelectedButton").click();

		assertThat(window.list("playQueueList").contents()).containsExactly(firstSong, thirdSong);
		assertThat(countQueueItemsInDatabase()).isEqualTo(2);
	}

	@Test
	@GUITest
	public void testClearQueueRemovesAllSongs() {
		window.list("genreList").selectItem(1);
		window.list("songList").selectItem(0);
		window.button("addToPlayQueueButton").click();

		window.list("songList").selectItem(1);
		window.button("addToPlayQueueButton").click();

		window.list("genreList").selectItem(0);
		window.list("songList").selectItem(0);
		window.button("addToPlayQueueButton").click();

		assertThat(window.list("playQueueList").contents()).hasSize(3);
		assertThat(countQueueItemsInDatabase()).isEqualTo(3);

		window.button("cleanQueueButton").click();

		assertThat(window.list("playQueueList").contents()).isEmpty();
		assertThat(countQueueItemsInDatabase()).isZero();
	}

	private void setupTestData() {
		EntityManager em = entityManagerFactory.createEntityManager();
		try {
			em.getTransaction().begin();

			GenreEntity rockEntity = new GenreEntity(ROCK_NAME, ROCK_DESCRIPTION);
			GenreEntity jazzEntity = new GenreEntity(JAZZ_NAME, JAZZ_DESCRIPTION);
			em.persist(rockEntity);
			em.persist(jazzEntity);

			SongEntity bohemianRhapsody = new SongEntity(BOHEMIAN_RHAPSODY_TITLE, QUEEN_ARTIST,
					BOHEMIAN_RHAPSODY_DURATION, rockEntity);
			SongEntity stairwayToHeaven = new SongEntity(STAIRWAY_TO_HEAVEN_TITLE, LED_ZEPPELIN_ARTIST,
					STAIRWAY_TO_HEAVEN_DURATION, rockEntity);
			SongEntity takeFive = new SongEntity(TAKE_FIVE_TITLE, DAVE_BRUBECK_ARTIST, TAKE_FIVE_DURATION, jazzEntity);

			em.persist(bohemianRhapsody);
			em.persist(stairwayToHeaven);
			em.persist(takeFive);

			em.getTransaction().commit();
		} finally {
			em.close();
		}
	}

	private long countQueueItemsInDatabase() {
		EntityManager em = entityManagerFactory.createEntityManager();
		try {
			return em.createQuery("SELECT COUNT(q) FROM PlayQueueItemEntity q", Long.class).getSingleResult();
		} finally {
			em.close();
		}
	}
}