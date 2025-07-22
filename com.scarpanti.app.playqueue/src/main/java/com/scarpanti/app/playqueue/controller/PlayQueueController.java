package com.scarpanti.app.playqueue.controller;

import java.util.Map;

import com.scarpanti.app.playqueue.model.Song;
import com.scarpanti.app.playqueue.transaction.TransactionManager;
import com.scarpanti.app.playqueue.view.PlayQueueView;

public class PlayQueueController {
	private final TransactionManager transactionManager;
	private final PlayQueueView playQueueView;

	public PlayQueueController(TransactionManager transactionManager, PlayQueueView playQueueView) {
		this.transactionManager = transactionManager;
		this.playQueueView = playQueueView;
	}

	public void onSongSelected(Song song) {
		Map<Long, Song> songs = transactionManager
				.doInTransaction((genreRepo, songRepo, playQueueRepo) -> {
			playQueueRepo.enqueue(song);
			return playQueueRepo.getAllSongs();
		});

		playQueueView.showQueue(songs);
	}

	public void onPlayNext() {
		Map<Long, Song> songs = transactionManager
				.doInTransaction((genreRepo, songRepo, playQueueRepo) -> {
			playQueueRepo.dequeue();
			return playQueueRepo.getAllSongs();
		});

		playQueueView.showQueue(songs);
	}

	public void onSongRemoved(Long queueId) {
		Map<Long, Song> songs = transactionManager
				.doInTransaction((genreRepo, songRepo, playQueueRepo) -> {
			playQueueRepo.remove(queueId);
			return playQueueRepo.getAllSongs();
		});

		playQueueView.showQueue(songs);
	}

	public void getPlayQueue() {
		Map<Long, Song> songs = transactionManager
				.doInTransaction((genreRepo, songRepo, playQueueRepo) -> playQueueRepo.getAllSongs());

		playQueueView.showQueue(songs);
	}
}