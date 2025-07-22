package com.scarpanti.app.playqueue.mapper;

import com.scarpanti.app.playqueue.entity.GenreEntity;
import com.scarpanti.app.playqueue.model.Genre;

public class GenreMapper {

	private GenreMapper() {

	}

	public static Genre entityToModel(GenreEntity entity) {
		return new Genre(entity.getName(), entity.getDescription());
	}

	public static GenreEntity modelToEntity(Genre model) {
		return new GenreEntity(model.getName(), model.getDescription());
	}
}