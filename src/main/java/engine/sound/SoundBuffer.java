package engine.sound;

import org.lwjgl.system.*;
import org.lwjgl.stb.STBVorbisInfo;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class SoundBuffer {

    public enum FileType {
        OGG
    };

    private final int bufferID;

    private ShortBuffer pcm;

    public SoundBuffer(String filePath, FileType fileType) {
        this.bufferID = alGenBuffers();
        try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
            switch (fileType) {
                case OGG -> pcm = readVorbis(filePath, info);
            }
            alBufferData(bufferID, info.channels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, pcm, info.sample_rate());
        }
    }

    public void cleanup() {
        alDeleteBuffers(this.bufferID);
        if (pcm != null) MemoryUtil.memFree(pcm);
    }

    public int getBufferID() { return bufferID; }

    private ShortBuffer readVorbis(String filePath, STBVorbisInfo info) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer error = stack.mallocInt(1);
            long decoder = stb_vorbis_open_filename(filePath, error, null);
            if (decoder == NULL) throw new RuntimeException("Failed to open Ogg Vorbis file: " + error.get(0));

            stb_vorbis_get_info(decoder, info);
            int channels = info.channels();
            int lengthSamples = stb_vorbis_stream_length_in_samples(decoder);

            ShortBuffer result = MemoryUtil.memAllocShort(lengthSamples * channels);
            result.limit(stb_vorbis_get_samples_short_interleaved(decoder, channels, result) * channels);
            stb_vorbis_close(decoder);

            return result;
        }
    }

}
