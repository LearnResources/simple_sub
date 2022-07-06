package com.sub.example.sub.checker.model;

import com.google.gson.annotations.SerializedName;

public class Response{

	@SerializedName("script")
	private Script script;

	public Script getScript(){
		return script;
	}
}