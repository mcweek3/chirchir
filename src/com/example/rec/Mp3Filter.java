package com.example.rec;

import java.io.File;
import java.io.FilenameFilter;

public class Mp3Filter implements FilenameFilter {     
	public boolean accept(File dir, String name) {         
		return (name.endsWith(".mp4"));     
		}}
