package com.scarpanti.app.playqueue.model;

import java.util.List;
import java.util.Objects;

public class PlayQueue {
	private List<Song> songs;

	public PlayQueue(List<Song> songs) {
		super();
		this.songs = songs;
	}

	public List<Song> getSongs() {
		return songs;
	}

	@Override
	public int hashCode() {
		return Objects.hash(songs);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlayQueue other = (PlayQueue) obj;
		return Objects.equals(songs, other.songs);
	}

	@Override
	public String toString() {
		return "PlayQueue [songs=" + songs + "]";
	}

}