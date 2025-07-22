package com.scarpanti.app.playqueue.repository;

import java.util.Map;

import com.scarpanti.app.playqueue.model.Song;

public interface PlayQueueRepository {

	Map<Long, Song> getAllSongs();

	void enqueue(Song song);

	void dequeue();

	void remove(Long queueId);

}
