package ass2.spec;

import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLProfile;
import java.io.File;
import java.io.IOException;

/** Texture loader and manager
 *
 * Created by sdba660 on 5/10/2014.
 * Adapted from malcolmr's MyTexture class
 */
public class Texture {
    private int[] textureID;

    public Texture(GL2 gl, String fileName) {
        TextureData data = null;
        textureID = new int[1];
        try {
            // Read in texture file
            File f = new File(fileName);
            // Create new TextureData
            data = TextureIO.newTextureData(GLProfile.getDefault(), f, false, null);
        } catch (IOException e) {
            System.err.println("Error loading file " + fileName);
            System.exit(1);
        }
        if (data == null) {
            System.err.println("Error loading file " + fileName);
            System.exit(1);
        }
        // Get new texture ID (#textures, textureName/ID, offset)
        gl.glGenTextures(1, textureID, 0);
        // Bind texture to ID
        gl.glBindTexture(GL.GL_TEXTURE_2D, textureID[0]);
        // Bind image data from file to texture
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0,
                data.getInternalFormat(),
                data.getWidth(),
                data.getHeight(),
                0,
                data.getPixelFormat(),
                data.getPixelType(),
                data.getBuffer());
        // Set texture params and generate mipmaps
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);
        gl.glGenerateMipmap(GL2.GL_TEXTURE_2D);
    }

    public int getTextureID() {
        return textureID[0];
    }

    public void bindTexture(GL2 gl) {
        gl.glBindTexture(GL.GL_TEXTURE_2D, textureID[0]);
    }

    public void releaseTexture(GL2 gl) {
        if (textureID[0] > 0) {
            gl.glDeleteTextures(1, textureID, 0);
        }
    }
}
