package engine.sound;

import org.joml.*;
import org.lwjgl.openal.*;

import engine.scene.view.Camera;

import java.nio.*;
import java.util.*;

import static org.lwjgl.openal.AL10.AL_DISTANCE_MODEL;
import static org.lwjgl.openal.AL10.alDistanceModel;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class SoundManager {

    private final List<SoundBuffer> soundBuffers;
    private final Map<String, SoundSource> soundSources;

    private long context;
    private long device;

    private SoundListener listener;

    private int attenuationModel;

    public SoundManager() {
        this(AL11.AL_EXPONENT_DISTANCE);
    }

    public SoundManager(int attenuationModel) {
        soundBuffers = new ArrayList<>();
        soundSources = new HashMap<>();

        device = alcOpenDevice((ByteBuffer) null);
        if (device == NULL) throw new IllegalStateException("Failed to open the default OpenAL device.");

        ALCCapabilities deviceCapabilities = ALC.createCapabilities(device);
        context = alcCreateContext(device, (IntBuffer) null);
        if (context == NULL) throw new IllegalStateException("Failed to create OpenAL context.");

        alcMakeContextCurrent(context);
        AL.createCapabilities(deviceCapabilities);

        setAttenuationModel(attenuationModel);
    }

    public void cleanup() {
        soundSources.values().forEach(SoundSource::cleanup);
        soundSources.clear();
        soundBuffers.forEach(SoundBuffer::cleanup);
        soundBuffers.clear();
        if (context != NULL) alcDestroyContext(context);
        if (device != NULL) alcCloseDevice(device);
    }

    public void addSoundBuffer(SoundBuffer soundBuffer) { this.soundBuffers.add(soundBuffer); }
    public void addSoundSource(String name, SoundSource soundSource) { this.soundSources.put(name, soundSource); }

    public void removeSoundSource(String name) { this.soundSources.remove(name); }

    public void playSoundSource(String name) {
        SoundSource soundSource = this.soundSources.get(name);
        if (soundSource != null && !soundSource.isPlaying()) soundSource.play();
    }


    public SoundListener getListener() { return listener; }
    public SoundSource getSoundSource(String name) { return this.soundSources.get(name); }
    public int getAttenuationModel() { return attenuationModel; }

    public void setListener(SoundListener listener) { this.listener = listener; }
    public void setAttenuationModel(int model) {
        this.attenuationModel = model;
        alDistanceModel(this.attenuationModel);
    }

    public void updateListenerPosition(Camera camera) {
        Matrix4f viewMatrix = camera.getViewMatrix();

        listener.setPosition(camera.getPosition());

        Vector3f forward = new Vector3f();
        viewMatrix.positiveZ(forward).negate();

        Vector3f up = new Vector3f();
        viewMatrix.positiveY(up);

        listener.setOrientation(forward, up);
    }

}
