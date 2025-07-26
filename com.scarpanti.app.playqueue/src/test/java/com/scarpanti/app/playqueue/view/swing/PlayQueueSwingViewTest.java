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

	@Test
	@GUITest
	public void testGenreSelectionCallsSongController() {
		Genre rock = new Genre("Rock", "Rock music");
		GuiActionRunner.execute(() -> {
			view.showGenres(Arrays.asList(rock));
		});
		window.list("genreList").selectItem(0);
		verify(songController).onGenreSelected(rock);
	}

	@Test
	@GUITest
	public void testAddToPlayQueueButtonEnabledWhenSongSelected() {
		Genre rock = new Genre("Rock", "Rock music");
		Song song = new Song(1L, "Bohemian Rhapsody", "Queen", 354, rock);
		GuiActionRunner.execute(() -> {
			view.showSongs(Arrays.asList(song));
		});
		window.list("songList").selectItem(0);
		window.button("addToPlayQueueButton").requireEnabled();
	}

	@Test
	@GUITest
	public void testAddToPlayQueueButtonDisablesWhenNoSongSelected() {
		Genre rock = new Genre("Rock", "Rock music");
		Song song = new Song(1L, "Bohemian Rhapsody", "Queen", 354, rock);
		GuiActionRunner.execute(() -> view.showSongs(Arrays.asList(song)));
		window.list("songList").selectItem(0);
		window.button("addToPlayQueueButton").requireEnabled();
		window.list("songList").clearSelection();
		window.button("addToPlayQueueButton").requireDisabled();
	}

	@Test
	@GUITest
	public void testAddToPlayQueueButtonCallsPlayQueueController() {
		Genre rock = new Genre("Rock", "Rock music");
		Song song = new Song(1L, "Bohemian Rhapsody", "Queen", 354, rock);
		GuiActionRunner.execute(() -> {
			view.showSongs(Arrays.asList(song));
		});
		window.list("songList").selectItem(0);
		window.button("addToPlayQueueButton").click();
		verify(playQueueController).onSongSelected(song);
	}

	@Test
	@GUITest
	public void testRemoveSelectedButtonEnabledWhenQueueSongSelected() {
		Genre rock = new Genre("Rock", "Rock music");
		Song song1 = new Song(1L, "Bohemian Rhapsody", "Queen", 354, rock);
		Song song2 = new Song(2L, "Stairway to Heaven", "Led Zeppelin", 482, rock);
		Map<Long, Song> queueMap = new LinkedHashMap<>();
		queueMap.put(1L, song1);
		queueMap.put(2L, song2);
		GuiActionRunner.execute(() -> {
			view.showQueue(queueMap);
		});
		window.list("playQueueList").selectItem(0);
		window.button("removeSelectedButton").requireEnabled();
	}

	@Test
	@GUITest
	public void testRemoveSelectedButtonDisabledWhenNoQueueSongSelected() {
		Genre rock = new Genre("Rock", "Rock music");
		Song song1 = new Song(1L, "Bohemian Rhapsody", "Queen", 354, rock);
		Map<Long, Song> queueMap = new LinkedHashMap<>();
		queueMap.put(1L, song1);
		GuiActionRunner.execute(() -> {
			view.showQueue(queueMap);
		});
		window.list("playQueueList").clearSelection();
		window.button("removeSelectedButton").requireDisabled();
	}

	@Test
	@GUITest
	public void testRemoveSelectedButtonCallsPlayQueueController() {
		Genre rock = new Genre("Rock", "Rock music");
		Song song1 = new Song(1L, "Bohemian Rhapsody", "Queen", 354, rock);
		Song song2 = new Song(2L, "Stairway to Heaven", "Led Zeppelin", 482, rock);
		Map<Long, Song> queueMap = new LinkedHashMap<>();
		queueMap.put(1L, song1);
		queueMap.put(2L, song2);
		GuiActionRunner.execute(() -> {
			view.showQueue(queueMap);
		});
		window.list("playQueueList").selectItem(0);
		window.button("removeSelectedButton").click();
		verify(playQueueController).onSongRemoved(1L);
	}

	@Test
	@GUITest
	public void testRemoveSelectedButtonCallsControllerWithCorrectQueueIdWhenDuplicateSongsPresent() {
		Genre rock = new Genre("Rock", "Rock music");
		Song duplicateSong = new Song(1L, "Bohemian Rhapsody", "Queen", 354, rock);
		Map<Long, Song> queueMap = new LinkedHashMap<>();
		queueMap.put(10L, duplicateSong);
		queueMap.put(20L, duplicateSong);
		GuiActionRunner.execute(() -> {
			view.showQueue(queueMap);
		});
		window.list("playQueueList").selectItem(1);
		window.button("removeSelectedButton").click();
		verify(playQueueController).onSongRemoved(20L);
	}

	@Test
	@GUITest
	public void testPlayNextButtonEnabledWhenQueueNotEmpty() {
		Genre rock = new Genre("Rock", "Rock music");
		Song song1 = new Song(1L, "Bohemian Rhapsody", "Queen", 354, rock);
		Song song2 = new Song(2L, "Stairway to Heaven", "Led Zeppelin", 482, rock);
		Map<Long, Song> queueMap = new LinkedHashMap<>();
		queueMap.put(1L, song1);
		queueMap.put(2L, song2);
		GuiActionRunner.execute(() -> {
			view.showQueue(queueMap);
		});
		window.button("playNextButton").requireEnabled();
	}

	@Test
	@GUITest
	public void testPlayNextButtonDisabledWhenQueueEmpty() {
		Map<Long, Song> emptyQueue = new LinkedHashMap<>();
		GuiActionRunner.execute(() -> {
			view.showQueue(emptyQueue);
		});
		window.button("playNextButton").requireDisabled();
	}

	@Test
	@GUITest
	public void testPlayNextButtonCallsPlayQueueController() {
		Genre rock = new Genre("Rock", "Rock music");
		Song song = new Song(1L, "Bohemian Rhapsody", "Queen", 354, rock);
		Map<Long, Song> queueMap = new LinkedHashMap<>();
		queueMap.put(1L, song);
		GuiActionRunner.execute(() -> {
			view.showQueue(queueMap);
		});
		window.button("playNextButton").click();
		verify(playQueueController).onPlayNext();
	}

	@Test
	@GUITest
	public void testCleanQueueButtonEnabledWhenQueueNotEmpty() {
		Genre rock = new Genre("Rock", "Rock music");
		Song song1 = new Song(1L, "Bohemian Rhapsody", "Queen", 354, rock);
		Song song2 = new Song(2L, "Stairway to Heaven", "Led Zeppelin", 482, rock);
		Map<Long, Song> queueMap = new LinkedHashMap<>();
		queueMap.put(1L, song1);
		queueMap.put(2L, song2);
		GuiActionRunner.execute(() -> {
			view.showQueue(queueMap);
		});
		window.button("cleanQueueButton").requireEnabled();
	}

	@Test
	@GUITest
	public void testCleanQueuetButtonEnabledWhenQueueEmpty() {
		Map<Long, Song> emptyQueue = new LinkedHashMap<>();
		GuiActionRunner.execute(() -> {
			view.showQueue(emptyQueue);
		});
		window.button("cleanQueueButton").requireDisabled();
	}

	@Test
	@GUITest
	public void testCleanQueueButtonCallsController() {
		Genre rock = new Genre("Rock", "Rock music");
		Song song = new Song(1L, "Bohemian Rhapsody", "Queen", 354, rock);
		Map<Long, Song> queueMap = new LinkedHashMap<>();
		queueMap.put(1L, song);
		GuiActionRunner.execute(() -> {
			view.showQueue(queueMap);
		});
		window.button("cleanQueueButton").click();
		verify(playQueueController).clearQueue();
	}

	@Test
	@GUITest
	public void testButtonsDisabledWhenPlayQueueBecomesEmpty() {
		Genre rock = new Genre("Rock", "Rock music");
		Song song = new Song(1L, "Bohemian Rhapsody", "Queen", 354, rock);
		Map<Long, Song> nonEmptyQueue = new LinkedHashMap<>();
		nonEmptyQueue.put(1L, song);
		GuiActionRunner.execute(() -> view.showQueue(nonEmptyQueue));
		window.button("playNextButton").requireEnabled();
		window.button("cleanQueueButton").requireEnabled();
		Map<Long, Song> emptyQueue = new LinkedHashMap<>();
		GuiActionRunner.execute(() -> view.showQueue(emptyQueue));
		window.button("playNextButton").requireDisabled();
		window.button("cleanQueueButton").requireDisabled();
	}
}