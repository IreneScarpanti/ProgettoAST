package com.scarpanti.app.playqueue.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.scarpanti.app.playqueue.entity.GenreEntity;
import com.scarpanti.app.playqueue.entity.SongEntity;
import com.scarpanti.app.playqueue.model.Genre;
import com.scarpanti.app.playqueue.model.Song;

public class SongMapperTest {

	@Test
	public void testEntityToModel() {
		GenreEntity genreEntity = new GenreEntity("Rock", "Rock music");
		SongEntity songEntity = new SongEntity("Bohemian Rhapsody", "Queen", 354, genreEntity);

		Song model = SongMapper.entityToModel(songEntity);

		assertThat(model.getId()).isNull();
		assertThat(model.getTitle()).isEqualTo("Bohemian Rhapsody");
		assertThat(model.getArtist()).isEqualTo("Queen");
		assertThat(model.getDuration()).isEqualTo(354);

		assertThat(model.getGenre().getName()).isEqualTo("Rock");
		assertThat(model.getGenre().getDescription()).isEqualTo("Rock music");
	}

	@Test
	public void testModelToEntity() {
		Genre genreModel = new Genre("Jazz", "Jazz music");
		Song songModel = new Song(1L, "Take Five", "Dave Brubeck", 324, genreModel);
		GenreEntity genreEntity = new GenreEntity("Jazz", "Jazz music");

		SongEntity entity = SongMapper.modelToEntity(songModel, genreEntity);

		assertThat(entity.getId()).isNull(); 
		assertThat(entity.getTitle()).isEqualTo("Take Five");
		assertThat(entity.getArtist()).isEqualTo("Dave Brubeck");
		assertThat(entity.getDuration()).isEqualTo(324);
		assertThat(entity.getGenre()).isEqualTo(genreEntity);
	}
}