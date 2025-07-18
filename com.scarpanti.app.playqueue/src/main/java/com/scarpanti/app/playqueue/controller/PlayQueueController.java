package com.scarpanti.app.playqueue.controller;

import java.util.List;

import com.scarpanti.app.playqueue.model.Song;
import com.scarpanti.app.playqueue.transaction.TransactionManager;
import com.scarpanti.app.playqueue.view.PlayQueueView;

public class PlayQueueController {
	private final TransactionManager transactionManager;
	private final PlayQueueView playQueueView;

	public PlayQueueController(TransactionManager transactionManager, PlayQueueView playQueueView) {
		super();
		this.transactionManager = transactionManager;
		this.playQueueView = playQueueView;
	}

	public void onSongSelected(Song song) {
		List<Song> songs = transactionManager.doInTransaction((genreRepo, songRepo, playQueueRepo) -> {
			playQueueRepo.enqueue(song);
			return playQueueRepo.getAllSongs();
		});

		playQueueView.showQueue(songs);
	}

	public void onSongRemoved(Song song) {
		List<Song> songs = transactionManager.doInTransaction((genreRepo, songRepo, playQueueRepo) -> {
			playQueueRepo.remove(song);
			return playQueueRepo.getAllSongs();
		});

		playQueueView.showQueue(songs);
	}

	public void onPlayNext() {
		List<Song> songs = transactionManager.doInTransaction((genreRepo, songRepo, playQueueRepo) -> {
			playQueueRepo.dequeue();
			return playQueueRepo.getAllSongs();
		});

		playQueueView.showQueue(songs);
	}

	public void getPlayQueue() {
		List<Song> songs = transactionManager
				.doInTransaction((genreRepo, songRepo, playQueueRepo) -> playQueueRepo.getAllSongs());

		playQueueView.showQueue(songs);
	}
}
