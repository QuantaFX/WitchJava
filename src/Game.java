import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

public class Game extends JPanel implements Runnable, KeyListener {
    private boolean running = true;
    private Sprite player;
    private double offsetX = 0; // Scroll offset for the player

    // Background layers
    private Image[] backgroundLayers = new Image[4];
    private double[] layerSpeeds = {1, 2, 3, 4}; // Speeds for each layer (slower to faster)
    private double[] layerOffsets = new double[4]; // Offsets for each layer

    private boolean showCollisionBoxes = false; // Debug flag for collision boxes

    private ArrayList<Platform> platforms = new ArrayList<>(); // List of platforms

    public static void main(String[] args) {
        // Create the game window
        JFrame frame = new JFrame("Side Scroller Game");
        Game game = new Game(frame);
        frame.add(game);
        frame.setSize(1920, 1080); // Set window size
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Start the game loop in a separate thread
        new Thread(game).start();
    }

    private JFrame frame;
    private boolean isFullscreen = true;


    public Game(JFrame frame) {
        this.frame = frame;
        addKeyListener(this); // Add key listener
        setFocusable(true); // Allow the panel to receive focus
        setFocusTraversalKeysEnabled(false); // Disable default focus traversal

        // Initialize the player sprite with a sprite sheet
        player = new Sprite("assets/Blue_witch/B_witch_idle.png", 1700, 600, 21, 39, 7, true); // Frame size: 64x64, Scale factor: 3

        // Load background layers
        try {
            backgroundLayers[0] = Toolkit.getDefaultToolkit().getImage("assets/Clouds/1.png");
            backgroundLayers[1] = Toolkit.getDefaultToolkit().getImage("assets/Clouds/2.png");
            backgroundLayers[2] = Toolkit.getDefaultToolkit().getImage("assets/Clouds/3.png");
            backgroundLayers[3] = Toolkit.getDefaultToolkit().getImage("assets/Clouds/4.png");

        } catch (Exception e) {
            System.err.println("Error loading background layers.");
            e.printStackTrace();
        }

        // Initialize platforms
        initializePlatforms();
    }

    private void initializePlatforms() {
        try {
            // Read the JSON file as a string
            String filePath = "Level1.json"; // Path to the JSON file
            String jsonContent = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath)));

            // Parse the JSON array
            JSONArray platformArray = new JSONArray(jsonContent);

            // Iterate through the array and create Platform objects
            for (int i = 0; i < platformArray.length(); i++) {
                JSONObject platformData = platformArray.getJSONObject(i);
                platforms.add(new Platform(
                        platformData.getInt("x"),
                        platformData.getInt("y"),
                        platformData.getInt("width"),
                        platformData.getInt("height"),
                        platformData.getBoolean("isObstacle"),
                        platformData.getBoolean("visible"),
                        platformData.optBoolean("hasLeftWall", false), // Default to false if not specified
                        platformData.optBoolean("hasRightWall", false) // Default to false if not specified
                ));
            }
        } catch (Exception e) {
            System.err.println("Error loading platforms from JSON file.");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        frame.dispose(); // Dispose the current frame
        frame.setUndecorated(true); // Remove window decorations
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Set fullscreen
        frame.setVisible(true);

        // Game loop
        while (running) {
            update(); // Update game logic
            repaint(); // Trigger rendering
            try {
                Thread.sleep(16); // Approx. 60 FPS (1000ms / 60 ≈ 16ms)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private int gravity = 1;
    private int velocityX = 0;
    private int velocityY = 0;
    private int animationCounter = 0;
    private final int ANIMATION_SPEED = 10; // Change frame every 10 updates

    private void update() {
        // Scroll when the player moves past a certain point

        animationCounter++;
        if (animationCounter >= ANIMATION_SPEED) {
            animationCounter = 0;

            // Cycle through frames
            int currentFrame = player.getCurrentFrame();
            player.setCurrentFrame((currentFrame + 1) % player.getTotalFrames());
        }

        if (running) {
            offsetX -= 0.1; // Move the background left

            // Update offsets for each background layer based on their speeds
            for (int i = 0; i < layerOffsets.length; i++) {
                layerOffsets[i] = (offsetX * layerSpeeds[i]) % getWidth();
            }
        }

        if (player.getX() < 0) {
            player.move(5, 0); // Prevent moving off the left edge
        }
        if (player.getY() < 0) {
            player.move(0, 10); // Prevent moving off the top edge
        }
        if (player.getX() > getWidth() - player.getWidth()) {
            player.move(-5, 0); // Prevent moving off the right edge
        }
        if (player.getY() > getHeight() - player.getHeight()) {
            player.move(0, -10); // Prevent moving off the bottom edge
        }

        velocityY += gravity; // Apply gravity
        player.move(velocityX, velocityY); // Update position based on velocity

        handleCollisions();

        // Prevent falling through the ground
        if (player.getY() > getHeight() - player.getHeight()){
            player.move(0, -(player.getY() - (getHeight()  - player.getHeight())));
            velocityY = 0; // Reset velocity when on the ground
        }
    }

    private void handleCollisions() {
        Rectangle playerBounds = player.getBounds();

        for (Platform platform : platforms) {
            Rectangle platformBounds = platform.getBounds();
            Rectangle leftWallBounds = platform.getLeftWallBounds();
            Rectangle rightWallBounds = platform.getRightWallBounds();

            // Check collision with the platform
            if (playerBounds.intersects(platformBounds)) {
                if (platform.isObstacle()) {
                    // Handle collision with an obstacle (e.g., reset player position or reduce health)
                    System.out.println("Player hit an obstacle!");
                    player.move(-100, 0); // Example: Push player back
                } else {
                    // Handle collision with a regular platform
                    if (velocityY > 0 && player.getY() + player.getHeight() <= platform.getY() + 40) {
                        // Player lands on top of the platform
                        player.move(0, -(player.getY() + player.getHeight() - platform.getY()));
                        velocityY = 0; // Stop falling
                    }
                }
            }

            // Check collision with the left wall
            if (leftWallBounds != null && playerBounds.intersects(leftWallBounds)) {
                player.move(-5, 0); // Push player away from the left wall
            }

            // Check collision with the right wall
            if (rightWallBounds != null && playerBounds.intersects(rightWallBounds)) {
                player.move(5, 0); // Push player away from the right wall
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        render(g); // Render sprites and other elements
    }

    private void render(Graphics g) {
        // Draw background layers with parallax effect
        for (int i = 0; i < backgroundLayers.length; i++) {
            int offset = (int) layerOffsets[i];
            g.drawImage(backgroundLayers[i], offset, 0, getWidth(), getHeight(), this);
            g.drawImage(backgroundLayers[i], offset + getWidth(), 0, getWidth(), getHeight(), this); // Repeat for seamless scrolling
        }

        g.drawImage(Toolkit.getDefaultToolkit().getImage("assets/Level1.png"), 0, 0, getWidth(), getHeight(), this);

        // Draw platforms
        for (Platform platform : platforms) {
            platform.draw(g);

            // Draw platform collision box
            if (showCollisionBoxes) {
                Rectangle platformBounds = platform.getBounds();
                g.setColor(Color.GREEN); // Example: Green for platforms
                g.drawRect(platformBounds.x, platformBounds.y, platformBounds.width, platformBounds.height);

                // Draw left wall collision box
                Rectangle leftWallBounds = platform.getLeftWallBounds();
                if (leftWallBounds != null) {
                    g.setColor(Color.BLUE); // Example: Blue for walls
                    g.drawRect(leftWallBounds.x, leftWallBounds.y, leftWallBounds.width, leftWallBounds.height);
                }

                // Draw right wall collision box
                Rectangle rightWallBounds = platform.getRightWallBounds();
                if (rightWallBounds != null) {
                    g.setColor(Color.BLUE); // Example: Blue for walls
                    g.drawRect(rightWallBounds.x, rightWallBounds.y, rightWallBounds.width, rightWallBounds.height);
                }
            }
        }

        // Draw the player sprite
        player.draw(g);

        // Draw player collision box
        if (showCollisionBoxes) {
            Rectangle playerBounds = player.getBounds();
            g.setColor(Color.RED); // Example: Red for player
            g.drawRect(playerBounds.x, playerBounds.y, playerBounds.width, playerBounds.height);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_F3) {
            showCollisionBoxes = !showCollisionBoxes; // Toggle debug mode
            System.out.println("Collision boxes: " + (showCollisionBoxes ? "Visible" : "Hidden"));
        }

        // Movement using W, A, S, D keys
        if (keyCode == KeyEvent.VK_W && velocityY == 0) { // Jump only when on the ground
            velocityY = -25; // Set upward velocity for jumping
        } else if (keyCode == KeyEvent.VK_A) {
            velocityX = -5; // Move left
            player.setFlipHorizontal(true); // Flip sprite horizontally
        } else if (keyCode == KeyEvent.VK_S) {
            velocityY = 10; // Move down
        } else if (keyCode == KeyEvent.VK_D) {
            velocityX = 5; // Move right
            player.setFlipHorizontal(false); // Flip sprite horizontally
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_D) {
            velocityX = 0; // Stop horizontal movement
        }
        if (keyCode == KeyEvent.VK_S) {
            velocityY = 0; // Stop downward movement
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}