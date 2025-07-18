package com.scarpanti.app.playqueue.controller;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.scarpanti.app.playqueue.model.Genre;
import com.scarpanti.app.playqueue.model.Song;
import com.scarpanti.app.playqueue.repository.GenreRepository;
import com.scarpanti.app.playqueue.repository.PlayQueueRepository;
import com.scarpanti.app.playqueue.repository.SongRepository;
import com.scarpanti.app.playqueue.transaction.TransactionCode;
import com.scarpanti.app.playqueue.transaction.TransactionManager;
import com.scarpanti.app.playqueue.view.PlayQueueView;

public class SongControllerTest {

	private static final Genre ROCK = new Genre("Rock", "Rock music");
	private static final Genre JAZZ = new Genre("Jazz", "Jazz music");
	private static final Song BOHEMIAN_RHAPSODY = new Song(1L, "Bohemian Rhapsody", "Queen", 354, ROCK);
	private static final Song STAIRWAY_TO_HEAVEN = new Song(2L, "Stairway To Heaven", "Led Zeppelin", 482, ROCK);

	private GenreRepository genreRepository;
	private SongRepository songRepository;
	private PlayQueueRepository playQueueRepository;
	private TransactionManager transactionManager;
	private PlayQueueView playQueueView;
	private SongController controller;

	@Before
	public void setUp() {
		genreRepository = mock(GenreRepository.class);
		songRepository = mock(SongRepository.class);
		playQueueRepository = mock(PlayQueueRepository.class);
		transactionManager = mock(TransactionManager.class);
		playQueueView = mock(PlayQueueView.class);

		when(transactionManager.doInTransaction(Mockito.<TransactionCode<?>>any())).thenAnswer(invocation -> {
			TransactionCode<?> code = invocation.getArgument(0);
			return code.execute(genreRepository, songRepository, playQueueRepository);
		});

		controller = new SongController(transactionManager, playQueueView);
	}

	@Test
	public void testLoadSongsByGenre() {
		List<Song> rockSongs = Arrays.asList(BOHEMIAN_RHAPSODY, STAIRWAY_TO_HEAVEN);
		when(songRepository.getSongsByGenre(ROCK)).thenReturn(rockSongs);

		controller.onGenreSelected(ROCK);


		InOrder inOrder = inOrder(transactionManager, songRepository, playQueueView);
		inOrder.verify(transactionManager).doInTransaction(Mockito.<TransactionCode<?>>any());
		inOrder.verify(songRepository).getSongsByGenre(ROCK);
		inOrder.verify(playQueueView).showSongs(rockSongs);
	}

	@Test
	public void testLoadSongsByGenreWhenEmpty() {
		List<Song> emptySongs = Collections.emptyList();
		when(songRepository.getSongsByGenre(JAZZ)).thenReturn(emptySongs);

		controller.onGenreSelected(JAZZ);

		InOrder inOrder = inOrder(transactionManager, songRepository, playQueueView);
		inOrder.verify(transactionManager).doInTransaction(Mockito.<TransactionCode<?>>any());
		inOrder.verify(songRepository).getSongsByGenre(JAZZ);
		inOrder.verify(playQueueView).showSongs(emptySongs);
	}

}
