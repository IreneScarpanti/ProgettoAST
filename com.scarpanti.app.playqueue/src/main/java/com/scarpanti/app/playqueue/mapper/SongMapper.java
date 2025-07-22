package com.scarpanti.app.playqueue.mapper;

import com.scarpanti.app.playqueue.entity.GenreEntity;
import com.scarpanti.app.playqueue.entity.SongEntity;
import com.scarpanti.app.playqueue.model.Genre;
import com.scarpanti.app.playqueue.model.Song;

public class SongMapper {

	private SongMapper() {

	}

	public static Song entityToModel(SongEntity entity) {
		Genre genre = GenreMapper.entityToModel(entity.getGenre());
		return new Song(entity.getId(), entity.getTitle(), entity.getArtist(), entity.getDuration(), genre);
	}

	public static SongEntity modelToEntity(Song model, GenreEntity genreEntity) {
		return new SongEntity(model.getTitle(), model.getArtist(), model.getDuration(), genreEntity);
	}
}