package com.scarpanti.app.playqueue.view;

import java.util.List;

import com.scarpanti.app.playqueue.model.Genre;
import com.scarpanti.app.playqueue.model.Song;

public interface PlayQueueView {

	void showGenres(List<Genre> genres);

	void showSongs(List<Song> rockSongs);

}
