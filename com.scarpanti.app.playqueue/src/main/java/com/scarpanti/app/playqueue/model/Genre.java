package com.scarpanti.app.playqueue.model;

import java.util.Objects;

public class Genre {
	private String name;
	private String description;

	public Genre(String name, String description) {
		super();
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public int hashCode() {
		return Objects.hash(description, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Genre other = (Genre) obj;
		return Objects.equals(description, other.description) && Objects.equals(name, other.name);
	}

	@Override
	public String toString() {
		return "Genre [name=" + name + ", description=" + description + "]";
	}

}