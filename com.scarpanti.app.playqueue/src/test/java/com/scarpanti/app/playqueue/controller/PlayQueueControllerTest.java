package com.scarpanti.app.playqueue.controller;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

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
	public void testOnSongSelectedShouldEnqueueSongAndShowQueue() {
		Map<Long, Song> songsInQueue = Map.of(1L, BOHEMIAN_RHAPSODY);
		when(playQueueRepository.getAllSongs()).thenReturn(songsInQueue);

		controller.onSongSelected(BOHEMIAN_RHAPSODY);

		InOrder inOrder = inOrder(playQueueRepository, playQueueView);
		inOrder.verify(playQueueRepository).enqueue(BOHEMIAN_RHAPSODY);
		inOrder.verify(playQueueRepository).getAllSongs();
		inOrder.verify(playQueueView).showQueue(songsInQueue);
	}

	@Test
	public void testOnPlayNext() {
		Map<Long, Song> songsAfterDequeue = Map.of(1L, STAIRWAY_TO_HEAVEN);
		when(playQueueRepository.getAllSongs()).thenReturn(songsAfterDequeue);

		controller.onPlayNext();

		InOrder inOrder = inOrder(transactionManager, playQueueRepository, playQueueView);
		inOrder.verify(transactionManager).doInTransaction(Mockito.<TransactionCode<?>>any());
		inOrder.verify(playQueueRepository).dequeue();
		inOrder.verify(playQueueRepository).getAllSongs();
		inOrder.verify(playQueueView).showQueue(songsAfterDequeue);
	}

	@Test
	public void testOnSongRemoved() {
		Map<Long, Song> songsAfterRemoval = Map.of(1L, STAIRWAY_TO_HEAVEN);
		when(playQueueRepository.getAllSongs()).thenReturn(songsAfterRemoval);

		controller.onSongRemoved(2L);

		InOrder inOrder = inOrder(transactionManager, playQueueRepository, playQueueView);
		inOrder.verify(transactionManager).doInTransaction(Mockito.<TransactionCode<?>>any());
		inOrder.verify(playQueueRepository).remove(2L);
		inOrder.verify(playQueueRepository).getAllSongs();
		inOrder.verify(playQueueView).showQueue(songsAfterRemoval);
	}

	@Test
	public void testGetPlayQueue() {
		Map<Long, Song> currentQueue = Map.of(1L, BOHEMIAN_RHAPSODY, 2L, STAIRWAY_TO_HEAVEN);
		when(playQueueRepository.getAllSongs()).thenReturn(currentQueue);

		controller.getPlayQueue();

		InOrder inOrder = inOrder(transactionManager, playQueueRepository, playQueueView);
		inOrder.verify(transactionManager).doInTransaction(Mockito.<TransactionCode<?>>any());
		inOrder.verify(playQueueRepository).getAllSongs();
		inOrder.verify(playQueueView).showQueue(currentQueue);
	}

}