package com.library;

import org.vosk.Model;
import org.vosk.Recognizer;
import javax.sound.sampled.*;
import java.io.IOException;

public class VoiceRecognizer {
    // private static final String MODEL_PATH = "vosk-model-small-en-us"; // Update
    // with your model path
    private static final String MODEL_PATH = "D:\\jupiterProjectsAleena\\libraryappproject\\smart-library\\src\\main\\resources\\vosk-model-small-en-us-0.15";

    public static String recognize() throws IOException, LineUnavailableException {
        try (Model model = new Model(MODEL_PATH)) {
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            byte[] buffer = new byte[4096];
            int bytesRead = microphone.read(buffer, 0, buffer.length);
            microphone.close();

            try (Recognizer recognizer = new Recognizer(model, 16000)) {
                recognizer.acceptWaveForm(buffer, bytesRead);
                String result = recognizer.getFinalResult();
                // Parse JSON result (Vosk returns JSON)
                if (result.contains("\"text\"")) {
                    return result.split("\"text\"\\s*:\\s*\"")[1].split("\"")[0];
                }
                return null;
            }
        }
    }
}