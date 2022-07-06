package com.sub.example.sub.checker.model;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class Script{

	@SerializedName("lifecycle")
	private Lifecycle lifecycle;

	@SerializedName("message")
	private List<MessageItem> message;

	@SerializedName("events")
	private List<EventsItem> events;

	public Lifecycle getLifecycle(){
		return lifecycle;
	}

	public List<MessageItem> getMessage(){
		return message;
	}

	public List<EventsItem> getEvents(){
		return events;
	}
}