package engine.sound;

import org.joml.Vector3f;

import static org.lwjgl.openal.AL10.*;

public class SoundSource {

    private final int sourceID;
    private Vector3f position;

    public SoundSource(boolean loop, boolean relative) {
        this(loop, relative, new Vector3f());
    }

    public SoundSource(boolean loop, boolean relative, float x, float y, float z) {
        this(loop, relative, new Vector3f().set(x, y, z));
    }

    public SoundSource(boolean loop, boolean relative, Vector3f position) {
        sourceID = alGenSources();
        alSourcei(sourceID, AL_LOOPING, loop ? AL_TRUE : AL_FALSE);
        alSourcei(sourceID, AL_SOURCE_RELATIVE, relative ? AL_TRUE : AL_FALSE);
        this.position = position;
    }

    public void stop() { alSourceStop(sourceID); }

    public void cleanup() {
        stop();
        alDeleteSources(sourceID);
    }

    public boolean isPlaying() { return alGetSourcei(sourceID, AL_SOURCE_STATE) == AL_PLAYING; }

    public void pause() { alSourcePause(sourceID); }
    public void play() { alSourcePlay(sourceID); }

    public void setBuffer(int bufferID) {
        stop();
        alSourcei(sourceID, AL_BUFFER, bufferID);
    }

    public void setGain(float gain) { alSourcef(sourceID, AL_GAIN, gain); }

    public void setPosition(Vector3f position) {
        alSource3f(sourceID, AL_POSITION, position.x, position.y, position.z);
        this.position = position;
    }

    public void setPosition(float x, float y, float z) {
        alSource3f(sourceID, AL_POSITION, x, y, z);
        this.position.set(x, y, z);
    }

}
