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
@Table(name = "play_queue")
public class PlayQueueItemEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "queue_id")
	private Long queueId;

	@ManyToOne
	@JoinColumn(name = "song_id", nullable = false)
	private SongEntity song;

	public PlayQueueItemEntity() {
	}

	public PlayQueueItemEntity(SongEntity song) {
		this.song = song;
	}

	public Long getQueueId() {
		return queueId;
	}

	public SongEntity getSong() {
		return song;
	}

	@Override
	public int hashCode() {
		return Objects.hash(queueId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlayQueueItemEntity other = (PlayQueueItemEntity) obj;
		return Objects.equals(queueId, other.queueId);
	}

	@Override
	public String toString() {
		return "PlayQueueItemEntity [queueId=" + queueId + ", song=" + song + "]";
	}
}