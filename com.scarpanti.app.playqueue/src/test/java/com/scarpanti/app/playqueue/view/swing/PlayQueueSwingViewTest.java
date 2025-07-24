package com.scarpanti.app.playqueue.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.scarpanti.app.playqueue.controller.GenreController;
import com.scarpanti.app.playqueue.controller.PlayQueueController;
import com.scarpanti.app.playqueue.model.Genre;
import com.scarpanti.app.playqueue.model.Song;

@RunWith(GUITestRunner.class)
public class PlayQueueSwingViewTest extends AssertJSwingJUnitTestCase {

	@Mock
	private GenreController genreController;

	@Mock
	private PlayQueueController playQueueController;

	private AutoCloseable closeable;
	private PlayQueueSwingView view;
	private FrameFixture window;

	@Override
	protected void onSetUp() throws Exception {
		closeable = MockitoAnnotations.openMocks(this);
		GuiActionRunner.execute(() -> {
			view = new PlayQueueSwingView();
			view.setGenreController(genreController);
			view.setPlayQueueController(playQueueController);
			return view;
		});

		window = new FrameFixture(robot(), view);
		window.show();
	}

	@Override
	protected void onTearDown() throws Exception {
		closeable.close();
	}

	@Test
	@GUITest
	public void testInitialConfiguration() {
		window.requireTitle("PlayQueue");

		window.label(JLabelMatcher.withText("Play Queue"));
		window.list("playQueueList").requireEnabled();
		window.button("playNextButton").requireDisabled();
		window.button("removeSelectedButton").requireDisabled();
		window.button("cleanQueueButton").requireDisabled();

		window.label(JLabelMatcher.withText("Music Library"));
		window.label(JLabelMatcher.withText("Genres:"));
		window.list("genreList").requireEnabled();

		window.label(JLabelMatcher.withText("Songs:"));
		window.list("songList").requireEnabled();
		window.button("addToPlayQueueButton").requireDisabled();

		verify(genreController).loadGenres();
		verify(playQueueController).getPlayQueue();
	}

	@Test
	@GUITest
	public void testShowGenres() {
		Genre rock = new Genre("Rock", "Rock music");
		Genre jazz = new Genre("Jazz", "Jazz music");
		GuiActionRunner.execute(() -> {
			view.showGenres(Arrays.asList(rock, jazz));
		});
		assertThat(window.list("genreList").contents()).containsExactly(rock.toString(), jazz.toString());
	}

	@Test
	@GUITest
	public void testShowSongs() {
		Genre rock = new Genre("Rock", "Rock music");
		Song song = new Song(1L, "Bohemian Rhapsody", "Queen", 354, rock);
		GuiActionRunner.execute(() -> {
			view.showSongs(Arrays.asList(song));
		});
		assertThat(window.list("songList").contents()).containsExactly(song.toString());
	}

	@Test
	@GUITest
	public void testShowPlayQueue() {
		Genre rock = new Genre("Rock", "Rock music");
		Song song1 = new Song(1L, "Bohemian Rhapsody", "Queen", 354, rock);
		Song song2 = new Song(2L, "Stairway to Heaven", "Led Zeppelin", 482, rock);
		Map<Long, Song> queueMap = new LinkedHashMap<>();
		queueMap.put(1L, song1);
		queueMap.put(2L, song2);
		GuiActionRunner.execute(() -> {
			view.showQueue(queueMap);
		});
		assertThat(window.list("playQueueList").contents()).containsExactly(song1.toString(), song2.toString());
	}

}