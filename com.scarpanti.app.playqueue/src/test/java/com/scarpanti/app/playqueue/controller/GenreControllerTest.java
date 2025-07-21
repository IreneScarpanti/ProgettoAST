package com.scarpanti.app.playqueue.controller;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.scarpanti.app.playqueue.model.Genre;
import com.scarpanti.app.playqueue.repository.GenreRepository;
import com.scarpanti.app.playqueue.repository.PlayQueueRepository;
import com.scarpanti.app.playqueue.repository.SongRepository;
import com.scarpanti.app.playqueue.transaction.TransactionCode;
import com.scarpanti.app.playqueue.transaction.TransactionManager;
import com.scarpanti.app.playqueue.view.PlayQueueView;

public class GenreControllerTest {

	private static final Genre ROCK = new Genre("Rock", "Rock music");
	private static final Genre JAZZ = new Genre("Jazz", "Jazz music");

	private GenreRepository genreRepository;
	private SongRepository songRepository;
	private PlayQueueRepository playQueueRepository;
	private TransactionManager transactionManager;
	private PlayQueueView playQueueView;
	private GenreController controller;

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

		controller = new GenreController(transactionManager, playQueueView);
	}

	@Test
	public void testLoadGenres() {
		List<Genre> genres = Arrays.asList(ROCK, JAZZ);
		when(genreRepository.getAllGenres()).thenReturn(genres);

		controller.loadGenres();

		InOrder inOrder = inOrder(transactionManager, genreRepository, playQueueView);
		inOrder.verify(transactionManager).doInTransaction(Mockito.<TransactionCode<?>>any());
		inOrder.verify(genreRepository).getAllGenres();
		inOrder.verify(playQueueView).showGenres(genres);
	}

	@Test
	public void testLoadGenresWhenEmpty() {
		List<Genre> emptyGenres = Arrays.asList();
		when(genreRepository.getAllGenres()).thenReturn(emptyGenres);

		controller.loadGenres();

		InOrder inOrder = inOrder(transactionManager, genreRepository, playQueueView);
		inOrder.verify(transactionManager).doInTransaction(Mockito.<TransactionCode<?>>any());
		inOrder.verify(genreRepository).getAllGenres();
		inOrder.verify(playQueueView).showGenres(emptyGenres);
	}

}
