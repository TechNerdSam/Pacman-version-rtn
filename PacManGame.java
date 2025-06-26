import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

/**
 * EN: The main class for the Cyber Runner game, a Pac-Man style game.
 * It handles the game window, game states, game logic, and user interface.
 * FR: La classe principale du jeu Cyber Runner, un jeu de style Pac-Man.
 * Elle gère la fenêtre de jeu, les états de jeu, la logique de jeu et l'interface utilisateur.
 */
public class PacManGame extends JFrame implements KeyListener, ActionListener {

    // =================================================================================
    // Constants
    // =================================================================================

    /**
     * EN: The width of the game window in pixels.
     * FR: La largeur de la fenêtre de jeu en pixels.
     */
    private static final int WIDTH = 800;
    /**
     * EN: The height of the game window in pixels.
     * FR: La hauteur de la fenêtre de jeu en pixels.
     */
    private static final int HEIGHT = 800;
    /**
     * EN: The size of each cell in the game grid in pixels.
     * FR: La taille de chaque cellule dans la grille de jeu en pixels.
     */
    private static final int CELL_SIZE = 40;
    /**
     * EN: The filename for storing high scores.
     * FR: Le nom du fichier pour stocker les meilleurs scores.
     */
    private static final String HIGHSCORE_FILE = "highscores.dat";
    /**
     * EN: The filename for storing the player's profile.
     * FR: Le nom du fichier pour stocker le profil du joueur.
     */
    private static final String PROFILE_FILE = "player_profile.dat";
    /**
     * EN: The maximum number of high scores to store.
     * FR: Le nombre maximum de meilleurs scores à stocker.
     */
    private static final int MAX_HIGHSCORES = 5;

    // =================================================================================
    // Game State Enum
    // =================================================================================

    /**
     * EN: Represents the different states of the game.
     * FR: Représente les différents états du jeu.
     */
    private enum GameState {
        MENU, PLAYING, PAUSED, GAME_OVER, WIN, HIGHSCORES
    }

    private GameState currentGameState;

    // =================================================================================
    // Game Elements
    // =================================================================================

    /**
     * EN: The current position of the player on the grid.
     * FR: La position actuelle du joueur sur la grille.
     */
    private Point playerPosition;
    /**
     * EN: The list of dots that the player needs to collect.
     * FR: La liste des points que le joueur doit collecter.
     */
    private List<Point> dots;
    /**
     * EN: The set of obstacles (walls) on the grid.
     * FR: L'ensemble des obstacles (murs) sur la grille.
     */
    private Set<Point> obstacles;
    /**
     * EN: The player's current score.
     * FR: Le score actuel du joueur.
     */
    private int score;
    /**
     * EN: The main timer for the game loop.
     * FR: Le minuteur principal pour la boucle de jeu.
     */
    private Timer gameTimer;
    /**
     * EN: A random number generator for various game events.
     * FR: Un générateur de nombres aléatoires pour divers événements de jeu.
     */
    private Random random;

    // =================================================================================
    // Level and Score Management
    // =================================================================================

    private int currentLevel = 1;
    private int maxLevel = 20;
    private int unlockedLevel = 1;
    private int targetScore;

    // =================================================================================
    // Grid Dimensions
    // =================================================================================

    private int gridCols;
    private int gridRows;

    // =================================================================================
    // UI Components
    // =================================================================================

    private JLayeredPane layeredPane;
    private GamePanel gamePanel;
    private JPanel endScreenPanel, highScoresPanel, creditsPanel;
    private JLabel endMessageLabel;
    private JTextArea highScoresDisplay;

    /**
     * EN: The list of high score entries.
     * FR: La liste des entrées de meilleurs scores.
     */
    private List<HighScoreEntry> highScores;

    // =================================================================================
    // Player Profile
    // =================================================================================

    /**
     * EN: Represents the player's profile, storing statistics.
     * FR: Représente le profil du joueur, stockant des statistiques.
     */
    private static class PlayerProfile implements Serializable {
        private static final long serialVersionUID = 1L;
        long totalScore = 0;
        int powerupsCollected = 0;
        int enemiesDefeated = 0;
        int levelsCompleted = 0;
    }
    private PlayerProfile playerProfile;
    
    // =================================================================================
    // UI Themes
    // =================================================================================

    /**
     * EN: Represents the different UI themes for the game.
     * FR: Représente les différents thèmes d'interface utilisateur pour le jeu.
     */
    private enum UITheme {
        CYBER_NEON("Cyber Néon", new Color(5, 5, 15), new Color(0, 80, 200), new Color(0, 180, 255, 150), Color.CYAN),
        VOLCANIC_CORE("Noyau Volcanique", new Color(20, 5, 5), new Color(180, 50, 0), new Color(255, 100, 0, 150), Color.ORANGE),
        ARCTIC_MATRIX("Matrice Arctique", new Color(5, 15, 20), new Color(0, 100, 120), new Color(100, 200, 255, 150), Color.WHITE);

        final String name; final Color bgColor; final Color wallColor; final Color wallGlow; final Color accentColor;
        UITheme(String name, Color bg, Color wall, Color glow, Color accent) {
            this.name = name; this.bgColor = bg; this.wallColor = wall; this.wallGlow = glow; this.accentColor = accent;
        }
    }
    private UITheme currentTheme = UITheme.CYBER_NEON;

    // =================================================================================
    // Menu Logic
    // =================================================================================

    private CardLayout menuCardLayout;
    private JPanel menuContainerPanel;
    private MenuBackgroundPanel menuBackgroundPanel;
    private Timer menuTimer;
    private boolean backgroundAnimationEnabled = true;
    
    private Timer glitchTimer;
    private JLabel titleLabel;

    private Timer fadeTimer;
    private float fadeAlpha = 0f;
    private boolean isFadingOut = false;
    private String targetCard = "";

    // =================================================================================
    // Sound Management
    // =================================================================================
    private SoundManager soundManager;


    // =================================================================================
    // Power-ups
    // =================================================================================

    private enum PowerUpType { SUPER_PELLET, FREEZE, SHIELD }
    private static class PowerUp {
        Point position; PowerUpType type; long spawnTime;
        PowerUp(Point position, PowerUpType type) { this.position = position; this.type = type; this.spawnTime = System.currentTimeMillis(); }
    }
    private List<PowerUp> powerUps;
    private boolean isShieldActive = false, areEnemiesFrozen = false, areEnemiesVulnerable = false;
    private long shieldEndTime = 0, freezeEndTime = 0, vulnerableEndTime = 0;

    // =================================================================================
    // Enemies
    // =================================================================================

    private enum EnemyState { PATROLLING, AGGRO_TELEGRAPH, CHASING, FLEEING }
    private enum EnemyBehavior { HUNTER, AMBUSHER, FLANKER, ROAMER }
    private static class Enemy {
        Point position; EnemyBehavior behavior; EnemyState state = EnemyState.PATROLLING;
        long stateChangeTime = 0;
        int pathRecalculationCounter = 0;

        Enemy(Point position, EnemyBehavior behavior) {
            this.position = position; this.behavior = behavior;
        }
        
        void changeState(EnemyState newState) {
            if(this.state != newState) {
                this.state = newState;
                this.stateChangeTime = System.currentTimeMillis();
            }
        }
    }
    private List<Enemy> enemies;
    private Set<EnemyBehavior> discoveredEnemies = new HashSet<>();

    /**
     * EN: Constructor for the PacManGame class. Initializes the game window and all game components.
     * FR: Constructeur pour la classe PacManGame. Initialise la fenêtre de jeu et tous les composants du jeu.
     */
    public PacManGame() {
        setTitle("Cyber Runner");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        
        powerUps = new ArrayList<>();
        enemies = new ArrayList<>();
        random = new Random();
        highScores = new ArrayList<>();
        
        soundManager = new SoundManager();

        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        getContentPane().add(layeredPane, BorderLayout.CENTER);

        gamePanel = new GamePanel();
        gamePanel.setBounds(0, 0, WIDTH, HEIGHT);
        layeredPane.add(gamePanel, JLayeredPane.DEFAULT_LAYER, 0);
        
        initUIComponents();
        addKeyListener(this);
        setFocusable(true);

        initGame();
        loadHighScores();
        loadProfile();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                saveHighScores();
                saveProfile();
            }
        });
        showMenu();
    }
    
    /**
     * EN: Initializes all UI components, including menus and game panels.
     * FR: Initialise tous les composants de l'interface utilisateur, y compris les menus et les panneaux de jeu.
     */
    private void initUIComponents() {
        menuCardLayout = new CardLayout();
        menuContainerPanel = new JPanel(menuCardLayout);
        menuContainerPanel.setBounds(0, 0, WIDTH, HEIGHT);
        menuContainerPanel.setOpaque(false);
        
        menuBackgroundPanel = new MenuBackgroundPanel();
        menuBackgroundPanel.setBounds(0, 0, WIDTH, HEIGHT);

        JPanel mainMenuPanel = createTransparentPanel(new GridBagLayout());
        GridBagConstraints gbc = createGBC();

        titleLabel = createLabel("CYBER RUNNER", currentTheme.accentColor, new Font("Orbitron", Font.BOLD, 56), SwingConstants.CENTER);
        mainMenuPanel.add(titleLabel, gbc);

        gbc.insets = new Insets(10, 0, 10, 0);
        mainMenuPanel.add(new AnimatedButton("Nouvelle Partie", e -> transitionTo("LEVEL_SELECT")), gbc);
        mainMenuPanel.add(new AnimatedButton("Meilleurs Scores", e -> showHighScores()), gbc);
        mainMenuPanel.add(new AnimatedButton("Crédits", e -> showCredits()), gbc);
        mainMenuPanel.add(new AnimatedButton("Portfolio", e -> openURL("https://github.com/TechNerdSam")), gbc);
        mainMenuPanel.add(new AnimatedButton("Contact", e -> openURL("mailto:samynantoy@gmail.com")), gbc);
        mainMenuPanel.add(new AnimatedButton("Options", e -> transitionTo("OPTIONS")), gbc);
        mainMenuPanel.add(new AnimatedButton("Quitter", e -> System.exit(0)), gbc);
        
        JPanel levelSelectPanel = createTransparentPanel(new GridBagLayout());
        levelSelectPanel.add(createLabel("SÉLECTION DE MISSION", Color.YELLOW, new Font("Orbitron", Font.BOLD, 36), SwingConstants.CENTER), gbc);
        JPanel levelGrid = new JPanel(new GridLayout(4, 5, 10, 10));
        levelGrid.setOpaque(false);
        for(int i = 1; i <= 20; i++) {
            int levelNum = i;
            AnimatedButton levelButton = new AnimatedButton(String.valueOf(i));
            levelButton.setEnabled(i <= unlockedLevel);
            levelButton.addActionListener(e -> startPredefinedGame(levelNum));
            levelGrid.add(levelButton);
        }
        levelSelectPanel.add(levelGrid, gbc);
        levelSelectPanel.add(new AnimatedButton("Retour", e -> transitionTo("MAIN")), gbc);
        
        creditsPanel = createTransparentPanel(new GridBagLayout());
        
        JPanel optionsPanel = createTransparentPanel(new GridBagLayout());
        optionsPanel.add(createLabel("OPTIONS", Color.YELLOW, new Font("Orbitron", Font.BOLD, 36), SwingConstants.CENTER), gbc);
        JCheckBox animCheckbox = new JCheckBox("Activer l'arrière-plan animé");
        configureCheckbox(animCheckbox);
        animCheckbox.setSelected(true);
        animCheckbox.addActionListener(e -> backgroundAnimationEnabled = animCheckbox.isSelected());
        optionsPanel.add(animCheckbox, gbc);
        
        JComboBox<String> themeSelector = new JComboBox<>(new String[]{UITheme.CYBER_NEON.name, UITheme.VOLCANIC_CORE.name, UITheme.ARCTIC_MATRIX.name});
        themeSelector.addActionListener(e -> {
            String selected = (String) themeSelector.getSelectedItem();
            for(UITheme theme : UITheme.values()) {
                if(theme.name.equals(selected)) { currentTheme = theme; break; }
            }
            titleLabel.setForeground(currentTheme.accentColor);
        });
        optionsPanel.add(themeSelector, gbc);
        optionsPanel.add(new AnimatedButton("Retour", e -> transitionTo("MAIN")), gbc);

        menuContainerPanel.add(mainMenuPanel, "MAIN");
        menuContainerPanel.add(levelSelectPanel, "LEVEL_SELECT");
        menuContainerPanel.add(creditsPanel, "CREDITS");
        menuContainerPanel.add(optionsPanel, "OPTIONS");

        layeredPane.add(menuBackgroundPanel, JLayeredPane.DEFAULT_LAYER, 0);
        layeredPane.add(menuContainerPanel, JLayeredPane.PALETTE_LAYER, 1);
        
        endScreenPanel = new JPanel();
        endScreenPanel.setBounds(0, 0, WIDTH, HEIGHT);
        endScreenPanel.setBackground(new Color(10, 5, 15, 235));
        endScreenPanel.setLayout(new GridBagLayout());
        endScreenPanel.setVisible(false);
        
        GridBagConstraints gbcEnd = new GridBagConstraints();
        gbcEnd.gridwidth = GridBagConstraints.REMAINDER;
        gbcEnd.insets = new Insets(15, 0, 15, 0);

        endMessageLabel = createLabel("", Color.RED, new Font("Ebrima", Font.BOLD, 48), SwingConstants.CENTER);
        endScreenPanel.add(endMessageLabel, gbcEnd);

        JLabel endSubMessage = createLabel("Votre score a été enregistré.", Color.LIGHT_GRAY, new Font("Ebrima", Font.PLAIN, 22), SwingConstants.CENTER);
        endScreenPanel.add(endSubMessage, gbcEnd);
        
        gbcEnd.insets = new Insets(50, 0, 15, 0);
        endScreenPanel.add(new AnimatedButton("Rejouer", e -> { restartGame(); hideEndScreen(); }), gbcEnd);
        gbcEnd.insets = new Insets(15, 0, 15, 0);
        endScreenPanel.add(new AnimatedButton("Quitter au Menu", e -> showMenu()), gbcEnd);
        layeredPane.add(endScreenPanel, JLayeredPane.MODAL_LAYER, 2);
        
        highScoresPanel = new JPanel();
        highScoresPanel.setBounds(0, 0, WIDTH, HEIGHT);
        highScoresPanel.setBackground(new Color(10, 5, 15, 245));
        highScoresPanel.setLayout(new GridBagLayout());
        highScoresPanel.setVisible(false);

        GridBagConstraints gbcHigh = new GridBagConstraints();
        gbcHigh.gridwidth = GridBagConstraints.REMAINDER;
        gbcHigh.insets = new Insets(10, 0, 10, 0);
        gbcHigh.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel highScoresTitleLabel = createLabel("PANTHÉON DES HACKERS", new Color(0, 200, 255), new Font("Ebrima", Font.BOLD, 40), SwingConstants.CENTER);
        gbcHigh.insets = new Insets(0, 0, 40, 0);
        highScoresPanel.add(highScoresTitleLabel, gbcHigh);

        highScoresDisplay = new JTextArea(10, 30);
        highScoresDisplay.setEditable(false);
        highScoresDisplay.setBackground(new Color(15, 10, 25));
        highScoresDisplay.setForeground(Color.WHITE);
        highScoresDisplay.setFont(new Font("Consolas", Font.BOLD, 24));
        highScoresDisplay.setMargin(new Insets(20, 20, 20, 20));
        
        JScrollPane scrollPane = new JScrollPane(highScoresDisplay);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0, 200, 255), 2));
        scrollPane.getViewport().setBackground(new Color(15, 10, 25));
        
        gbcHigh.insets = new Insets(10, 50, 10, 50);
        highScoresPanel.add(scrollPane, gbcHigh);

        gbcHigh.insets = new Insets(40, 0, 10, 0);
        highScoresPanel.add(new AnimatedButton("Retour au Menu", e -> showMenu()), gbcHigh);
        layeredPane.add(highScoresPanel, JLayeredPane.MODAL_LAYER, 3);
        
        fadeTimer = new Timer(25, e -> {
            if (isFadingOut) {
                fadeAlpha += 0.1f;
                if (fadeAlpha >= 1.0f) {
                    fadeAlpha = 1.0f;
                    menuCardLayout.show(menuContainerPanel, targetCard);
                    isFadingOut = false;
                }
            } else {
                fadeAlpha -= 0.1f;
                if (fadeAlpha <= 0.0f) {
                    fadeAlpha = 0.0f;
                    fadeTimer.stop();
                }
            }
            layeredPane.repaint();
        });
    }

    /**
     * EN: Creates a GridBagConstraints object with default settings for the menu UI.
     * FR: Crée un objet GridBagConstraints avec des paramètres par défaut pour l'interface utilisateur du menu.
     * @return A new GridBagConstraints object.
     */
    private GridBagConstraints createGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 0, 20, 0);
        return gbc;
    }

    /**
     * EN: Configures the appearance of a JCheckBox for the menu.
     * FR: Configure l'apparence d'une JCheckBox pour le menu.
     * @param cb The JCheckBox to configure.
     */
    private void configureCheckbox(JCheckBox cb) {
        cb.setFont(new Font("Orbitron", Font.PLAIN, 20));
        cb.setOpaque(false);
        cb.setForeground(Color.WHITE);
    }

    /**
     * EN: Starts a new game at a specific level.
     * FR: Commence une nouvelle partie à un niveau spécifique.
     * @param level The level to start the game at.
     */
    private void startPredefinedGame(int level) {
        currentLevel = level;
        startGame();
    }

    /**
     * EN: Creates a transparent JPanel with a specified layout manager.
     * FR: Crée un JPanel transparent avec un gestionnaire de layout spécifié.
     * @param layout The layout manager to use for the panel.
     * @return A new transparent JPanel.
     */
    private JPanel createTransparentPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    /**
     * EN: Creates a JLabel with specified text, color, font, and alignment.
     * FR: Crée un JLabel avec un texte, une couleur, une police et un alignement spécifiés.
     * @param text The text to display on the label.
     * @param fgColor The foreground color of the label.
     * @param font The font to use for the label.
     * @param horizontalAlignment The horizontal alignment of the label's text.
     * @return A new configured JLabel.
     */
    private JLabel createLabel(String text, Color fgColor, Font font, int horizontalAlignment) {
        JLabel label = new JLabel(text, horizontalAlignment);
        label.setForeground(fgColor);
        label.setFont(font);
        return label;
    }
    
    /**
     * EN: Opens a URL in the default web browser.
     * FR: Ouvre une URL dans le navigateur web par défaut.
     * @param url The URL to open.
     */
    private void openURL(String url) {
        try {
            soundManager.play("click");
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Impossible d'ouvrir le lien.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * EN: Displays the credits screen.
     * FR: Affiche l'écran des crédits.
     */
    private void showCredits() {
        creditsPanel.removeAll();
        GridBagConstraints gbc = createGBC();
        gbc.insets = new Insets(15,0,15,0);
        creditsPanel.add(createLabel("CRÉDITS", Color.YELLOW, new Font("Orbitron", Font.BOLD, 36), SwingConstants.CENTER), gbc);
        creditsPanel.add(createLabel("Conception et Développement", Color.WHITE, new Font("Ebrima", Font.PLAIN, 22), SwingConstants.CENTER), gbc);
        creditsPanel.add(createLabel("Samyn-Antoy ABASSE", new Color(0, 200, 255), new Font("Ebrima", Font.BOLD, 28), SwingConstants.CENTER), gbc);
        
        gbc.insets = new Insets(40,0,15,0);
        creditsPanel.add(createLabel("Inspiré par les jeux de labyrinthe classiques.", Color.GRAY, new Font("Ebrima", Font.ITALIC, 18), SwingConstants.CENTER), gbc);
        
        gbc.insets = new Insets(50,0,15,0);
        creditsPanel.add(new AnimatedButton("Retour", e -> transitionTo("MAIN")), gbc);

        creditsPanel.revalidate();
        creditsPanel.repaint();
        transitionTo("CREDITS");
    }

    /**
     * EN: Initializes or resets the game state to its default values.
     * FR: Initialise ou réinitialise l'état du jeu à ses valeurs par défaut.
     */
    private void initGame() {
        currentGameState = GameState.MENU;
        score = 0;
        playerPosition = new Point(1, 1);
        dots = new ArrayList<>();
        obstacles = new HashSet<>();
        gridCols = WIDTH / CELL_SIZE;
        gridRows = HEIGHT / CELL_SIZE;
        gameTimer = new Timer(16, this);

        glitchTimer = new Timer(100, e -> {
            if(currentGameState == GameState.MENU) {
                if (random.nextInt(10) < 2) {
                    int offsetX = random.nextInt(11) - 5;
                    int offsetY = random.nextInt(7) - 3;
                    titleLabel.setLocation(titleLabel.getX() + offsetX, titleLabel.getY() + offsetY);
                    Timer resetTimer = new Timer(50, ae -> titleLabel.setLocation(titleLabel.getX() - offsetX, titleLabel.getY() - offsetY));
                    resetTimer.setRepeats(false);
                    resetTimer.start();
                }
            }
        });

        menuTimer = new Timer(16, e -> { if(currentGameState == GameState.MENU) menuBackgroundPanel.repaint(); });
        gameTimer.stop();
        menuTimer.stop();
        glitchTimer.stop();
    }
    
    /**
     * EN: Starts the game, hiding the menu and showing the game panel.
     * FR: Démarre le jeu, en masquant le menu et en affichant le panneau de jeu.
     */
    private void startGame() {
        soundManager.stop("menu_music");
        
        setMenuUIVisible(false);
        currentGameState = GameState.PLAYING;
        score = 0;
        initGameElementsForLevel(currentLevel);
        gamePanel.setVisible(true);
        gameTimer.start();
        this.requestFocusInWindow();
    }
    
    /**
     * EN: Initializes game elements for a specific level.
     * FR: Initialise les éléments de jeu pour un niveau spécifique.
     * @param level The level to initialize.
     */
    private void initGameElementsForLevel(int level) {
        playerPosition = new Point(1, 1);
        createMazeAndDotsForLevel(level);
        targetScore = dots.size() * 10;
        
        placeEnemies();
        placePowerUps();

        int newDelay = Math.max(16, 120 - (level - 1) * 5);
        gameTimer.setDelay(newDelay);
        gamePanel.repaint();
    }

    /**
     * EN: Creates the maze, dots, and obstacles for a given level.
     * FR: Crée le labyrinthe, les points et les obstacles pour un niveau donné.
     * @param level The current level, affecting the density of obstacles.
     */
    private void createMazeAndDotsForLevel(int level) {
        dots.clear(); obstacles.clear();
        for (int i = 0; i < gridCols; i++) {
            for (int j = 0; j < gridRows; j++) {
                if (i == 0 || j == 0 || i == gridCols - 1 || j == gridRows - 1) obstacles.add(new Point(i, j));
                else if (random.nextInt(100) < 20 + (level % 5)) obstacles.add(new Point(i, j));
                else dots.add(new Point(i, j));
            }
        }
        obstacles.remove(new Point(1, 1));
        dots.remove(new Point(1, 1));
    }
    
    /**
     * EN: Places enemies on the grid at random available positions.
     * FR: Place les ennemis sur la grille à des positions disponibles aléatoires.
     */
    private void placeEnemies() {
        enemies.clear();
        List<Point> availablePositions = getAvailablePositions();
        availablePositions.removeIf(p -> p.distance(playerPosition) < 5);
        Collections.shuffle(availablePositions, random);
        
        int spawnDelay = 500;
        int enemyCount = Math.min(4, availablePositions.size());
        for(int i = 0; i < enemyCount; i++) {
            final int index = i;
            if (availablePositions.isEmpty()) break;
            Point spawnPoint = availablePositions.remove(0);
            Timer spawnTimer = new Timer(spawnDelay * (i + 1), (e) -> {
                EnemyBehavior behavior = EnemyBehavior.values()[index % EnemyBehavior.values().length];
                enemies.add(new Enemy(spawnPoint, behavior));
                ((Timer)e.getSource()).stop();
            });
            spawnTimer.setRepeats(false);
            spawnTimer.start();
        }
    }

    /**
     * EN: Places power-ups on the grid at random available positions.
     * FR: Place les power-ups sur la grille à des positions disponibles aléatoires.
     */
    private void placePowerUps() {
        powerUps.clear();
        List<Point> availablePositions = getAvailablePositions();
        Collections.shuffle(availablePositions, random);
        if(availablePositions.size() > 0) powerUps.add(new PowerUp(availablePositions.remove(0), PowerUpType.SUPER_PELLET));
        if(currentLevel % 2 == 0 && availablePositions.size() > 0) powerUps.add(new PowerUp(availablePositions.remove(0), PowerUpType.FREEZE));
        if(random.nextInt(100) < 40 && availablePositions.size() > 0) powerUps.add(new PowerUp(availablePositions.remove(0), PowerUpType.SHIELD));
    }
    
    /**
     * EN: Gets a list of all available (non-obstacle, non-player) positions on the grid.
     * FR: Obtient une liste de toutes les positions disponibles (non-obstacle, non-joueur) sur la grille.
     * @return A list of available points.
     */
    private List<Point> getAvailablePositions() {
        List<Point> available = new ArrayList<>();
        for (int r = 1; r < gridRows - 1; r++) {
            for (int c = 1; c < gridCols - 1; c++) {
                Point p = new Point(c, r);
                if (!obstacles.contains(p) && !p.equals(playerPosition)) available.add(p);
            }
        }
        return available;
    }


    /**
     * EN: Updates the game logic, such as power-up timers and enemy states.
     * FR: Met à jour la logique du jeu, comme les minuteurs de power-up et les états des ennemis.
     */
    private void updateGameLogic() {
        long currentTime = System.currentTimeMillis();
        if (isShieldActive && currentTime > shieldEndTime) isShieldActive = false;
        if (areEnemiesFrozen && currentTime > freezeEndTime) areEnemiesFrozen = false;
        if (areEnemiesVulnerable && currentTime > vulnerableEndTime) {
            areEnemiesVulnerable = false;
            for(Enemy e : enemies) e.changeState(EnemyState.PATROLLING);
        }
        
        for (Enemy enemy : enemies) {
            if(enemy.state == EnemyState.AGGRO_TELEGRAPH && currentTime - enemy.stateChangeTime > 300) {
                enemy.changeState(EnemyState.CHASING);
            }
        }
    }
    
    /**
     * EN: Handles the game over sequence.
     * FR: Gère la séquence de fin de partie.
     */
    private void gameOver() {
        if (currentGameState == GameState.PLAYING) {
            currentGameState = GameState.GAME_OVER; 
            gameTimer.stop();
            endMessageLabel.setText("RAPPORT DE FIN DE MISSION");
            endMessageLabel.setForeground(new Color(255, 80, 80));
            endScreenPanel.setVisible(true);
            layeredPane.repaint();
            askForNameAndAddHighScore(score);
            this.requestFocusInWindow();
        }
    }

    /**
     * EN: Handles the level completion sequence.
     * FR: Gère la séquence de fin de niveau.
     */
    private void gameWinLevel() {
        gameTimer.stop();
        currentLevel++;
        if(currentLevel > unlockedLevel) unlockedLevel = currentLevel;
        playerProfile.levelsCompleted++;
        score += 1000;
        
        if (currentLevel > maxLevel) {
            currentGameState = GameState.WIN;
            endMessageLabel.setText("SYSTÈME PÉNETRÉ");
            endMessageLabel.setForeground(Color.GREEN);
            endScreenPanel.setVisible(true);
            askForNameAndAddHighScore(score);
        } else {
            JOptionPane.showMessageDialog(this, "Niveau " + (currentLevel - 1) + " terminé ! Préparez-vous pour le niveau " + currentLevel + " !");
            initGameElementsForLevel(currentLevel);
            gameTimer.start();
        }
        this.requestFocusInWindow();
    }
    
    /**
     * EN: Shows the main menu.
     * FR: Affiche le menu principal.
     */
    private void showMenu() {
        currentGameState = GameState.MENU;
        setMenuUIVisible(true);
        highScoresPanel.setVisible(false);
        endScreenPanel.setVisible(false);
        gamePanel.setVisible(false);
        gameTimer.stop();
        menuTimer.start();
        
        soundManager.loop("menu_music");
        
        glitchTimer.start();

        transitionTo("MAIN");
        this.requestFocusInWindow();
    }
    
    private void transitionTo(String cardName) {
        if (fadeTimer.isRunning() || cardName.equals(targetCard)) return;
        
        soundManager.play("navigate");
        targetCard = cardName;
        isFadingOut = true;
        fadeTimer.start();
    }
    
    /**
     * EN: Sets the visibility of the menu UI components.
     * FR: Définit la visibilité des composants de l'interface utilisateur du menu.
     * @param visible True to show the menu, false to hide it.
     */
    private void setMenuUIVisible(boolean visible) {
        menuBackgroundPanel.setVisible(visible);
        menuContainerPanel.setVisible(visible);
        if(visible) {
            menuTimer.start();
            glitchTimer.start();
        } else {
            menuTimer.stop();
            glitchTimer.stop();
        }
    }
    
    /**
     * EN: Hides the end screen panel.
     * FR: Masque le panneau de l'écran de fin.
     */
    private void hideEndScreen() { endScreenPanel.setVisible(false); }
    
    /**
     * EN: Restarts the game by showing the main menu.
     * FR: Redémarre le jeu en affichant le menu principal.
     */
    private void restartGame() { showMenu(); }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (fadeTimer.isRunning()) {
            Graphics2D g2d = (Graphics2D) layeredPane.getGraphics();
            g2d.setColor(new Color(10, 5, 25, (int)(fadeAlpha * 255)));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
    
    /**
     * EN: The main game panel where the game is rendered.
     * FR: Le panneau de jeu principal où le jeu est rendu.
     */
    private class GamePanel extends JPanel {
        private final Font uiFont = new Font("Orbitron", Font.BOLD, 22);
        public GamePanel() { setOpaque(true); setDoubleBuffered(true); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(currentTheme.bgColor);

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (currentGameState == GameState.PLAYING || currentGameState == GameState.PAUSED) {
                for (Point obstacle : obstacles) {
                    int x = obstacle.x * CELL_SIZE; int y = obstacle.y * CELL_SIZE;
                    g2d.setColor(currentTheme.wallGlow);
                    g2d.fillRect(x - 2, y - 2, CELL_SIZE + 4, CELL_SIZE + 4);
                    g2d.setColor(currentTheme.wallColor);
                    g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                }

                g2d.setColor(new Color(0, 255, 128));
                for (Point dot : dots) g2d.fill(new Ellipse2D.Double(dot.x * CELL_SIZE + CELL_SIZE * 0.4, dot.y * CELL_SIZE + CELL_SIZE * 0.4, CELL_SIZE * 0.2, CELL_SIZE * 0.2));
                for (PowerUp p : powerUps) drawPowerUp(g2d, p);

                int playerX = playerPosition.x * CELL_SIZE; int playerY = playerPosition.y * CELL_SIZE;
                g2d.setColor(Color.YELLOW);
                g2d.fill(new Ellipse2D.Double(playerX + 2, playerY + 2, CELL_SIZE - 4, CELL_SIZE - 4));
                g2d.setColor(Color.WHITE);
                g2d.fill(new Ellipse2D.Double(playerX + 8, playerY + 8, CELL_SIZE - 16, CELL_SIZE - 16));
                
                if(isShieldActive) {
                    g2d.setColor(new Color(0, 255, 255, 100));
                    g2d.setStroke(new BasicStroke(3));
                    g2d.draw(new Ellipse2D.Double(playerX - 2, playerY - 2, CELL_SIZE + 4, CELL_SIZE + 4));
                }

                for (Enemy enemy : enemies) drawEnemy(g2d, enemy);

                g2d.setFont(uiFont);
                g2d.setColor(currentTheme.accentColor);
                g2d.drawString("Score: " + score, 15, 25);
                g2d.drawString("Niveau: " + currentLevel, WIDTH / 2 - 50, 25);
            }
        }
        
        /**
         * EN: Draws a power-up on the screen.
         * FR: Dessine un power-up à l'écran.
         * @param g2d The Graphics2D context.
         * @param powerUp The power-up to draw.
         */
        private void drawPowerUp(Graphics2D g2d, PowerUp powerUp) {
            int x = powerUp.position.x * CELL_SIZE; int y = powerUp.position.y * CELL_SIZE;
            float pulse = (float) (Math.sin((System.currentTimeMillis() - powerUp.spawnTime) / 200.0) + 1.0) / 2.0f;
            switch(powerUp.type) {
                case SUPER_PELLET: g2d.setColor(new Color(255, 255, 0, (int)(155 + 100 * pulse))); g2d.fill(new Ellipse2D.Double(x + CELL_SIZE*0.2, y + CELL_SIZE*0.2, CELL_SIZE*0.6, CELL_SIZE*0.6)); break;
                case FREEZE: g2d.setColor(new Color(0, 150, 255, (int)(155 + 100 * pulse))); g2d.fill(new Rectangle2D.Double(x + CELL_SIZE*0.25, y + CELL_SIZE*0.25, CELL_SIZE*0.5, CELL_SIZE*0.5)); g2d.setColor(Color.WHITE); g2d.draw(new Rectangle2D.Double(x + CELL_SIZE*0.25, y + CELL_SIZE*0.25, CELL_SIZE*0.5, CELL_SIZE*0.5)); break;
                case SHIELD: g2d.setColor(new Color(0, 255, 0, (int)(155 + 100 * pulse))); g2d.setStroke(new BasicStroke(3)); g2d.draw(new Ellipse2D.Double(x + CELL_SIZE*0.2, y + CELL_SIZE*0.2, CELL_SIZE*0.6, CELL_SIZE*0.6)); break;
            }
        }
        
        /**
         * EN: Draws an enemy on the screen.
         * FR: Dessine un ennemi à l'écran.
         * @param g2d The Graphics2D context.
         * @param enemy The enemy to draw.
         */
        private void drawEnemy(Graphics2D g2d, Enemy enemy) {
            int ex = enemy.position.x * CELL_SIZE; int ey = enemy.position.y * CELL_SIZE;
            Color bodyColor;

            switch(enemy.state) {
                case FLEEING: bodyColor = new Color(0, 100, 255, 150); break;
                case AGGRO_TELEGRAPH:
                    bodyColor = (System.currentTimeMillis() / 100) % 2 == 0 ? Color.WHITE : getEnemyBaseColor(enemy.behavior);
                    break;
                case CHASING:
                    bodyColor = getEnemyBaseColor(enemy.behavior).brighter();
                    g2d.setColor(new Color(255, 0, 0, 100));
                    g2d.fill(new Ellipse2D.Double(ex, ey, CELL_SIZE, CELL_SIZE));
                    break;
                case PATROLLING: default:
                    bodyColor = getEnemyBaseColor(enemy.behavior);
                    break;
            }
            
            g2d.setColor(bodyColor);
            g2d.fill(new Rectangle2D.Double(ex + 4, ey + 4, CELL_SIZE - 8, CELL_SIZE - 8));
        }
        
        /**
         * EN: Gets the base color for an enemy based on its behavior.
         * FR: Obtient la couleur de base d'un ennemi en fonction de son comportement.
         * @param behavior The enemy's behavior.
         * @return The base color of the enemy.
         */
        private Color getEnemyBaseColor(EnemyBehavior behavior) {
            switch(behavior) {
                case HUNTER: return new Color(255, 0, 0);
                case AMBUSHER: return new Color(255, 105, 180);
                case FLANKER: return new Color(255, 165, 0);
                case ROAMER: default: return new Color(128, 0, 128);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (currentGameState == GameState.PLAYING) {
            Point newPlayerPos = (Point) playerPosition.clone();
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP: newPlayerPos.y--; break;
                case KeyEvent.VK_DOWN: newPlayerPos.y++; break;
                case KeyEvent.VK_LEFT: newPlayerPos.x--; break;
                case KeyEvent.VK_RIGHT: newPlayerPos.x++; break;
                default: return;
            }
            if (isWalkable(newPlayerPos)) {
                playerPosition.setLocation(newPlayerPos);
                if (dots.removeIf(dot -> dot.equals(playerPosition))) score += 10;
                powerUps.removeIf(p -> {
                    if (p.position.equals(playerPosition)) { activatePowerUp(p.type); return true; }
                    return false;
                });
            }
        }
    }
    
    /**
     * EN: Activates a power-up when the player collects it.
     * FR: Active un power-up lorsque le joueur le récupère.
     * @param type The type of power-up to activate.
     */
    private void activatePowerUp(PowerUpType type) {
        long currentTime = System.currentTimeMillis();
        playerProfile.powerupsCollected++;
        switch(type) {
            case SHIELD: isShieldActive = true; shieldEndTime = currentTime + 5000; break;
            case FREEZE: areEnemiesFrozen = true; freezeEndTime = currentTime + 3000; break;
            case SUPER_PELLET:
                areEnemiesVulnerable = true;
                vulnerableEndTime = currentTime + 8000;
                for(Enemy e : enemies) e.changeState(EnemyState.FLEEING);
                score += 50;
                break;
        }
    }

    /**
     * EN: Checks if a given point on the grid is walkable.
     * FR: Vérifie si un point donné sur la grille est praticable.
     * @param p The point to check.
     * @return True if the point is walkable, false otherwise.
     */
    private boolean isWalkable(Point p) {
        return p.x >= 0 && p.y >= 0 && p.x < gridCols && p.y < gridRows && !obstacles.contains(p);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentGameState == GameState.PLAYING) {
            updateGameLogic();
            if(!areEnemiesFrozen) moveEnemies();
            checkEnemyCollision(); checkWinCondition();
            gamePanel.repaint();
        }
    }

    /**
     * EN: Checks if the win condition (all dots collected) has been met.
     * FR: Vérifie si la condition de victoire (tous les points collectés) est remplie.
     */
    private void checkWinCondition() {
        if (currentGameState == GameState.PLAYING && dots.isEmpty()) gameWinLevel();
    }
    
    /**
     * EN: Moves the enemies based on their behavior and state.
     * FR: Déplace les ennemis en fonction de leur comportement et de leur état.
     */
    private void moveEnemies() {
        for (Enemy enemy : enemies) {
            discoveredEnemies.add(enemy.behavior);
            
            if(enemy.state == EnemyState.PATROLLING && enemy.position.distance(playerPosition) < 6) {
                enemy.changeState(EnemyState.AGGRO_TELEGRAPH);
            } else if (enemy.state == EnemyState.CHASING && enemy.position.distance(playerPosition) > 10) {
                 enemy.changeState(EnemyState.PATROLLING);
            }
            
            if(enemy.state == EnemyState.FLEEING) {
                moveEnemyAway(enemy);
            } else if (enemy.state == EnemyState.CHASING) {
                enemy.pathRecalculationCounter++;
                boolean shouldRecalculate = true;
                if(currentLevel < 5) {
                    if(enemy.pathRecalculationCounter % (random.nextInt(3)+1) != 0) {
                        shouldRecalculate = false;
                    }
                }
                if(shouldRecalculate) {
                    Point target = getTargetForEnemy(enemy);
                    List<Point> path = findPath(enemy.position, target);
                    if (path != null && path.size() > 1) enemy.position.setLocation(path.get(1));
                }
            }
        }
    }
    
    /**
     * EN: Moves an enemy away from the player (when fleeing).
     * FR: Éloigne un ennemi du joueur (lorsqu'il fuit).
     * @param enemy The enemy to move.
     */
    private void moveEnemyAway(Enemy enemy) {
        int bestDist = -1;
        Point bestMove = enemy.position;
        int[] dx = {0, 0, 1, -1, 0}; int[] dy = {1, -1, 0, 0, 0};
        
        for (int i = 0; i < 5; i++) {
            Point next = new Point(enemy.position.x + dx[i], enemy.position.y + dy[i]);
            if(isWalkable(next)) {
                double dist = next.distanceSq(playerPosition);
                if(dist > bestDist) {
                    bestDist = (int) dist;
                    bestMove = next;
                }
            }
        }
        enemy.position.setLocation(bestMove);
    }

    /**
     * EN: Determines the target position for an enemy based on its behavior.
     * FR: Détermine la position cible d'un ennemi en fonction de son comportement.
     * @param enemy The enemy to determine the target for.
     * @return The target point.
     */
    private Point getTargetForEnemy(Enemy enemy) {
        switch(enemy.behavior) {
            case HUNTER: return playerPosition;
            case AMBUSHER: return new Point(playerPosition.x + random.nextInt(5)-2, playerPosition.y + random.nextInt(5)-2);
            case ROAMER:
                if(enemy.position.distance(playerPosition) > 8) return playerPosition;
                else return new Point(1,1);
            case FLANKER:
                 if(enemies.size() > 0) {
                     Point hunterPos = enemies.get(0).position;
                     return new Point(hunterPos.x + (hunterPos.x - playerPosition.x), hunterPos.y + (hunterPos.y - playerPosition.y));
                 }
                 return playerPosition;
            default: return playerPosition;
        }
    }

    /**
     * EN: Finds a path from a start point to a target point using Breadth-First Search (BFS).
     * FR: Trouve un chemin d'un point de départ à un point cible en utilisant une recherche en largeur (BFS).
     * @param start The starting point.
     * @param target The target point.
     * @return A list of points representing the path, or null if no path is found.
     */
    private List<Point> findPath(Point start, Point target) {
        Queue<List<Point>> queue = new LinkedList<>();
        Set<Point> visited = new HashSet<>();
        List<Point> initialPath = new ArrayList<>();
        initialPath.add(start);
        queue.add(initialPath);
        visited.add(start);

        while (!queue.isEmpty()) {
            List<Point> currentPath = queue.poll();
            Point current = currentPath.get(currentPath.size() - 1);
            if (current.equals(target)) return currentPath;

            int[] dx = {0, 0, 1, -1}; int[] dy = {1, -1, 0, 0};
            for (int i = 0; i < 4; i++) {
                Point next = new Point(current.x + dx[i], current.y + dy[i]);
                if (isWalkable(next) && !visited.contains(next)) {
                    visited.add(next);
                    List<Point> newPath = new ArrayList<>(currentPath);
                    newPath.add(next);
                    queue.add(newPath);
                }
            }
        }
        return null;
    }

    /**
     * EN: Checks for collisions between the player and enemies.
     * FR: Vérifie les collisions entre le joueur et les ennemis.
     */
    private void checkEnemyCollision() {
        if (currentGameState != GameState.PLAYING) return;
        for (Iterator<Enemy> iterator = enemies.iterator(); iterator.hasNext();) {
            Enemy enemy = iterator.next();
            if (enemy.position.equals(playerPosition)) {
                if (enemy.state == EnemyState.FLEEING) {
                    score += 200;
                    playerProfile.enemiesDefeated++;
                    iterator.remove(); 
                } else if (isShieldActive) {
                    isShieldActive = false;
                    enemy.position.setLocation(1, gridRows - 2);
                } else {
                    gameOver(); return;
                }
            }
        }
    }
    
    /**
     * EN: Represents a high score entry, with a name and a score.
     * FR: Représente une entrée de meilleur score, avec un nom et un score.
     */
    private static class HighScoreEntry implements Serializable, Comparable<HighScoreEntry> {
        private static final long serialVersionUID = 2L; String name; int score;
        public HighScoreEntry(String name, int score) { this.name = name; this.score = score; }
        @Override public String toString() { return String.format("%-15s %d", name, score); }
        @Override public int compareTo(HighScoreEntry other) { return Integer.compare(other.score, this.score); }
    }

    /**
     * EN: Loads high scores from a file.
     * FR: Charge les meilleurs scores depuis un fichier.
     */
    @SuppressWarnings("unchecked")
    private void loadHighScores() {
        File file = new File(HIGHSCORE_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) { highScores = (List<HighScoreEntry>) ois.readObject(); } catch (Exception e) { highScores = new ArrayList<>(); }
        }
    }

    /**
     * EN: Saves the current high scores to a file.
     * FR: Sauvegarde les meilleurs scores actuels dans un fichier.
     */
    private void saveHighScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(HIGHSCORE_FILE))) { oos.writeObject(highScores); } catch (IOException e) { /* Gérer l'erreur */ }
    }
    
    /**
     * EN: Loads the player's profile from a file.
     * FR: Charge le profil du joueur depuis un fichier.
     */
     private void loadProfile() {
        File file = new File(PROFILE_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) { playerProfile = (PlayerProfile) ois.readObject(); } catch (Exception e) { playerProfile = new PlayerProfile(); }
        } else { playerProfile = new PlayerProfile(); }
    }

    /**
     * EN: Saves the current player's profile to a file.
     * FR: Sauvegarde le profil du joueur actuel dans un fichier.
     */
    private void saveProfile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PROFILE_FILE))) { oos.writeObject(playerProfile); } catch (IOException e) { /* Gérer l'erreur */ }
    }
    
    /**
     * EN: Asks the player for their name and adds their score to the high scores list if applicable.
     * FR: Demande au joueur son nom et ajoute son score à la liste des meilleurs scores si applicable.
     * @param finalScore The player's final score.
     */
    private void askForNameAndAddHighScore(int finalScore) {
        playerProfile.totalScore += finalScore;
        if (highScores.size() < MAX_HIGHSCORES || highScores.isEmpty() || finalScore > highScores.get(highScores.size() - 1).score) {
            String name = JOptionPane.showInputDialog(this, "Nouveau meilleur score ! Entrez votre nom:", "Meilleur Score", JOptionPane.PLAIN_MESSAGE);
            if (name == null || name.trim().isEmpty()) name = "Anonyme";
            addHighScore(new HighScoreEntry(name.trim(), finalScore));
        }
    }

    /**
     * EN: Adds a new high score entry to the list, sorts the list, and saves it.
     * FR: Ajoute une nouvelle entrée de meilleur score à la liste, trie la liste et la sauvegarde.
     * @param entry The high score entry to add.
     */
    private void addHighScore(HighScoreEntry entry) {
        highScores.add(entry);
        Collections.sort(highScores);
        while (highScores.size() > MAX_HIGHSCORES) highScores.remove(highScores.size() - 1);
        saveHighScores();
    }
    
    /**
     * EN: Displays the high scores screen.
     * FR: Affiche l'écran des meilleurs scores.
     */
    private void showHighScores() {
        currentGameState = GameState.HIGHSCORES;
        setMenuUIVisible(false);
        highScoresPanel.setVisible(true);
        StringBuilder sb = new StringBuilder();
        if (highScores.isEmpty()) {
            sb.append("\n\n   AUCUN SCORE ENREGISTRÉ...");
        } else {
            for (int i = 0; i < highScores.size(); i++) {
                 sb.append(String.format(" %d. %s%n", (i + 1), highScores.get(i).toString()));
            }
        }
        highScoresDisplay.setText(sb.toString());
        highScoresDisplay.setCaretPosition(0);
        
        creditsPanel.setVisible(false);
        
        this.requestFocusInWindow();
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    /**
     * EN: A custom animated button for the menu.
     * FR: Un bouton animé personnalisé pour le menu.
     */
    private class AnimatedButton extends JButton {
        private float hoverAnimation = 0f; private Timer animationTimer;
        public AnimatedButton(String text) { this(text, null); }
        public AnimatedButton(String text, ActionListener listener) {
            super(text);
            if (listener != null) {
                addActionListener(e -> {
                    soundManager.play("click");
                    listener.actionPerformed(e);
                });
            }
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setForeground(Color.CYAN); setFont(new Font("Orbitron", Font.BOLD, 24));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            animationTimer = new Timer(15, e -> {
                if (getModel().isRollover()) hoverAnimation = Math.min(1f, hoverAnimation + 0.1f);
                else hoverAnimation = Math.max(0f, hoverAnimation - 0.1f);
                repaint();
                if (hoverAnimation == 0f || hoverAnimation == 1f) ((Timer)e.getSource()).stop();
            });
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) { 
                    soundManager.play("hover");
                    if (!animationTimer.isRunning()) animationTimer.start(); 
                }
                public void mouseExited(java.awt.event.MouseEvent evt) { if (!animationTimer.isRunning()) animationTimer.start(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            int baseWidth = 220;
            int baseHeight = 30;

            // Halo lumineux de survol
            if (hoverAnimation > 0) {
                int glowSize = (int) (12 * hoverAnimation);
                g2d.setColor(new Color(0, 255, 255, (int) (70 * hoverAnimation)));
                g2d.fillRoundRect(width / 2 - (baseWidth + glowSize) / 2, height / 2 - (baseHeight + glowSize) / 2, baseWidth + glowSize, baseHeight + glowSize, 30, 30);
            }
            
            // Contour du bouton
            g2d.setColor(Color.CYAN);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawRoundRect(width/2 - baseWidth/2, height/2 - baseHeight/2, baseWidth, baseHeight, 20, 20);

            // Changer la couleur du texte au survol
            if(hoverAnimation > 0.5f){
                setForeground(Color.WHITE);
            } else {
                setForeground(Color.CYAN);
            }
            
            super.paintComponent(g);
            g2d.dispose();
        }
        @Override public Dimension getPreferredSize() { return new Dimension(300, 50); }
        @Override public Dimension getMaximumSize() { return getPreferredSize(); }
    }

    /**
     * EN: A panel for displaying an animated background in the menu.
     * FR: Un panneau pour afficher un arrière-plan animé dans le menu.
     */
    private class MenuBackgroundPanel extends JPanel {
        private final List<GridPoint> gridPoints = new ArrayList<>();
        private final List<DataShard> dataShards = new ArrayList<>();
        private final List<GlitchSquare> glitchSquares = new ArrayList<>();
        private final List<PulseWave> pulseWaves = new ArrayList<>();
        private long lastPulseTime = 0;
        
        private class GridPoint {
            float x, y, brightness;
            double angle;
            GridPoint(float x, float y) {
                this.x = x; this.y = y;
                this.angle = random.nextDouble() * 2 * Math.PI;
            }
            void update() { 
                angle += 0.02;
                brightness = (float)(Math.sin(angle) + 1) / 2f; 
            }
        }

        private class DataShard {
            float x, y, speed, length;
            int alpha;
            DataShard() { reset(); this.y = random.nextInt(HEIGHT); }
            void update() {
                x += speed; y += speed;
                if (x > WIDTH || y > HEIGHT) reset();
            }
            void reset() {
                x = -50; y = random.nextInt(HEIGHT);
                speed = 2 + random.nextFloat() * 4;
                length = 15 + random.nextFloat() * 50;
                alpha = 60 + random.nextInt(100);
            }
        }
        
        private class GlitchSquare {
            int x, y, size, life;
            Color color;
            GlitchSquare() {
                x = random.nextInt(WIDTH); y = random.nextInt(HEIGHT);
                size = 5 + random.nextInt(15);
                life = 5 + random.nextInt(15);
                color = random.nextBoolean() ? new Color(0, 255, 255) : new Color(255, 0, 255);
            }
            boolean update() { life--; return life > 0; }
        }
        
        private class PulseWave {
            float radius = 0;
            float maxRadius = 1200;
            float speed = 3f;
            float alpha = 1.0f;

            boolean update() {
                radius += speed;
                if (radius > maxRadius) return false;
                alpha = 1.0f - (radius / maxRadius);
                return true;
            }
        }

        public MenuBackgroundPanel() {
            int gridSpacing = 40;
            for (int i = 0; i < WIDTH; i += gridSpacing) {
                for (int j = 0; j < HEIGHT; j += gridSpacing) {
                    gridPoints.add(new GridPoint(i, j));
                }
            }
            for (int i = 0; i < 30; i++) dataShards.add(new DataShard());
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // Fond fixe
            g2d.setColor(new Color(10, 5, 25));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            if (!backgroundAnimationEnabled) return;
            
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Fond en dégradé radial pulsant
            float pulse = (float)(Math.sin(System.currentTimeMillis() * 0.0002) + 1) / 2f;
            Point2D center = new Point2D.Float(WIDTH / 2f, HEIGHT / 2f);
            float radius = 400 + pulse * 200;
            float[] dist = {0.0f, 1.0f};
            Color[] colors = {new Color(100, 80, 220, 100), new Color(10, 5, 25, 150)};
            RadialGradientPaint rgp = new RadialGradientPaint(center, radius, dist, colors);
            g2d.setPaint(rgp);
            g2d.fillRect(0, 0, WIDTH, HEIGHT);

            // 1. Grille de données connectée
            for (GridPoint p : gridPoints) {
                p.update();
                g2d.setColor(new Color(180, 200, 255, (int)(p.brightness * 70)));
                g2d.fill(new Ellipse2D.Float(p.x-1, p.y-1, 3, 3));
                
                // Dessiner des connexions aléatoires
                if (random.nextInt(1000) < 5) {
                    GridPoint p2 = gridPoints.get(random.nextInt(gridPoints.size()));
                    g2d.setStroke(new BasicStroke(0.5f));
                    g2d.setColor(new Color(150, 180, 255, 30));
                    g2d.drawLine((int)p.x, (int)p.y, (int)p2.x, (int)p2.y);
                }
            }

            // 2. Ondes de choc
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastPulseTime > 3000) {
                pulseWaves.add(new PulseWave());
                lastPulseTime = currentTime;
            }
            
            Iterator<PulseWave> waveIterator = pulseWaves.iterator();
            while(waveIterator.hasNext()){
                PulseWave wave = waveIterator.next();
                if(wave.update()){
                    g2d.setColor(new Color(200, 220, 255, (int)(wave.alpha * 50)));
                    g2d.setStroke(new BasicStroke(2f));
                    g2d.draw(new Ellipse2D.Float(WIDTH/2f - wave.radius, HEIGHT/2f - wave.radius, wave.radius*2, wave.radius*2));
                } else {
                    waveIterator.remove();
                }
            }
            
            // 3. Particules cosmiques (anciennement DataShards)
            for (DataShard shard : dataShards) {
                shard.update();
                GradientPaint gp = new GradientPaint(shard.x, shard.y, new Color(220, 240, 255, (int)(shard.alpha * 0.8)), 
                                                     shard.x - shard.length, shard.y - shard.length, new Color(150, 180, 255, 0));
                g2d.setPaint(gp);
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawLine((int)shard.x, (int)shard.y, (int)(shard.x - shard.length), (int)(shard.y - shard.length));
            }
            
            // 4. Carrés "glitch"
            if (random.nextInt(10) < 2) {
                glitchSquares.add(new GlitchSquare());
            }
            
            Iterator<GlitchSquare> iterator = glitchSquares.iterator();
            while(iterator.hasNext()) {
                GlitchSquare sq = iterator.next();
                if (sq.update()) {
                    g2d.setColor(new Color(sq.color.getRed(), sq.color.getGreen(), sq.color.getBlue(), random.nextInt(150) + 50));
                    g2d.fillRect(sq.x, sq.y, sq.size, sq.size);
                } else {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * EN: A simple sound manager class.
     * FR: Classe de gestion simplifiée du son.
     */
    private class SoundManager {
        private Clip menuMusic, hoverSound, clickSound, navigateSound;

        public SoundManager() {
            // Assurez-vous que les fichiers .wav sont dans un dossier "sounds" à la racine de votre classpath.
            menuMusic = loadClip("/sounds/menu_music.wav");
            hoverSound = loadClip("/sounds/hover.wav");
            clickSound = loadClip("/sounds/click.wav");
            navigateSound = loadClip("/sounds/navigate.wav");
        }

        private Clip loadClip(String path) {
            try {
                URL url = this.getClass().getResource(path);
                if (url == null) {
                    System.err.println("Can't find sound file: " + path);
                    return null;
                }
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                return clip;
            } catch (Exception e) {
                System.err.println("Error loading sound: " + path);
                return null;
            }
        }

        public void play(String soundName) {
            Clip clip = getClip(soundName);
            if (clip != null) {
                if (clip.isRunning()) clip.stop();
                clip.setFramePosition(0);
                clip.start();
            }
        }

        public void loop(String soundName) {
            Clip clip = getClip(soundName);
            if (clip != null && !clip.isRunning()) {
                clip.setFramePosition(0);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        }

        public void stop(String soundName) {
            Clip clip = getClip(soundName);
            if (clip != null && clip.isRunning()) {
                clip.stop();
            }
        }

        private Clip getClip(String name) {
            switch (name) {
                case "menu_music": return menuMusic;
                case "hover": return hoverSound;
                case "click": return clickSound;
                case "navigate": return navigateSound;
                default: return null;
            }
        }
    }


    /**
     * EN: The main entry point for the application.
     * FR: Le point d'entrée principal de l'application.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PacManGame game = new PacManGame();
            game.setVisible(true);
        });
    }
}