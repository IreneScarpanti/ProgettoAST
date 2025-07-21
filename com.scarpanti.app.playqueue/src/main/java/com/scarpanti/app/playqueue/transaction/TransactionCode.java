package com.scarpanti.app.playqueue.transaction;

import com.scarpanti.app.playqueue.repository.GenreRepository;
import com.scarpanti.app.playqueue.repository.PlayQueueRepository;
import com.scarpanti.app.playqueue.repository.SongRepository;

public interface TransactionCode<T> {
	T execute(GenreRepository genreRepository, SongRepository songRepository, PlayQueueRepository playQueueRepository);
}
