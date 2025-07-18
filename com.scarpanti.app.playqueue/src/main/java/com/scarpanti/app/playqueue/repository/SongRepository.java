package com.scarpanti.app.playqueue.repository;

import java.util.List;

import com.scarpanti.app.playqueue.model.Genre;
import com.scarpanti.app.playqueue.model.Song;

public interface SongRepository {

	List<Song> getSongsByGenre(Genre rock);

}
