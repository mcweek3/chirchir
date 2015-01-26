package com.story.tirrtirr;

import java.io.File;
import java.io.FilenameFilter;

public class Mp3Filter implements FilenameFilter {
	public boolean accept(File dir, String name) {
		return name.endsWith(".mp4") || name.endsWith(".m4a") || name.endsWith(".mp3") || name.endsWith(".mpeg");     
	}
}
