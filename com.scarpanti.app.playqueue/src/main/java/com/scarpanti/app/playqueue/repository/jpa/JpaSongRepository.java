package com.scarpanti.app.playqueue.repository.jpa;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.scarpanti.app.playqueue.entity.SongEntity;
import com.scarpanti.app.playqueue.mapper.SongMapper;
import com.scarpanti.app.playqueue.model.Genre;
import com.scarpanti.app.playqueue.model.Song;
import com.scarpanti.app.playqueue.repository.SongRepository;

public class JpaSongRepository implements SongRepository {

	private EntityManager entityManager;

	public JpaSongRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public List<Song> getSongsByGenre(Genre genre) {
		TypedQuery<SongEntity> query = entityManager.createQuery(
				"SELECT s FROM SongEntity s WHERE s.genre.name = :genreName ORDER BY s.title", SongEntity.class);

		query.setParameter("genreName", genre.getName());

		List<SongEntity> entities = query.getResultList();

		return entities.stream().map(SongMapper::entityToModel).collect(Collectors.toList());
	}
}