package org.zhihanli.hw5;

import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.media.client.Audio;
import static org.zhihanli.hw5.Situation.*;

public class AudioControl {
	public static Audio createAudio(Situation situation) {

		Audio audio = Audio.createIfSupported();
		if (audio == null) {
			return null;
		}
		// audio.addSource("path/file.ogg", AudioElement.TYPE_OGG);
		// audio.addSource("path/file.mp3", AudioElement.TYPE_MP3);

		if (situation == EAT) {
			audio.addSource("zl_sounds/eat.wav", AudioElement.TYPE_WAV);
			audio.addSource("zl_sounds/eat.mp3", AudioElement.TYPE_MP3);

		}

		if (situation == END_OF_GAME) {
			audio.addSource("zl_sounds/gameend.mp3", AudioElement.TYPE_MP3);
			audio.addSource("zl_sounds/gameend.wav", AudioElement.TYPE_WAV);
		}

		if (situation == SELECT) {
			audio.addSource("zl_sounds/select.mp3", AudioElement.TYPE_MP3);
			audio.addSource("zl_sounds/select.wav", AudioElement.TYPE_WAV);
		}
		return audio;
	}
}
