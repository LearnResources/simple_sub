package com.sub.example.sub.checker.model;

import com.google.gson.annotations.SerializedName;

public class AppOpen{

	@SerializedName("number")
	private int number;

	@SerializedName("name")
	private String name;

	public int getNumber(){
		return number;
	}

	public String getName(){
		return name;
	}
}