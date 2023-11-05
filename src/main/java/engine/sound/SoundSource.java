package engine.sound;

import org.joml.Vector3f;

import static org.lwjgl.openal.AL10.*;

public class SoundSource {

    private final int sourceID;
    private Vector3f position;
    private float gain;

    public SoundSource(boolean loop, boolean relative) {
        this(loop, relative, new Vector3f(), 1.0f);
    }

    public SoundSource(boolean loop, boolean relative, float x, float y, float z) {
        this(loop, relative, new Vector3f().set(x, y, z), 1.0f);
    }

    public SoundSource(boolean loop, boolean relative, float x, float y, float z, float gain) {
        this(loop, relative, new Vector3f().set(x, y, z), gain);
    }

    public SoundSource(boolean loop, boolean relative, Vector3f position) {
        this(loop, relative, position, 1.0f);
    }

    public SoundSource(boolean loop, boolean relative, Vector3f position, float gain) {
        sourceID = alGenSources();
        alSourcei(sourceID, AL_LOOPING, loop ? AL_TRUE : AL_FALSE);
        alSourcei(sourceID, AL_SOURCE_RELATIVE, relative ? AL_TRUE : AL_FALSE);
        alSourcef(sourceID, AL_GAIN, gain);
        this.position = position;
        this.gain = gain;
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

    public float getGain() { return gain; }
    public void setGain(float gain) { alSourcef(sourceID, AL_GAIN, gain); }

    public Vector3f getPosition() { return position; }
    public void setPosition(Vector3f position) {
        alSource3f(sourceID, AL_POSITION, position.x, position.y, position.z);
        this.position = position;
    }
    public void setPosition(float x, float y, float z) {
        alSource3f(sourceID, AL_POSITION, x, y, z);
        this.position.set(x, y, z);
    }

}
