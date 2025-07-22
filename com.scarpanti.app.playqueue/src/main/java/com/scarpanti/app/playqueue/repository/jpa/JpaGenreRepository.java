package com.scarpanti.app.playqueue.repository.jpa;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.scarpanti.app.playqueue.entity.GenreEntity;
import com.scarpanti.app.playqueue.mapper.GenreMapper;
import com.scarpanti.app.playqueue.model.Genre;
import com.scarpanti.app.playqueue.repository.GenreRepository;

public class JpaGenreRepository implements GenreRepository {

	private EntityManager entityManager;

	public JpaGenreRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public List<Genre> getAllGenres() {
		TypedQuery<GenreEntity> query = entityManager.createQuery("SELECT g FROM GenreEntity g ORDER BY g.name",
				GenreEntity.class);

		List<GenreEntity> entities = query.getResultList();

		return entities.stream().map(GenreMapper::entityToModel).collect(Collectors.toList());
	}
}