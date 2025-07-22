package com.scarpanti.app.playqueue.repository;

import java.util.List;

import com.scarpanti.app.playqueue.model.Song;

public interface PlayQueueRepository {

	List<Song> getAllSongs();

	void enqueue(Song song);

	void dequeue();

}
