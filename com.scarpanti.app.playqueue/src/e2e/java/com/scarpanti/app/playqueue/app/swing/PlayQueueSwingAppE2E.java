package com.scarpanti.app.playqueue.app.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.launcher.ApplicationLauncher.application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.MySQLContainer;

import com.scarpanti.app.playqueue.entity.GenreEntity;
import com.scarpanti.app.playqueue.entity.SongEntity;

@RunWith(GUITestRunner.class)
public class PlayQueueSwingAppE2E extends AssertJSwingJUnitTestCase {

	private static final String BOHEMIAN_RHAPSODY_TITLE = "Bohemian Rhapsody";
	private static final String QUEEN_ARTIST = "Queen";

	private static final String STAIRWAY_TO_HEAVEN_TITLE = "Stairway To Heaven";
	private static final String LED_ZEPPELIN_ARTIST = "Led Zeppelin";

	@ClassRule
	public static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

	private EntityManagerFactory entityManagerFactory;
	private FrameFixture window;

	@Override
	protected void onSetUp() throws Exception {
		String containerHost = mysql.getHost();
		Integer mappedPort = mysql.getFirstMappedPort();

		String jdbcUrl = "jdbc:mysql://" + containerHost + ":" + mappedPort + "/";
		Connection connection = DriverManager.getConnection(jdbcUrl, mysql.getUsername(), mysql.getPassword());

		try (Statement stmt = connection.createStatement()) {
			stmt.execute("DROP DATABASE IF EXISTS " + mysql.getDatabaseName());
			stmt.execute("CREATE DATABASE " + mysql.getDatabaseName());
		}
		connection.close();

		Map<String, String> properties = new HashMap<>();
		properties.put("javax.persistence.jdbc.url",
				"jdbc:mysql://" + containerHost + ":" + mappedPort + "/" + mysql.getDatabaseName());
		properties.put("javax.persistence.jdbc.user", mysql.getUsername());
		properties.put("javax.persistence.jdbc.password", mysql.getPassword());

		entityManagerFactory = Persistence.createEntityManagerFactory("playqueue-test", properties);
		EntityManager em = entityManagerFactory.createEntityManager();

		try {
			em.getTransaction().begin();

			GenreEntity rockGenre = new GenreEntity("Rock", "Rock music");
			em.persist(rockGenre);
			GenreEntity jazzGenre = new GenreEntity("Jazz", "Jazz music");
			em.persist(jazzGenre);
			GenreEntity popGenre = new GenreEntity("Pop", "Pop music");
			em.persist(popGenre);

			em.persist(new SongEntity("Bohemian Rhapsody", "Queen", 354, rockGenre));
			em.persist(new SongEntity("Stairway To Heaven", "Led Zeppelin", 482, rockGenre));
			em.persist(new SongEntity("Take Five", "Dave Brubeck", 324, jazzGenre));
			em.persist(new SongEntity("Billie Jean", "Michael Jackson", 294, popGenre));

			em.getTransaction().commit();
		} finally {
			em.close();
		}

		application("com.scarpanti.app.playqueue.app.swing.PlayQueueSwingApp").withArgs("--db-host=" + containerHost,
				"--db-port=" + mappedPort.toString(), "--db-name=" + mysql.getDatabaseName(),
				"--db-user=" + mysql.getUsername(), "--db-password=" + mysql.getPassword()).start();

		window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return "PlayQueue".equals(frame.getTitle()) && frame.isShowing();
			}
		}).using(robot());

		window.maximize();
	}

	@Override
	public void onTearDown() throws Exception {
		if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
			entityManagerFactory.close();
		}
	}

	@Test
	@GUITest
	public void testGenresAreShowedOnStartup() {
		assertThat(window.list("genreList").contents()).hasSize(3);
		assertThat(window.list("genreList").contents()).containsExactly("Genre [name=Jazz, description=Jazz music]",
				"Genre [name=Pop, description=Pop music]", "Genre [name=Rock, description=Rock music]");
	}

	@Test
	@GUITest
	public void testClickingGenreShowsSongs() {
		assertThat(window.list("songList").contents()).isEmpty();

		window.list("genreList").selectItem("Genre [name=Rock, description=Rock music]");
		assertThat(window.list("songList").contents()).hasSize(2);
		assertThat(window.list("songList").contents()[0]).contains(BOHEMIAN_RHAPSODY_TITLE).contains(QUEEN_ARTIST);
		assertThat(window.list("songList").contents()[1]).contains(STAIRWAY_TO_HEAVEN_TITLE)
				.contains(LED_ZEPPELIN_ARTIST);
	}

	@Test
	@GUITest
	public void testSwitchBetweenGenres() {
		window.list("genreList").selectItem("Genre [name=Jazz, description=Jazz music]");
		assertThat(window.list("songList").contents()).hasSize(1);
		assertThat(window.list("songList").contents()[0]).contains("Take Five");

		window.list("genreList").selectItem("Genre [name=Pop, description=Pop music]");
		assertThat(window.list("songList").contents()).hasSize(1);
		assertThat(window.list("songList").contents()[0]).contains("Billie Jean");
	}

	@Test
	@GUITest
	public void testSelectSongAndAddToPlayQueue() {
		window.list("genreList").selectItem("Genre [name=Rock, description=Rock music]");

		assertThat(window.list("playQueueList").contents()).isEmpty();

		assertThat(window.button("addToPlayQueueButton").target().isEnabled()).isFalse();

		window.list("songList").selectItem(0);
		assertThat(window.button("addToPlayQueueButton").target().isEnabled()).isTrue();

		window.button("addToPlayQueueButton").click();
		assertThat(window.list("playQueueList").contents()).hasSize(1);
		assertThat(window.list("playQueueList").contents()[0]).contains(BOHEMIAN_RHAPSODY_TITLE).contains(QUEEN_ARTIST);
	}

	@Test
	@GUITest
	public void testPlayNextFunctionality() {
		addSongsToQueue();

		assertThat(window.list("playQueueList").contents()).hasSize(3);
		String firstSong = window.list("playQueueList").contents()[0];
		String secondSong = window.list("playQueueList").contents()[1];
		String thirdSong = window.list("playQueueList").contents()[2];

		window.button("playNextButton").click();

		assertThat(window.list("playQueueList").contents()).hasSize(2);
		assertThat(window.list("playQueueList").contents()).doesNotContain(firstSong);
		assertThat(window.list("playQueueList").contents()[0]).isEqualTo(secondSong);
		assertThat(window.list("playQueueList").contents()[1]).isEqualTo(thirdSong);
	}

	@Test
	@GUITest
	public void testRemoveSpecificSong() {
		addSongsToQueue();

		assertThat(window.list("playQueueList").contents()).hasSize(3);
		String firstSong = window.list("playQueueList").contents()[0];
		String secondSong = window.list("playQueueList").contents()[1];
		String thirdSong = window.list("playQueueList").contents()[2];

		window.list("playQueueList").selectItem(1);
		window.button("removeSelectedButton").click();

		assertThat(window.list("playQueueList").contents()).hasSize(2);
		assertThat(window.list("playQueueList").contents()).doesNotContain(secondSong);
		assertThat(window.list("playQueueList").contents()[0]).isEqualTo(firstSong);
		assertThat(window.list("playQueueList").contents()[1]).isEqualTo(thirdSong);

		window.list("playQueueList").selectItem(0);
		window.button("removeSelectedButton").click();

		assertThat(window.list("playQueueList").contents()).hasSize(1);
		assertThat(window.list("playQueueList").contents()[0]).isEqualTo(thirdSong);
	}

	@Test
	@GUITest
	public void testClearQueue() {
		addSongsToQueue();

		assertThat(window.list("playQueueList").contents()).hasSize(3);

		window.button("cleanQueueButton").click();

		assertThat(window.list("playQueueList").contents()).isEmpty();
	}

	@Test
	@GUITest
	public void testAddSameSongMultipleTimes() {
		window.list("genreList").selectItem(0);

		window.list("songList").selectItem(0);
		window.button("addToPlayQueueButton").click();
		window.button("addToPlayQueueButton").click();
		window.button("addToPlayQueueButton").click();

		assertThat(window.list("playQueueList").contents()).hasSize(3);
		String songName = window.list("playQueueList").contents()[0];
		assertThat(window.list("playQueueList").contents()[1]).isEqualTo(songName);
		assertThat(window.list("playQueueList").contents()[2]).isEqualTo(songName);
	}

	@Test
	@GUITest
	public void testButtonStatesBasedOnQueueContent() {
		assertThat(window.button("playNextButton").target().isEnabled()).isFalse();
		assertThat(window.button("cleanQueueButton").target().isEnabled()).isFalse();
		assertThat(window.button("removeSelectedButton").target().isEnabled()).isFalse();

		window.list("genreList").selectItem("Genre [name=Rock, description=Rock music]");
		window.list("songList").selectItem(0);
		window.button("addToPlayQueueButton").click();

		assertThat(window.button("playNextButton").target().isEnabled()).isTrue();
		assertThat(window.button("cleanQueueButton").target().isEnabled()).isTrue();

		window.list("playQueueList").selectItem(0);
		assertThat(window.button("removeSelectedButton").target().isEnabled()).isTrue();

		window.list("playQueueList").clearSelection();
		assertThat(window.button("removeSelectedButton").target().isEnabled()).isFalse();

		window.button("cleanQueueButton").click();

		assertThat(window.button("playNextButton").target().isEnabled()).isFalse();
		assertThat(window.button("cleanQueueButton").target().isEnabled()).isFalse();
	}

	private void addSongsToQueue() {
		window.list("genreList").selectItem("Genre [name=Rock, description=Rock music]");

		window.list("songList").selectItem(0);
		window.button("addToPlayQueueButton").click();

		window.list("songList").selectItem(1);
		window.button("addToPlayQueueButton").click();

		window.list("genreList").selectItem("Genre [name=Jazz, description=Jazz music]");

		window.list("songList").selectItem(0);
		window.button("addToPlayQueueButton").click();
	}
}