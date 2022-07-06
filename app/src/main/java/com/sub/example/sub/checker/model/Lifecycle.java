package com.sub.example.sub.checker.model;

import com.google.gson.annotations.SerializedName;

public class Lifecycle{

	@SerializedName("app_open")
	private AppOpen appOpen;

	public AppOpen getAppOpen(){
		return appOpen;
	}
}