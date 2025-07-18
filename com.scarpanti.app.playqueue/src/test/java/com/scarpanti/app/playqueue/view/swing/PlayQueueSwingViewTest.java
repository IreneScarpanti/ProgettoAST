package com.scarpanti.app.playqueue.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;

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
import com.scarpanti.app.playqueue.controller.SongController;
import com.scarpanti.app.playqueue.model.Genre;
import com.scarpanti.app.playqueue.model.Song;

@RunWith(GUITestRunner.class)
public class PlayQueueSwingViewTest extends AssertJSwingJUnitTestCase {

	@Mock
	private GenreController genreController;

	@Mock
	private SongController songController;

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
			view.setSongController(songController);
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

		window.label(JLabelMatcher.withText("Music Library"));
		window.label(JLabelMatcher.withText("Genres:"));
		window.list("genreList").requireEnabled();

		window.label(JLabelMatcher.withText("Songs:"));
		window.list("songList").requireEnabled();
		window.button("addToPlayQueueButton").requireDisabled();

		verify(genreController).loadGenres();
	}

	@Test
	@GUITest
	public void testAddToPlayQueueButtonEnabledWhenSongSelected() {
		Genre rock = new Genre("Rock", "Rock music");
		Song song1 = new Song(1L, "Bohemian Rhapsody", "Queen", 354, rock);
		Song song2 = new Song(2L, "Stairway to Heaven", "Led Zeppelin", 482, rock);

		GuiActionRunner.execute(() -> {
			view.showSongs(Arrays.asList(song1, song2));
		});

		window.list("songList").selectItem(0);

		window.button("addToPlayQueueButton").requireEnabled();
	}

	@Test
	@GUITest
	public void testAddToPlayQueueButtonDisabledWhenNoSongSelected() {
		Genre rock = new Genre("Rock", "Rock music");
		Song song1 = new Song(1L, "Bohemian Rhapsody", "Queen", 354, rock);

		GuiActionRunner.execute(() -> {
			view.showSongs(Arrays.asList(song1));
		});

		window.list("songList").clearSelection();

		window.button("addToPlayQueueButton").requireDisabled();
	}

	@Test
	@GUITest
	public void testPlayNextButtonEnabledWhenQueueNotEmpty() {
		Genre rock = new Genre("Rock", "Rock music");
		Song song1 = new Song(1L, "Bohemian Rhapsody", "Queen", 354, rock);
		Song song2 = new Song(2L, "Stairway to Heaven", "Led Zeppelin", 482, rock);

		GuiActionRunner.execute(() -> {
			view.showQueue(Arrays.asList(song1, song2));
		});

		window.button("playNextButton").requireEnabled();
	}

	@Test
	@GUITest
	public void testPlayNextButtonDisabledWhenQueueEmpty() {
		GuiActionRunner.execute(() -> {
			view.showQueue(Collections.emptyList());
		});

		window.button("playNextButton").requireDisabled();
	}

	@Test
	@GUITest
	public void testRemoveSelectedButtonEnabledWhenQueueSongSelected() {
		Genre rock = new Genre("Rock", "Rock music");
		Song song1 = new Song(1L, "Bohemian Rhapsody", "Queen", 354, rock);
		Song song2 = new Song(2L, "Stairway to Heaven", "Led Zeppelin", 482, rock);

		GuiActionRunner.execute(() -> {
			view.showQueue(Arrays.asList(song1, song2));
		});

		window.list("playQueueList").selectItem(0);

		window.button("removeSelectedButton").requireEnabled();
	}

	@Test
	@GUITest
	public void testRemoveSelectedButtonDisabledWhenNoQueueSongSelected() {
		Genre rock = new Genre("Rock", "Rock music");
		Song song1 = new Song(1L, "Bohemian Rhapsody", "Queen", 354, rock);

		GuiActionRunner.execute(() -> {
			view.showQueue(Arrays.asList(song1));
		});

		window.list("playQueueList").clearSelection();

		window.button("removeSelectedButton").requireDisabled();
	}

	@Test
	@GUITest
	public void testAddToPlayQueueButtonCallsController() {
		Genre rock = new Genre("Rock", "Rock music");
		Song song1 = new Song(1L, "Bohemian Rhapsody", "Queen", 354, rock);

		GuiActionRunner.execute(() -> {
			view.showSongs(Arrays.asList(song1));
		});

		window.list("songList").selectItem(0);

		window.button("addToPlayQueueButton").click();

		verify(playQueueController).onSongSelected(song1);
	}

	@Test
	@GUITest
	public void testPlayNextButtonCallsController() {
		Genre rock = new Genre("Rock", "Rock music");
		Song song1 = new Song(1L, "Bohemian Rhapsody", "Queen", 354, rock);

		GuiActionRunner.execute(() -> {
			view.showQueue(Arrays.asList(song1));
		});

		window.button("playNextButton").click();

		verify(playQueueController).onPlayNext();
	}

	@Test
	@GUITest
	public void testRemoveSelectedButtonCallsController() {
		Genre rock = new Genre("Rock", "Rock music");
		Song song1 = new Song(1L, "Bohemian Rhapsody", "Queen", 354, rock);

		GuiActionRunner.execute(() -> {
			view.showQueue(Arrays.asList(song1));
		});

		window.list("playQueueList").selectItem(0);

		window.button("removeSelectedButton").click();

		verify(playQueueController).onSongRemoved(song1);
	}

	@Test
	@GUITest
	public void testGenreSelectionTriggersShowSongs() {
		Genre rock = new Genre("Rock", "Rock music");
		Genre jazz = new Genre("Jazz", "Jazz music");

		GuiActionRunner.execute(() -> {
			view.showGenres(Arrays.asList(rock, jazz));
		});

		window.list("genreList").selectItem(0);

		verify(songController).onGenreSelected(rock);
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
	public void testShowQueue() {
		Genre rock = new Genre("Rock", "Rock music");
		Song song = new Song(1L, "Bohemian Rhapsody", "Queen", 354, rock);

		GuiActionRunner.execute(() -> {
			view.showQueue(Arrays.asList(song));
		});

		assertThat(window.list("playQueueList").contents()).containsExactly(song.toString());
	}

}