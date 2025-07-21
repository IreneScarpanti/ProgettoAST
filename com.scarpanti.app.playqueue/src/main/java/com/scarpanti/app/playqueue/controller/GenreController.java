package com.scarpanti.app.playqueue.controller;

import java.util.List;

import com.scarpanti.app.playqueue.model.Genre;
import com.scarpanti.app.playqueue.transaction.TransactionManager;
import com.scarpanti.app.playqueue.view.PlayQueueView;

public class GenreController {
	private TransactionManager transactionManager;
	private PlayQueueView playQueueView;

	public GenreController(TransactionManager transactionManager, PlayQueueView playQueueView) {
		super();
		this.transactionManager = transactionManager;
		this.playQueueView = playQueueView;
	}

	public void loadGenres() {
		List<Genre> genres = transactionManager
				.doInTransaction((genreRepo, songRepo, playQueueRepo) -> genreRepo.getAllGenres());

		playQueueView.showGenres(genres);
	}
}
