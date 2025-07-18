package com.scarpanti.app.playqueue.controller;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

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

public class PlayQueueControllerTest {

	private static final Genre ROCK = new Genre("Rock", "Rock music");
	private static final Song BOHEMIAN_RHAPSODY = new Song(1L, "Bohemian Rhapsody", "Queen", 354, ROCK);
	private static final Song STAIRWAY_TO_HEAVEN = new Song(2L, "Stairway To Heaven", "Led Zeppelin", 482, ROCK);

	private GenreRepository genreRepository;
	private SongRepository songRepository;
	private PlayQueueRepository playQueueRepository;
	private TransactionManager transactionManager;
	private PlayQueueView playQueueView;
	private PlayQueueController controller;

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

		controller = new PlayQueueController(transactionManager, playQueueView);
	}

	@Test
	public void testAddSongToPlayQueue() {
		when(playQueueRepository.getAllSongs()).thenReturn(Arrays.asList(BOHEMIAN_RHAPSODY));

		controller.onSongSelected(BOHEMIAN_RHAPSODY);

		InOrder inOrder = inOrder(transactionManager, playQueueRepository, playQueueView);
		inOrder.verify(transactionManager).doInTransaction(Mockito.<TransactionCode<?>>any());
		inOrder.verify(playQueueRepository).enqueue(BOHEMIAN_RHAPSODY);
		inOrder.verify(playQueueRepository).getAllSongs();
		inOrder.verify(playQueueView).showQueue(Arrays.asList(BOHEMIAN_RHAPSODY));
	}

	@Test
	public void testAddSongToPlayQueueWhenNotEmpty() {
		when(playQueueRepository.getAllSongs()).thenReturn(Arrays.asList(BOHEMIAN_RHAPSODY, STAIRWAY_TO_HEAVEN));

		controller.onSongSelected(STAIRWAY_TO_HEAVEN);

		InOrder inOrder = inOrder(transactionManager, playQueueRepository, playQueueView);
		inOrder.verify(transactionManager).doInTransaction(Mockito.<TransactionCode<?>>any());
		inOrder.verify(playQueueRepository).enqueue(STAIRWAY_TO_HEAVEN);
		inOrder.verify(playQueueRepository).getAllSongs();
		inOrder.verify(playQueueView).showQueue(Arrays.asList(BOHEMIAN_RHAPSODY, STAIRWAY_TO_HEAVEN));
	}

	@Test
	public void testRemoveSongFromPlayQueueWhenIsSelected() {
		when(playQueueRepository.getAllSongs()).thenReturn(Collections.emptyList());

		controller.onSongRemoved(BOHEMIAN_RHAPSODY);

		InOrder inOrder = inOrder(transactionManager, playQueueRepository, playQueueView);
		inOrder.verify(transactionManager).doInTransaction(Mockito.<TransactionCode<?>>any());
		inOrder.verify(playQueueRepository).remove(BOHEMIAN_RHAPSODY);
		inOrder.verify(playQueueRepository).getAllSongs();
		inOrder.verify(playQueueView).showQueue(Collections.emptyList());
	}

	@Test
	public void testRemoveSongFromPlayQueueWhenThereIsAtLeastOneSong() {
		when(playQueueRepository.getAllSongs()).thenReturn(Arrays.asList(STAIRWAY_TO_HEAVEN));

		controller.onSongRemoved(BOHEMIAN_RHAPSODY);

		InOrder inOrder = inOrder(transactionManager, playQueueRepository, playQueueView);
		inOrder.verify(transactionManager).doInTransaction(Mockito.<TransactionCode<?>>any());
		inOrder.verify(playQueueRepository).remove(BOHEMIAN_RHAPSODY);
		inOrder.verify(playQueueRepository).getAllSongs();
		inOrder.verify(playQueueView).showQueue(Arrays.asList(STAIRWAY_TO_HEAVEN));
	}

	@Test
	public void testPlayNextSong() {
		when(playQueueRepository.getAllSongs()).thenReturn(Arrays.asList(STAIRWAY_TO_HEAVEN));

		controller.onPlayNext();

		InOrder inOrder = inOrder(transactionManager, playQueueRepository, playQueueView);
		inOrder.verify(transactionManager).doInTransaction(Mockito.<TransactionCode<?>>any());
		inOrder.verify(playQueueRepository).dequeue();
		inOrder.verify(playQueueRepository).getAllSongs();
		inOrder.verify(playQueueView).showQueue(Arrays.asList(STAIRWAY_TO_HEAVEN));
	}

	@Test
	public void testPlayNextSongWhenOnlyOneSong() {
		when(playQueueRepository.getAllSongs()).thenReturn(Collections.emptyList());

		controller.onPlayNext();

		InOrder inOrder = inOrder(transactionManager, playQueueRepository, playQueueView);
		inOrder.verify(transactionManager).doInTransaction(Mockito.<TransactionCode<?>>any());
		inOrder.verify(playQueueRepository).dequeue();
		inOrder.verify(playQueueRepository).getAllSongs();
		inOrder.verify(playQueueView).showQueue(Collections.emptyList());
	}

	@Test
	public void testGetPlayQueueWithSongs() {
		when(playQueueRepository.getAllSongs()).thenReturn(Arrays.asList(BOHEMIAN_RHAPSODY, STAIRWAY_TO_HEAVEN));

		controller.getPlayQueue();

		InOrder inOrder = inOrder(transactionManager, playQueueRepository, playQueueView);
		inOrder.verify(transactionManager).doInTransaction(Mockito.<TransactionCode<?>>any());
		inOrder.verify(playQueueRepository).getAllSongs();
		inOrder.verify(playQueueView).showQueue(Arrays.asList(BOHEMIAN_RHAPSODY, STAIRWAY_TO_HEAVEN));
	}

	@Test
	public void testGetPlayQueueEmpty() {
		when(playQueueRepository.getAllSongs()).thenReturn(Collections.emptyList());

		controller.getPlayQueue();

		InOrder inOrder = inOrder(transactionManager, playQueueRepository, playQueueView);
		inOrder.verify(transactionManager).doInTransaction(Mockito.<TransactionCode<?>>any());
		inOrder.verify(playQueueRepository).getAllSongs();
		inOrder.verify(playQueueView).showQueue(Collections.emptyList());
	}
}