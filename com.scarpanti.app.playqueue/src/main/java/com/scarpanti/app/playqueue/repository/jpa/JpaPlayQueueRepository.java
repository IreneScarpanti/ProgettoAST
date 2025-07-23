package com.scarpanti.app.playqueue.repository.jpa;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.scarpanti.app.playqueue.entity.PlayQueueItemEntity;
import com.scarpanti.app.playqueue.mapper.SongMapper;
import com.scarpanti.app.playqueue.model.Song;
import com.scarpanti.app.playqueue.repository.PlayQueueRepository;

public class JpaPlayQueueRepository implements PlayQueueRepository {

	private EntityManager entityManager;

	public JpaPlayQueueRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public Map<Long, Song> getAllSongs() {
		TypedQuery<PlayQueueItemEntity> query = entityManager
				.createQuery("SELECT pq FROM PlayQueueItemEntity pq ORDER BY pq.queueId", PlayQueueItemEntity.class);

		List<PlayQueueItemEntity> entities = query.getResultList();

		Map<Long, Song> result = new LinkedHashMap<>();
		for (PlayQueueItemEntity entity : entities) {
			Song song = SongMapper.entityToModel(entity.getSong());
			result.put(entity.getQueueId(), song);
		}
		return result;
	}

	@Override
	public void enqueue(Song song) {

	}

	@Override
	public void dequeue() {

	}

	@Override
	public void remove(Long queueId) {

	}

	@Override
	public void clear() {

	}

}
