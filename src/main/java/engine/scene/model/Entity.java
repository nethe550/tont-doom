package engine.scene.model;

import engine.graph.model.Model;
import engine.scene.Scene;
import engine.sound.SoundBuffer;
import engine.sound.SoundSource;
import org.joml.*;

import java.util.List;

public class Entity {

    protected final String id;
    protected final String modelID;
    protected Matrix4f modelMatrix;
    protected Vector3f position;
    protected Quaternionf rotation;
    protected float scale;
    protected AnimationData animationData;
    protected Sound sound;

    public Entity(String id, String modelID) {
        this.id = id;
        this.modelID = modelID;
        modelMatrix = new Matrix4f();
        position = new Vector3f();
        rotation = new Quaternionf();
        scale = 1f;
    }

    public void update() {
        if (sound != null) sound.getSource().setPosition(position);
    }

    public String getID() { return id; }
    public String getModelID() { return modelID; }
    public Matrix4f getModelMatrix() { return modelMatrix; }
    public Vector3f getPosition() { return position; }
    public Quaternionf getRotation() { return rotation; }
    public float getScale() { return scale; }
    public AnimationData getAnimationData() { return animationData; }
    public Sound getSound() { return sound; }

    public final void setPosition(Vector3f position) {
        this.setPosition(position.x, position.y, position.z);
    }
    public final void setPosition(Vector3f position, boolean updateMatrix) {
        this.setPosition(position.x, position.y, position.z, updateMatrix);
    }
    public final void setPosition(float x, float y, float z) {
        position.x = x;
        position.y = y;
        position.z = z;
        updateModelMatrix();
    }
    public final void setPosition(float x, float y, float z, boolean updateMatrix) {
        position.x = x;
        position.y = y;
        position.z = z;
        if (updateMatrix) updateModelMatrix();
    }

    public final void setRotation(Vector3f axis, float angle) {
        this.setRotation(axis.x, axis.y, axis.z, angle);
    }
    public final void setRotation(Vector3f axis, float angle, boolean updateMatrix) {
        this.setRotation(axis.x, axis.y, axis.z, angle, updateMatrix);
    }
    public void setRotation(float x, float y, float z, float angle) {
        rotation.fromAxisAngleRad(x, y, z, angle);
        updateModelMatrix();
    }
    public void setRotation(float x, float y, float z, float angle, boolean updateMatrix) {
        rotation.fromAxisAngleRad(x, y, z, angle);
        if (updateMatrix) updateModelMatrix();
    }

    public void setScale(float scale) {
        this.scale = scale;
        updateModelMatrix();
    }
    public void setScale(float scale, boolean updateMatrix) {
        this.scale = scale;
        if (updateMatrix) updateModelMatrix();
    }

    public void setAnimationData(AnimationData animationData) { this.animationData = animationData; }
    public void setAnimation(Scene scene, int animationIndex) throws RuntimeException {
        Model model = scene.getModel(modelID);
        Model.Animation animation = model.getAnimations().get(animationIndex);
        if (animation == null) throw new RuntimeException("Failed to find animation at index \"" + animationIndex + "\".\n");
        this.setAnimationData(new AnimationData(animation));
    }

    public void setSound(Sound sound) { this.sound = sound; }


    public void updateModelMatrix() {
        modelMatrix.translationRotateScale(position, rotation, scale);
    }

    public static class Sound {
        private String id;
        private SoundSource source;
        private List<SoundBuffer> buffers;
        private int activeBuffer;

        public Sound(String id, SoundSource source, List<SoundBuffer> buffers) {
            this(id, source, buffers, 0);
        }

        public Sound(String id, SoundSource source, List<SoundBuffer> buffers, int activeBuffer) {
            this.id = id;
            this.source = source;
            if (buffers.size() < 1) throw new RuntimeException("No sound buffers provided.");
            this.buffers = buffers;
            if (activeBuffer == -1 && buffers.size() > 0) this.activeBuffer = 0;
            else if (activeBuffer < 0 || activeBuffer >= this.buffers.size()) throw new IndexOutOfBoundsException("Invalid index for activeBuffer (" + activeBuffer + ")\nMust be in range [0, " + (this.buffers.size() - 1) + "]");
            else this.activeBuffer = activeBuffer;
            updateSource();
        }

        public String getID() { return id; }
        public SoundSource getSource() { return source; }
        public List<SoundBuffer> getBuffers() { return buffers; }
        public SoundBuffer getBuffer(int index) { return buffers.get(index); }
        public SoundBuffer getActiveBuffer() { return buffers.get(activeBuffer); }
        public float getGain() { return source.getGain(); }

        public void addBuffer(SoundBuffer buffer) { buffers.add(buffer); }
        public boolean removeBuffer(SoundBuffer buffer) { return buffers.remove(buffer); }
        public SoundBuffer removeBuffer(int index) {
            if (index >= 0 && index < buffers.size()) return buffers.get(index);
            else throw new IndexOutOfBoundsException("Failed to find sound buffer at index " + index + ".");
        }
        public void setActiveBuffer(int index) {
            this.activeBuffer = index;
            updateSource();
        }
        public void setGain(float gain) { source.setGain(gain); }

        private void updateSource() { this.source.setBuffer(getBuffer(activeBuffer).getBufferID()); }

        public boolean isPlaying() { return source.isPlaying(); }
        public void play() { source.play(); }
        public void pause() { source.pause(); }
        public void stop() { source.stop(); }
    }

}
