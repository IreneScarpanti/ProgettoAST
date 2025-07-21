package com.scarpanti.app.playqueue.model;

import java.util.Objects;

public class Song {
	private Long id;
	private String title;
	private String artist;
	private int duration;
	private Genre genre;

	public Song(Long id, String title, String artist, int duration, Genre genre) {
		super();
		this.id = id;
		this.title = title;
		this.artist = artist;
		this.duration = duration;
		this.genre = genre;
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getArtist() {
		return artist;
	}

	public int getDuration() {
		return duration;
	}

	public Genre getGenre() {
		return genre;
	}

	@Override
	public int hashCode() {
		return Objects.hash(artist, duration, genre, id, title);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Song other = (Song) obj;
		return Objects.equals(artist, other.artist) && duration == other.duration && Objects.equals(genre, other.genre)
				&& Objects.equals(id, other.id) && Objects.equals(title, other.title);
	}

	@Override
	public String toString() {
		return "Song [id=" + id + ", title=" + title + ", artist=" + artist + ", duration=" + duration + ", genre="
				+ genre + "]";
	}

}