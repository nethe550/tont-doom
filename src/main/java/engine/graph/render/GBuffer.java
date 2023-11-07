package engine.graph.render;

import engine.Window;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL30.*;

public class GBuffer {

    private static final int TOTAL_TEXTURES = 4;

    private int gBufferID;
    private int width;
    private int height;
    private int[] textureIDs;

    public GBuffer(Window window) {
        gBufferID = glGenFramebuffers();
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, gBufferID);

        textureIDs = new int[TOTAL_TEXTURES];
        glGenTextures(textureIDs);

        width = window.getWidth();
        height = window.getHeight();

        for (int i = 0; i < TOTAL_TEXTURES; i++) {
            glBindTexture(GL_TEXTURE_2D, textureIDs[i]);

            int attachment;
            if (i == TOTAL_TEXTURES - 1) {
                attachment = GL_DEPTH_ATTACHMENT;
                glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
            }
            else {
                attachment = GL_COLOR_ATTACHMENT0 + i;
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, width, height, 0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);
            }
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            glFramebufferTexture2D(GL_FRAMEBUFFER, attachment, GL_TEXTURE_2D, textureIDs[i], 0);
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buf = stack.mallocInt(TOTAL_TEXTURES);
            for (int i = 0; i < TOTAL_TEXTURES; i++) {
                buf.put(i, GL_COLOR_ATTACHMENT0 + i);
            }
            glDrawBuffers(buf);
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void cleanup() {
        glDeleteFramebuffers(gBufferID);
        Arrays.stream(textureIDs).forEach(GL30::glDeleteTextures);
    }

    public int getGBufferID() { return gBufferID; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int[] getTextureIDs() { return textureIDs; }

}
