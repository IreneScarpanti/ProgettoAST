package com.scarpanti.app.playqueue.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.scarpanti.app.playqueue.entity.GenreEntity;
import com.scarpanti.app.playqueue.model.Genre;

public class GenreMapperTest {

	@Test
	public void testEntityToModel() {
		GenreEntity entity = new GenreEntity("Rock", "Rock music");

		Genre model = GenreMapper.entityToModel(entity);

		assertThat(model.getName()).isEqualTo("Rock");
		assertThat(model.getDescription()).isEqualTo("Rock music");
	}

	@Test
	public void testModelToEntity() {
		Genre model = new Genre("Jazz", "Jazz music");

		GenreEntity entity = GenreMapper.modelToEntity(model);

		assertThat(entity.getName()).isEqualTo("Jazz");
		assertThat(entity.getDescription()).isEqualTo("Jazz music");
		assertThat(entity.getId()).isNull(); 
	}
}