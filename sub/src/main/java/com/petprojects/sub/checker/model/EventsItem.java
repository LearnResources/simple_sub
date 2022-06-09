package com.petprojects.sub.checker.model;

import com.google.gson.annotations.SerializedName;

public class EventsItem{

	@SerializedName("name")
	private String name;

	public String getName(){
		return name;
	}
}