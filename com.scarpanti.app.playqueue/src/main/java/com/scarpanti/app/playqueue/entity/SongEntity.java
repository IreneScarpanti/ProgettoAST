package com.scarpanti.app.playqueue.entity;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "songs")
public class SongEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "artist", nullable = false)
	private String artist;

	@Column(name = "duration", nullable = false)
	private int duration;

	@ManyToOne
	@JoinColumn(name = "genre_id", nullable = false)
	private GenreEntity genre;

	public SongEntity() {
	}

	public SongEntity(String title, String artist, int duration, GenreEntity genre) {
		this.title = title;
		this.artist = artist;
		this.duration = duration;
		this.genre = genre;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public GenreEntity getGenre() {
		return genre;
	}

	public void setGenre(GenreEntity genre) {
		this.genre = genre;
	}

	@Override
	public int hashCode() {
		return Objects.hash(artist, duration, genre, title);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SongEntity other = (SongEntity) obj;
		return duration == other.duration && Objects.equals(artist, other.artist) && Objects.equals(genre, other.genre)
				&& Objects.equals(title, other.title);
	}

	@Override
	public String toString() {
		return "SongEntity [id=" + id + ", title=" + title + ", artist=" + artist + ", duration=" + duration
				+ ", genre=" + genre + "]";
	}
}