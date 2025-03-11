import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Game extends JPanel implements Runnable, KeyListener {
    private boolean running = true;
    private Sprite player;
    private int offsetX = 0; // Scroll offset for the player

    // Background layers
    private Image[] backgroundLayers = new Image[4];
    private int[] layerSpeeds = {1, 2, 3, 4}; // Speeds for each layer (slower to faster)
    private int[] layerOffsets = new int[4]; // Offsets for each layer

    public static void main(String[] args) {
        // Create the game window
        JFrame frame = new JFrame("Side Scroller Game");
        Game game = new Game(frame);
        frame.add(game);
        frame.setSize(800, 600); // Set window size
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Start the game loop in a separate thread
        new Thread(game).start();
    }

    private JFrame frame;
    private boolean isFullscreen = false;

    public Game(JFrame frame) {
        this.frame = frame;
        addKeyListener(this); // Add key listener
        setFocusable(true); // Allow the panel to receive focus
        setFocusTraversalKeysEnabled(false); // Disable default focus traversal

        // Initialize the player sprite with a sprite sheet
        player = new Sprite("assets/Blue_witch/B_witch_idle.png", 100, 400, 32, 48, 6, true); // Frame size: 64x64, Scale factor: 3

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
    }

    @Override
    public void run() {
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

        if (player.getX() > 500) {
            offsetX -= 1; // Move the background left
            player.move(-5, 0); // Adjust player position

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

        // Prevent falling through the ground
        if (player.getY() > getHeight() - player.getHeight()) {
            player.move(0, -(player.getY() - (getHeight() - player.getHeight())));
            velocityY = 0; // Reset velocity when on the ground
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
            int offset = layerOffsets[i];
            g.drawImage(backgroundLayers[i], offset, 0, getWidth(), getHeight(), this);
            g.drawImage(backgroundLayers[i], offset + getWidth(), 0, getWidth(), getHeight(), this); // Repeat for seamless scrolling
        }

        // Draw the player sprite
        player.draw(g);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        // Toggle fullscreen mode with F11
        if (keyCode == KeyEvent.VK_F11) {
            isFullscreen = !isFullscreen;
            if (isFullscreen) {
                frame.dispose(); // Dispose the current frame
                frame.setUndecorated(true); // Remove window decorations
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Set fullscreen
                frame.setVisible(true);
            } else {
                frame.dispose(); // Dispose the current frame
                frame.setUndecorated(false); // Restore window decorations
                frame.setSize(800, 600); // Restore original size
                frame.setVisible(true);
            }
            revalidate(); // Refresh layout
            repaint(); // Redraw components
        }

        // Movement using W, A, S, D keys
        if (keyCode == KeyEvent.VK_W && velocityY == 0) { // Jump only when on the ground
            velocityY = -10; // Set upward velocity for jumping
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