import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Sprite {
    private BufferedImage spriteSheet; // The entire sprite sheet
    private int frameWidth, frameHeight; // Dimensions of each frame
    private int scaleFactor = 3; // Scaling factor
    private int currentFrame = 0; // Current frame index
    private int totalFrames; // Total number of frames in the sprite sheet
    private boolean isVertical = false; // Indicates if the sprite sheet is vertical
    private boolean flipHorizontal = false; // Indicates if the sprite should be flipped horizontally

    private int x, y; // Position of the sprite

    public Sprite(String imagePath, int x, int y, int frameWidth, int frameHeight, int scaleFactor, boolean isVertical) {
        try {
            this.spriteSheet = ImageIO.read(new File(imagePath)); // Load the sprite sheet
        } catch (IOException e) {
            System.err.println("Error loading sprite sheet: " + imagePath);
            e.printStackTrace();
        }
        this.x = x;
        this.y = y;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.scaleFactor = scaleFactor;
        this.isVertical = isVertical;

        // Calculate the total number of frames in the sprite sheet
        if (isVertical) {
            this.totalFrames = spriteSheet.getHeight() / frameHeight; // Vertical layout
        } else {
            this.totalFrames = spriteSheet.getWidth() / frameWidth; // Horizontal layout
        }
    }

    public void draw(Graphics g) {
        // Extract the current frame from the sprite sheet
        BufferedImage frame;
        if (isVertical) {
            int frameY = currentFrame * frameHeight; // Y-coordinate of the current frame
            frame = spriteSheet.getSubimage(0, frameY, frameWidth, frameHeight); // Vertical layout
        } else {
            int frameX = currentFrame * frameWidth; // X-coordinate of the current frame
            frame = spriteSheet.getSubimage(frameX, 0, frameWidth, frameHeight); // Horizontal layout
        }

        // Apply transformations for flipping
        Graphics2D g2d = (Graphics2D) g.create(); // Create a copy of the Graphics object
        AffineTransform transform = new AffineTransform();

        // Scale the image based on the flipHorizontal flag
        if (flipHorizontal) {
            transform.translate(x + frameWidth * scaleFactor, y); // Translate to the flipped position
            transform.scale(-scaleFactor, scaleFactor); // Flip horizontally
        } else {
            transform.translate(x, y); // Translate to the normal position
            transform.scale(scaleFactor, scaleFactor); // Scale normally
        }

        // Draw the transformed frame
        g2d.drawImage(frame, transform, null);
        g2d.dispose(); // Dispose of the Graphics2D object
    }

    public void move(int dx, int dy) {
        x += dx; // Move horizontally
        y += dy; // Move vertically
    }

    public Rectangle getBounds() {
        // Return the bounding box for collision detection (scaled size)
        return new Rectangle(x, y, frameWidth * scaleFactor, frameHeight * scaleFactor);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setScaleFactor(int scaleFactor) {
        this.scaleFactor = scaleFactor; // Set the scaling factor
    }

    public int getScaleFactor() {
        return scaleFactor; // Get the current scaling factor
    }

    public int getHeight() {
        return frameHeight * scaleFactor; // Scaled height of the current frame
    }

    public int getWidth() {
        return frameWidth * scaleFactor; // Scaled width of the current frame
    }

    public void setCurrentFrame(int frameIndex) {
        // Set the current frame (ensure it's within bounds)
        if (frameIndex >= 0 && frameIndex < totalFrames) {
            this.currentFrame = frameIndex;
        }
    }

    public int getCurrentFrame() {
        return currentFrame; // Get the current frame index
    }

    public int getTotalFrames() {
        return totalFrames; // Get the total number of frames
    }

    public void setFlipHorizontal(boolean flipHorizontal) {
        this.flipHorizontal = flipHorizontal; // Set the flipHorizontal flag
    }

    public boolean isFlippedHorizontal() {
        return flipHorizontal; // Get the flipHorizontal flag
    }
}