package com.scarpanti.app.playqueue.controller;

import java.util.List;

import com.scarpanti.app.playqueue.model.Genre;
import com.scarpanti.app.playqueue.model.Song;
import com.scarpanti.app.playqueue.transaction.TransactionManager;
import com.scarpanti.app.playqueue.view.PlayQueueView;

public class SongController {

	private TransactionManager transactionManager;
	private PlayQueueView playQueueView;

	public SongController(TransactionManager transactionManager, PlayQueueView playQueueView) {
		this.transactionManager = transactionManager;
		this.playQueueView = playQueueView;
	}

	public void onGenreSelected(Genre genre) {
		List<Song> songs = transactionManager
				.doInTransaction((genreRepo, songRepo, playQueueRepo) -> songRepo.getSongsByGenre(genre));

		playQueueView.showSongs(songs);
	}
}