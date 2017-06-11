package com.linkinpark213.phone.common;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by ooo on 2017/4/29 0029.
 */
public class Message implements Serializable {
    public final static int CALL_REQUEST = 0;
    public final static int ANSWER = 1;
    public final static int SPEAK = 2;
    public final static int HANG_OFF = 3;
    public final static int CALL_REFUSE = 4;
    public final static int CALL_CANCEL = 5;
    private int type;
    private String content;
    private byte[] audioByteArray;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getAudioByteArray() {
        return audioByteArray;
    }

    public void setAudioByteArray(byte[] audioByteArray) {
        this.audioByteArray = audioByteArray;
    }

    public Message(int type, String content) {
        this.type = type;
        this.content = content;
        this.audioByteArray = null;
    }

    public Message(String content, byte[] bytes) {
        this.type = SPEAK;
        this.content = content;
        this.audioByteArray = bytes;
    }
}
