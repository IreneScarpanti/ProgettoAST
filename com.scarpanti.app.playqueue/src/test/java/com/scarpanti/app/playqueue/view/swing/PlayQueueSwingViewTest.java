package com.scarpanti.app.playqueue.view.swing;

import static org.mockito.Mockito.verify;

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

}