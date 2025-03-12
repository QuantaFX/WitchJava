import java.awt.Graphics;
import java.awt.Rectangle;

public class Platform {
    private int x, y, width, height;
    private boolean isObstacle;
    private boolean visible;

    // Wall properties
    private boolean hasLeftWall = false;
    private boolean hasRightWall = false;
    private int wallThickness = 10; // Thickness of the walls

    public Platform(int x, int y, int width, int height, boolean isObstacle, boolean visible,
                    boolean hasLeftWall, boolean hasRightWall) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isObstacle = isObstacle;
        this.visible = visible;
        this.hasLeftWall = hasLeftWall;
        this.hasRightWall = hasRightWall;
    }

    public void draw(Graphics g) {
        if (!visible) return; // Skip drawing if not visible

        // Draw the platform
        if (isObstacle) {
            g.setColor(java.awt.Color.RED); // Draw obstacles in red
        } else {
            g.setColor(java.awt.Color.GRAY); // Draw platforms in gray
        }
        g.fillRect(x, y, width, height);

        // Draw left wall if it exists
        if (hasLeftWall) {
            g.setColor(java.awt.Color.BLUE); // Example: Blue walls
            g.fillRect(x - wallThickness, y + 50, wallThickness, height - 50);
        }

        // Draw right wall if it exists
        if (hasRightWall) {
            g.setColor(java.awt.Color.BLUE); // Example: Blue walls
            g.fillRect(x + width, y + 50, wallThickness, height - 50);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height); // Bounding box for collision detection
    }

    public Rectangle getLeftWallBounds() {
        if (hasLeftWall) {
            return new Rectangle(x - wallThickness, y + 50, wallThickness, height - 50);
        }
        return null;
    }

    public Rectangle getRightWallBounds() {
        if (hasRightWall) {
            return new Rectangle(x + width, y + 50, wallThickness, height- 50);
        }
        return null;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isObstacle() {
        return isObstacle;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}