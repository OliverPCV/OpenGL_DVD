package educanet;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Game {

    private static final float[] vertices = {
            0.2f, 0.2f, 0.0f, // 0 -> Top right
            0.2f, -0.2f, 0.0f, // 1 -> Bottom right
            -0.2f, -0.2f, 0.0f, // 2 -> Bottom left
            -0.2f, 0.2f, 0.0f, // 3 -> Top left
    };

    private static final float[] colors = {
            1.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 0.0f,
    };

    private static final float[] textures = {
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
    };

    private static final int[] indices = {
            0, 1, 3, // First triangle
            1, 2, 3 // Second triangle
    };

    private static int squareVaoId;
    private static int squareVboId;
    private static int squareEboId;
    private static int colorsId;
    private static int textureIndicesId;
    private static int textureId;
    private static int uniformColorLocation;
    private static int uniformMatrixLocation;

    private static float spriteH;
    private static float spriteW;


    private static Matrix4f matrix = new Matrix4f()
            .identity();
    private static FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);


    public static void movement(float[] vertices){

    }

    public static void init(long window) {
        // Setup shaders
        Shaders.initShaders();

        uniformColorLocation = GL33.glGetUniformLocation(Shaders.shaderProgramId, "outColor");
        uniformMatrixLocation = GL33.glGetUniformLocation(Shaders.shaderProgramId, "matrix");

        // Generate all the ids
        squareVaoId = GL33.glGenVertexArrays();
        squareVboId = GL33.glGenBuffers();
        squareEboId = GL33.glGenBuffers();
        colorsId = GL33.glGenBuffers();
        textureIndicesId = GL33.glGenBuffers();

        textureId = GL33.glGenTextures();
        loadImage();

        GL33.glBindVertexArray(squareVaoId);

        // Tell OpenGL we are currently writing to this buffer (eboId)
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, squareEboId);
        IntBuffer ib = BufferUtils.createIntBuffer(indices.length)
                .put(indices)
                .flip();
        GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, ib, GL33.GL_STATIC_DRAW);

        // Change to VBOs...
        // Tell OpenGL we are currently writing to this buffer (vboId)
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, squareVboId);

        FloatBuffer fb = BufferUtils.createFloatBuffer(vertices.length)
                .put(vertices)
                .flip();

        // Send the buffer (positions) to the GPU
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, fb, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 0, 0);
        GL33.glEnableVertexAttribArray(0);


        GL33.glUseProgram(Shaders.shaderProgramId);
        GL33.glUniform3f(uniformColorLocation, 1.0f, 0.0f, 0.0f);

        // Sending Mat4 to GPU
        matrix.get(matrixBuffer);
        GL33.glUniformMatrix4fv(uniformMatrixLocation, false, matrixBuffer);

        // Send the buffer (positions) to the GPU
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, fb, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(0,3, GL33.GL_FLOAT, false, 0, 0);
        GL33.glEnableVertexAttribArray(0);

        // tell OpenGL we are currently writing into this buffer (vboId)
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, colorsId);

        FloatBuffer cb = BufferUtils.createFloatBuffer(colors.length)
                .put(colors)
                .flip();

        // Send the buffer (positions) to the GPU
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, cb, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(1,3, GL33.GL_FLOAT, false, 0, 0);
        GL33.glEnableVertexAttribArray(1);

        // Change to Textures...
        // Tell OpenGL we are currently writing to this buffer (colorsId)
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, textureIndicesId);

        FloatBuffer tb = BufferUtils.createFloatBuffer(textures.length)
                .put(textures)
                .flip();

        // Send the buffer (positions) to the GPU
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, tb, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(2, 2, GL33.GL_FLOAT, false, 0, 0);
        GL33.glEnableVertexAttribArray(2);

        // Clear the buffer from the memory (it's saved now on the GPU, no need for it here)
        MemoryUtil.memFree(fb);
    }

    public static void render(long window) {

        getPlayer(matrix);

        GL33.glUseProgram(Shaders.shaderProgramId);

        // Draw using the glDrawElements function
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, textureId);
        GL33.glBindVertexArray(squareVaoId);
        GL33.glDrawElements(GL33.GL_TRIANGLES, indices.length, GL33.GL_UNSIGNED_INT, 0);
    }


    public static void getPlayer(Matrix4f matrix){
        matrix.get(matrixBuffer);
    }


    public static void update(long window) {
        movePlayer(window, matrix);

        GL33.glUniformMatrix4fv(uniformMatrixLocation, false, matrixBuffer);
        GL33.glUseProgram(Shaders.shaderProgramId); // use this shader to render
        GL33.glBindVertexArray(squareVaoId);
        GL33.glDrawElements(GL33.GL_TRIANGLES, vertices.length, GL33.GL_UNSIGNED_INT, 0);
    }

    private static void loadImage() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            ByteBuffer img = STBImage.stbi_load("resources/textures/Cyborg_run.png", w, h, comp, 3);
            if (img != null) {
                img.flip();

                GL33.glBindTexture(GL33.GL_TEXTURE_2D, textureId);
                GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGB, w.get(), h.get(), 0, GL33.GL_RGB, GL33.GL_UNSIGNED_BYTE, img);
                GL33.glGenerateMipmap(GL33.GL_TEXTURE_2D);

                STBImage.stbi_image_free(img);
            }
        }


    }

    public static int timer = 0;

    public static boolean right = true;
    public static boolean up = false;
    static float baseSpeed = 0.01f;
    static float slow = 0.8f * baseSpeed; // 0.00015f
    static float fast = baseSpeed; // 0.0002f
    public static float playerTopLeftX = -0.125f; //TODO automatizovat
    public static float playerTopLeftY = 0.125f;



    public static void movePlayer(long window, Matrix4f matrix) {
        if (playerTopLeftX > 0.75f) { // on border hit flip x
            right = false;
        }
        else if (playerTopLeftX < -1f) { // on border hit flip x
            right = true;
        }

        if (playerTopLeftY > 1f) { // on border hit flip y
            up = false;
        }
        else if (playerTopLeftY < -0.75f) { // on border hit flip y
            up = true;
        }

        if (!right && !up) { // left down
            matrix = matrix.translate(-fast, -slow, 0f);
            playerTopLeftX -= fast;
            playerTopLeftY -= slow;
        }
        else if (!right && up) { // left up
            matrix = matrix.translate(-fast, slow, 0f);
            playerTopLeftX -= fast;
            playerTopLeftY += slow;
        }
        else if (right && !up) { // right down
            matrix = matrix.translate(fast, -slow, 0f);
            playerTopLeftX += fast;
            playerTopLeftY -= slow;
        }
        else { // right up
            matrix = matrix.translate(fast, slow, 0f);
            playerTopLeftX += fast;
            playerTopLeftY += slow;
        }


        /*timer++;
        if (timer % 100 == 0) {
            timer = 0;
        }*/

    }



}
