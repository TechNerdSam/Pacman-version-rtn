import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;
import java.util.List;
import javax.swing.Timer;
import java.io.*;

public class PacManGame extends JFrame implements KeyListener, ActionListener {

    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final int CELL_SIZE = 30;
    private static final String HIGHSCORE_FILE = "highscores.dat";
    private static final int MAX_HIGHSCORES = 5;

    private enum GameState {
        MENU, PLAYING, PAUSED, GAME_OVER, WIN, HIGHSCORES
    }

    private GameState currentGameState;

    // Game elements
    private Point playerPosition;
    private List<Point> dots;
    private Set<Point> obstacles;
    private List<Point> enemies;
    private int score;
    private Timer gameTimer;

    private Random random;

    private int currentLevel;
    private int maxLevel = 20; // MODIFIED: Initialise maxLevel à 20 ici
    private int targetScore; 
    private int initialDotCount; 

    // Grid dimensions
    private int gridCols;
    private int gridRows;

    // UI Components
    private JLayeredPane layeredPane;
    private GamePanel gamePanel;
    private JPanel menuPanel;
    private JPanel pausePanel;
    private JPanel endScreenPanel;
    private JPanel highScoresPanel;
    private JLabel endMessageLabel;
    private JTextArea highScoresDisplay;

    // High Scores
    private List<HighScoreEntry> highScores;

    // Constructor
    public PacManGame() {
        setTitle("Le Labyrinthe de Cubeman"); 
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        random = new Random();

        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        getContentPane().add(layeredPane, BorderLayout.CENTER);

        gamePanel = new GamePanel();
        gamePanel.setBounds(0, 0, WIDTH, HEIGHT);
        layeredPane.add(gamePanel, JLayeredPane.DEFAULT_LAYER);

        initUIComponents(); 

        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        initGame(); 
        
        loadHighScores();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                saveHighScores(); 
            }
        });
        
        showMenu();
    }

    private void initUIComponents() {
        // Menu Panel
        menuPanel = createPanel(BoxLayout.Y_AXIS, new Color(20, 20, 20), new Insets(100, 0, 0, 0)); 
        menuPanel.add(createLabel("Le Labyrinthe de Cubeman", Color.YELLOW, new Font("Arial", Font.BOLD, 28), Component.CENTER_ALIGNMENT)); 
        menuPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        menuPanel.add(createStyledButton("Nouvelle Partie (Choisir Niveau)", e -> chooseStartingLevel(), 250, 50, new Color(50, 50, 50), Color.WHITE, new Color(0, 180, 180), 2)); 
        menuPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        menuPanel.add(createStyledButton("Meilleurs Scores", e -> showHighScores(), 250, 50, new Color(50, 50, 50), Color.WHITE, new Color(0, 180, 180), 2)); 
        menuPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        menuPanel.add(createStyledButton("Instructions", e -> showInstructions(), 250, 50, new Color(50, 50, 50), Color.WHITE, new Color(0, 180, 180), 2)); 
        menuPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        menuPanel.add(createStyledButton("Crédits", e -> showCredits(), 250, 50, new Color(50, 50, 50), Color.WHITE, new Color(0, 180, 180), 2)); 
        menuPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        menuPanel.add(createStyledButton("Quitter", e -> System.exit(0), 250, 50, new Color(50, 50, 50), Color.WHITE, new Color(0, 180, 180), 2)); 
        menuPanel.add(Box.createRigidArea(new Dimension(0, 15))); 
        layeredPane.add(menuPanel, JLayeredPane.PALETTE_LAYER);

        // Pause Panel
        pausePanel = createPanel(BoxLayout.Y_AXIS, new Color(0, 0, 0, 150), new Insets(150, 0, 0, 0));
        pausePanel.setVisible(false);
        pausePanel.add(createLabel("PAUSE", Color.WHITE, new Font("Arial", Font.BOLD, 40), Component.CENTER_ALIGNMENT)); 
        pausePanel.add(Box.createRigidArea(new Dimension(0, 40)));

        pausePanel.add(createStyledButton("Reprendre", e -> resumeGame(), 200, 40, new Color(50, 50, 50), Color.WHITE, new Color(0, 180, 180), 1)); 
        pausePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        pausePanel.add(createStyledButton("Redémarrer", e -> { restartGame(); hidePauseScreen(); }, 200, 40, new Color(50, 50, 50), Color.WHITE, new Color(0, 180, 180), 1)); 
        pausePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        pausePanel.add(createStyledButton("Quitter au Menu", e -> showMenu(), 200, 40, new Color(50, 50, 50), Color.WHITE, new Color(0, 180, 180), 1)); 
        pausePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        layeredPane.add(pausePanel, JLayeredPane.MODAL_LAYER);

        // End Screen Panel
        endScreenPanel = createPanel(BoxLayout.Y_AXIS, new Color(0, 0, 0, 200), new Insets(150, 0, 0, 0));
        endScreenPanel.setVisible(false);
        endMessageLabel = createLabel("", Color.WHITE, new Font("Arial", Font.BOLD, 36), Component.CENTER_ALIGNMENT); 
        endScreenPanel.add(endMessageLabel);
        endScreenPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        endScreenPanel.add(createStyledButton("Rejouer", e -> { restartGame(); hideEndScreen(); }, 200, 40, new Color(50, 50, 50), Color.WHITE, new Color(0, 180, 180), 1)); 
        endScreenPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        endScreenPanel.add(createStyledButton("Quitter au Menu", e -> showMenu(), 200, 40, new Color(50, 50, 50), Color.WHITE, new Color(0, 180, 180), 1)); 
        endScreenPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        layeredPane.add(endScreenPanel, JLayeredPane.MODAL_LAYER);

        // High Scores Panel
        highScoresPanel = createPanel(BoxLayout.Y_AXIS, new Color(0, 0, 0, 200), new Insets(50, 0, 0, 0));
        highScoresPanel.setVisible(false);
        highScoresPanel.add(createLabel("MEILLEURS SCORES", Color.YELLOW, new Font("Arial", Font.BOLD, 30), Component.CENTER_ALIGNMENT));
        highScoresPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        highScoresDisplay = new JTextArea(10, 20);
        highScoresDisplay.setEditable(false);
        highScoresDisplay.setBackground(Color.BLACK);
        highScoresDisplay.setForeground(Color.WHITE);
        highScoresDisplay.setFont(new Font("Monospaced", Font.PLAIN, 20));
        highScoresDisplay.setMargin(new Insets(10, 50, 10, 50));
        JScrollPane scrollPane = new JScrollPane(highScoresDisplay);
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        scrollPane.setMaximumSize(new Dimension(400, 300));
        highScoresPanel.add(scrollPane);
        highScoresPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        highScoresPanel.add(createStyledButton("Retour au Menu", e -> showMenu(), 200, 40, new Color(50, 50, 50), Color.WHITE, new Color(0, 180, 180), 1)); 
        layeredPane.add(highScoresPanel, JLayeredPane.MODAL_LAYER);
    }

    private JPanel createPanel(int layoutAxis, Color bgColor, Insets borderInsets) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, layoutAxis));
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createEmptyBorder(borderInsets.top, borderInsets.left, borderInsets.bottom, borderInsets.right));
        panel.setBounds(0, 0, WIDTH, HEIGHT);
        return panel;
    }

    private JLabel createLabel(String text, Color fgColor, Font font, float alignmentX) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(alignmentX);
        label.setForeground(fgColor);
        label.setFont(font);
        return label;
    }

    private JButton createStyledButton(String text, ActionListener listener, int maxWidth, int maxHeight, Color bgColor, Color fgColor, Color borderColor, int borderWidth) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(maxWidth, maxHeight));
        button.setFont(new Font("Arial", Font.BOLD, 20)); 
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.addActionListener(listener);
        button.setBorder(BorderFactory.createLineBorder(borderColor, borderWidth));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }

    private void initGame() {
        currentGameState = GameState.MENU;
        score = 0;
        playerPosition = new Point(1, 1);

        dots = new ArrayList<>();
        obstacles = new HashSet<>();
        enemies = new ArrayList<>();
        highScores = new ArrayList<>();

        gridCols = WIDTH / CELL_SIZE;
        gridRows = HEIGHT / CELL_SIZE;

        gameTimer = new Timer(50, this); 
        gameTimer.stop();
    }

    private void createMazeAndDotsForLevel(int level) {
        dots.clear();
        obstacles.clear();

        int[][] mazeData;
        
        // Définition des motifs de labyrinthe
        if (level % 3 == 1) { 
            mazeData = new int[][]{
                    {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, 
                    {1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1}, 
                    {1,2,1,1,1,2,1,2,1,2,1,2,1,2,1,2,1,1,2,1}, 
                    {1,2,1,2,2,2,1,2,1,2,1,2,1,2,1,2,1,2,2,1}, 
                    {1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,1,2,1}, 
                    {1,2,2,2,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1}, 
                    {1,1,1,2,1,1,1,2,1,1,1,2,1,1,1,2,1,1,1,1}, 
                    {1,2,2,2,2,2,1,2,2,2,2,2,1,2,2,2,2,2,2,1}, 
                    {1,2,1,1,1,2,1,2,1,1,1,2,1,2,1,1,1,2,1,1}, 
                    {1,2,1,2,1,2,2,2,2,2,1,2,2,2,1,2,1,2,2,1}, 
                    {1,2,1,2,1,1,1,1,1,2,1,2,1,2,1,1,1,1,2,1}, 
                    {1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1}, 
                    {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, 
                    {1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1}, 
                    {1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,1}, 
                    {1,2,1,2,2,2,1,2,1,2,1,2,1,2,1,2,1,2,2,1}, 
                    {1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,1,2,1}, 
                    {1,2,2,2,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1}, 
                    {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}  
            };
        } else if (level % 3 == 2) {
            mazeData = new int[][]{
                    {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, 
                    {1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1}, 
                    {1,2,1,1,2,1,1,1,1,1,1,1,1,1,1,1,2,1,2,1}, 
                    {1,2,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,2,1}, 
                    {1,2,1,2,1,1,1,1,1,1,1,1,1,1,1,1,2,1,2,1}, 
                    {1,2,2,2,1,2,2,2,2,2,2,2,2,2,2,1,2,2,2,1}, 
                    {1,1,1,2,1,2,1,1,1,1,1,1,1,1,2,1,2,1,1,1}, 
                    {1,2,2,2,2,2,1,2,2,2,2,2,2,1,2,2,2,2,2,1}, 
                    {1,2,1,1,1,1,1,2,1,1,1,1,2,1,1,1,1,1,2,1}, 
                    {1,2,2,2,2,2,1,2,2,2,2,1,2,1,2,2,2,2,2,1}, 
                    {1,2,1,1,1,1,1,2,1,1,1,1,2,1,1,1,1,1,2,1}, 
                    {1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1}, 
                    {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, 
                    {1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1}, 
                    {1,2,1,1,2,1,1,1,1,1,1,1,1,1,1,1,2,1,2,1}, 
                    {1,2,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,2,1}, 
                    {1,2,1,2,1,1,1,1,1,1,1,1,1,1,1,1,2,1,2,1}, 
                    {1,2,2,2,1,2,2,2,2,2,2,2,2,2,2,1,2,2,2,1}, 
                    {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}  
            };
        } else { // Level 3, 6, 9...
            mazeData = new int[][]{
                    {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, 
                    {1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1}, 
                    {1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,1}, 
                    {1,2,1,2,2,2,1,2,1,2,1,2,1,2,1,2,1,2,2,1}, 
                    {1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,1,2,1}, 
                    {1,2,2,2,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1}, 
                    {1,1,1,2,1,1,1,2,1,1,1,2,1,1,1,2,1,1,1,1}, 
                    {1,2,2,2,2,2,1,2,2,2,2,2,1,2,2,2,2,2,2,1}, 
                    {1,2,1,1,1,2,1,2,1,1,1,2,1,2,1,1,1,2,1,1}, 
                    {1,2,1,2,1,2,2,2,2,2,1,2,2,2,1,2,1,2,2,1}, 
                    {1,2,1,2,1,1,1,1,1,2,1,2,1,2,1,1,1,1,2,1}, 
                    {1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1}, 
                    {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, 
                    {1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1}, 
                    {1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,1}, 
                    {1,2,1,2,2,2,1,2,1,2,1,2,1,2,1,2,1,2,2,1}, 
                    {1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,1,2,1}, 
                    {1,2,2,2,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1}, 
                    {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}  
            };
        }
        
        // --- Common Maze Modifications (déplacé en dehors des blocs if/else pour minimiser les lignes) ---
        // Ouvre la zone autour de (8,10) pour tous les niveaux
        mazeData[8][10] = 2; mazeData[9][10] = 2; mazeData[8][9] = 2; mazeData[8][11] = 2;
        
        // Ouvre une voie vers le labyrinthe du bas (Row 12, Cols 9 et 10) pour TOUS les niveaux
        mazeData[12][9] = 2;
        mazeData[12][10] = 2;

        // Ouvre passage à (14,16), (14,17), (14,18) pour l'accessibilité à droite pour TOUS les niveaux
        mazeData[14][16] = 2;
        mazeData[14][17] = 2;
        mazeData[14][18] = 2;

        // Ouvre des voies multiples autour de (16,17) (assumé "mur en forme de 6") pour TOUS les niveaux
        mazeData[16][17] = 2; 
        mazeData[15][17] = 2; 
        mazeData[17][17] = 2; 
        mazeData[16][16] = 2; 
        mazeData[16][18] = 2; 
        // --- Fin des modifications communes ---

        gridCols = mazeData[0].length;
        gridRows = mazeData.length;

        // Populate obstacles and dots lists
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                Point p = new Point(col, row); 
                if (mazeData[row][col] == 1) {
                    obstacles.add(p);
                } else if (mazeData[row][col] == 2) {
                    dots.add(p);
                }
            }
        }
    }

    private void placeEnemies() {
        enemies.clear();
        int numEnemies = 4; // Toujours 4 ennemis

        List<Point> availablePositions = new ArrayList<>();
        for (int r = 0; r < gridRows; r++) {
            for (int c = 0; c < gridCols; c++) {
                Point p = new Point(c, r);
                // La position ne doit pas être un obstacle ou la position du joueur.
                // Les ennemis peuvent désormais apparaître sur les points.
                if (!obstacles.contains(p) && !p.equals(playerPosition)) {
                    availablePositions.add(p);
                }
            }
        }
        
        Collections.shuffle(availablePositions, random); 

        for (int i = 0; i < numEnemies && i < availablePositions.size(); i++) {
            enemies.add(availablePositions.get(i));
        }

        // --- DEBUGGING OUTPUT ---
        System.out.println("DEBUG: Niveau " + currentLevel + " - Ennemis demandés: " + numEnemies);
        System.out.println("DEBUG: Positions disponibles pour ennemis (non murs/joueur): " + availablePositions.size());
        System.out.println("DEBUG: Ennemis finalement placés:");
        if (enemies.isEmpty()) {
            System.out.println("  AUCUN (Labyrinthe trop dense ou pas assez de places valides trouvées)");
        }
        for (Point enemy : enemies) {
            System.out.println("  Ennemi à: (" + enemy.x + ", " + enemy.y + ")");
        }
        // --- END DEBUGGING ---
    }

    private void showMenu() {
        currentGameState = GameState.MENU;
        setPanelVisibility(menuPanel, true);
        setPanelVisibility(pausePanel, false);
        setPanelVisibility(endScreenPanel, false);
        setPanelVisibility(highScoresPanel, false);
        gamePanel.setVisible(false);
        gameTimer.stop();
        this.requestFocusInWindow();
    }

    private void setPanelVisibility(JPanel panel, boolean visible) {
        panel.setVisible(visible);
    }

    private void chooseStartingLevel() {
        String levelInput = JOptionPane.showInputDialog(this, "Entrez le niveau de départ (1 à " + maxLevel + ") :", "Choisir Niveau", JOptionPane.PLAIN_MESSAGE); 
        if (levelInput != null && !levelInput.trim().isEmpty()) {
            try {
                int chosenLevel = Integer.parseInt(levelInput.trim());
                if (chosenLevel >= 1 && chosenLevel <= maxLevel) { 
                    currentLevel = chosenLevel;
                    // maxLevel est déjà initialisé à 20
                    startGame();
                } else { 
                    JOptionPane.showMessageDialog(this, "Le niveau doit être compris entre 1 et " + maxLevel + ".", "Erreur", JOptionPane.ERROR_MESSAGE);
                    this.requestFocusInWindow(); 
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Entrée invalide. Veuillez entrer un nombre.", "Erreur", JOptionPane.ERROR_MESSAGE);
                this.requestFocusInWindow(); 
            }
        }
        this.requestFocusInWindow();
    }

    private void startGame() {
        setPanelVisibility(menuPanel, false);
        currentGameState = GameState.PLAYING;
        score = 0; 
        initGameElementsForLevel(currentLevel); 
        gamePanel.setVisible(true);
        gameTimer.start();
        this.requestFocusInWindow();
    }

    private void initGameElementsForLevel(int level) {
        playerPosition = new Point(1, 1); 
        createMazeAndDotsForLevel(level);
        initialDotCount = dots.size(); 
        targetScore = 500 + (currentLevel - 1) * 250; 
        if (targetScore > initialDotCount * 10) {
            targetScore = initialDotCount * 10;
        }

        placeEnemies(); 
        
        // Adapte la vitesse des ennemis par niveau
        int newDelay = Math.max(50, 400 - (level - 1) * 10);
        gameTimer.setDelay(newDelay);

        gamePanel.repaint(); 
    }

    private void showInstructions() {
        JOptionPane.showMessageDialog(this,
                "Instructions du Jeu:\n" +
                        "- Utilisez les flèches directionnelles (HAUT, BAS, GAUCHE, DROITE) pour déplacer le carré jaune.\n" +
                        "- Mangez tous les petits cercles blancs pour gagner le niveau.\n" +
                        "- Évitez les murs (carrés bleus).\n" +
                        "- Les carrés rouges sont des ennemis ! Ils vous pourchassent ! Si un ennemi vous touche, c'est GAME OVER !\n" +
                        "- Appuyez sur 'P' pour mettre le jeu en pause.\n" +
                        "- Atteignez votre niveau final choisi pour gagner la partie !",
                "Instructions", JOptionPane.INFORMATION_MESSAGE);
        this.requestFocusInWindow();
    }

    private void showCredits() {
        JOptionPane.showMessageDialog(this,
                "Crédits:\n" +
                        "- Conception et développement: Samyn-Antoy ABASS (RTN)\n" +
                        "- Inspiré par des jeux de labyrinthe classiques.",
                "Crédits", JOptionPane.INFORMATION_MESSAGE);
        this.requestFocusInWindow();
    }

    private void pauseGame() {
        currentGameState = GameState.PAUSED;
        gameTimer.stop();
        setPanelVisibility(pausePanel, true);
        layeredPane.repaint();
    }

    private void resumeGame() {
        currentGameState = GameState.PLAYING;
        setPanelVisibility(pausePanel, false);
        gameTimer.start();
        this.requestFocusInWindow();
    }

    private void hidePauseScreen() {
        setPanelVisibility(pausePanel, false);
    }

    private void gameOver() {
        if (currentGameState == GameState.PLAYING) {
            currentGameState = GameState.GAME_OVER;
            gameTimer.stop();
            endMessageLabel.setText("GAME OVER ! Votre score final: " + score);
            setPanelVisibility(endScreenPanel, true);
            layeredPane.repaint();
            askForNameAndAddHighScore(score); 
            this.requestFocusInWindow();
        }
    }

    private void gameWinLevel() {
        gameTimer.stop(); 
        currentLevel++; 
        if (currentLevel > maxLevel) {
            currentLevel = 1; 
            score = 0; 
            JOptionPane.showMessageDialog(this, "Félicitations ! Vous avez terminé le cycle de " + maxLevel + " niveaux ! Recommencez au Niveau 1 !", "Cycle de Niveaux Complété", JOptionPane.INFORMATION_MESSAGE);
            initGameElementsForLevel(currentLevel); 
            gameTimer.start(); 
            this.requestFocusInWindow(); 
        } else {
            JOptionPane.showMessageDialog(this, "Niveau " + (currentLevel - 1) + " terminé ! Préparez-vous pour le niveau " + currentLevel + " !", "Niveau Terminé", JOptionPane.INFORMATION_MESSAGE);
            initGameElementsForLevel(currentLevel); 
            gameTimer.start(); 
            this.requestFocusInWindow(); 
        }
    }

    private void hideEndScreen() {
        setPanelVisibility(endScreenPanel, false);
    }

    private void restartGame() {
        showMenu();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
    }

    private class GamePanel extends JPanel {
        public GamePanel() {
            setOpaque(true);
            setBackground(Color.BLACK);
            setDoubleBuffered(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (currentGameState == GameState.PLAYING || currentGameState == GameState.PAUSED) {
                g.setColor(Color.DARK_GRAY); 
                for (Point obstacle : obstacles) {
                    g.fillRect(obstacle.x * CELL_SIZE, obstacle.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }

                g.setColor(Color.WHITE);
                for (Point dot : dots) {
                    g.fillOval(dot.x * CELL_SIZE + CELL_SIZE / 4, dot.y * CELL_SIZE + CELL_SIZE / 4, CELL_SIZE / 2, CELL_SIZE / 2);
                }

                g.setColor(Color.YELLOW);
                g.fillRect(playerPosition.x * CELL_SIZE, playerPosition.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);

                g.setColor(new Color(180, 0, 0)); 
                for (Point enemy : enemies) {
                    g.fillRect(enemy.x * CELL_SIZE, enemy.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }

                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 18)); 
                g.drawString("Score: " + score, 10, 20);
                g.drawString("Niveau: " + currentLevel + " / " + maxLevel, WIDTH - 180, 20); 
                g.drawString("Cible: " + targetScore, WIDTH - 80, 20); 
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
                case KeyEvent.VK_P: pauseGame(); return;
            }

            if (isWalkable(newPlayerPos)) {
                playerPosition.setLocation(newPlayerPos);

                dots.removeIf(dot -> dot.equals(playerPosition));
                score += 10;
                
                checkEnemyCollision(); 
                checkWinCondition(); 
            }
        } else if (e.getKeyCode() == KeyEvent.VK_P && currentGameState == GameState.PAUSED) {
            resumeGame();
        }
    }

    private boolean isWalkable(Point p) {
        return p.x >= 0 && p.y >= 0 && p.x < gridCols && p.y < gridRows && !obstacles.contains(p);
    }

    @Override
    public void keyTyped(KeyEvent e) { /* Not used */ }

    @Override
    public void keyReleased(KeyEvent e) { /* Not used */ }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentGameState == GameState.PLAYING) {
            moveEnemies();
            checkEnemyCollision(); 
            checkWinCondition(); 
            
            if (currentGameState == GameState.PLAYING) {
                gamePanel.repaint();
            }
        }
    }

    private void checkWinCondition() {
        if (currentGameState == GameState.PLAYING) {
            if (score >= targetScore) {
                gameWinLevel();
            }
        }
    }

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

            if (current.equals(target)) {
                return currentPath;
            }

            int[] dx = {0, 0, 1, -1};
            int[] dy = {1, -1, 0, 0};

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

    private void moveEnemies() {
        for (int i = 0; i < enemies.size(); i++) {
            Point currentEnemyPos = enemies.get(i);
            
            List<Point> path = findPath(currentEnemyPos, playerPosition);

            if (path != null && path.size() > 1) {
                enemies.get(i).setLocation(path.get(1));
            } else {
                List<Point> possibleMoves = new ArrayList<>();
                int[] dx = {0, 0, 1, -1};
                int[] dy = {1, -1, 0, 0};

                for (int j = 0; j < 4; j++) {
                    Point next = new Point(currentEnemyPos.x + dx[j], currentEnemyPos.y + dy[j]);
                    if (isWalkable(next)) {
                        possibleMoves.add(next);
                    }
                }
                if (!possibleMoves.isEmpty()) {
                    enemies.get(i).setLocation(possibleMoves.get(random.nextInt(possibleMoves.size())));
                }
            }
        }
    }

    private void checkEnemyCollision() {
        if (currentGameState == GameState.PLAYING) {
            for (Point enemy : enemies) {
                if (enemy.equals(playerPosition)) {
                    gameOver();
                    return;
                }
            }
        }
    }

    // --- HIGH SCORE MANAGEMENT ---

    private static class HighScoreEntry implements Serializable, Comparable<HighScoreEntry> {
        private static final long serialVersionUID = 1L;
        String name;
        int score;

        public HighScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }

        @Override
        public String toString() {
            return String.format("%s: %d", name, score);
        }

        @Override
    public int compareTo(HighScoreEntry other) {
            return Integer.compare(other.score, this.score); 
        }
    }

    @SuppressWarnings("unchecked")
    private void loadHighScores() {
        highScores.clear();
        File file = new File(HIGHSCORE_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object obj = ois.readObject();
                if (obj instanceof List) {
                    highScores = (List<HighScoreEntry>) obj;
                    Collections.sort(highScores);
                    while (highScores.size() > MAX_HIGHSCORES) {
                        highScores.remove(highScores.size() - 1);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading high scores: " + e.getMessage());
            }
        }
    }

    private void saveHighScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(HIGHSCORE_FILE))) {
            oos.writeObject(highScores);
        } catch (IOException e) {
            System.err.println("Error saving high scores: " + e.getMessage());
        }
    }

    private void askForNameAndAddHighScore(int finalScore) {
        if (highScores.size() < MAX_HIGHSCORES || (highScores.isEmpty() ? true : finalScore > highScores.get(highScores.size() - 1).score)) {
            String name = JOptionPane.showInputDialog(this, "Nouveau meilleur score ! Entrez votre nom:", "Meilleur Score", JOptionPane.PLAIN_MESSAGE);
            if (name != null && !name.trim().isEmpty()) {
                addHighScore(new HighScoreEntry(name.trim(), finalScore));
            } else {
                addHighScore(new HighScoreEntry("Anonyme", finalScore)); 
            }
            this.requestFocusInWindow(); 
        } else {
            saveHighScores(); 
        }
    }

    private void addHighScore(HighScoreEntry entry) {
        highScores.add(entry);
        Collections.sort(highScores);
        while (highScores.size() > MAX_HIGHSCORES) {
            highScores.remove(highScores.size() - 1);
        }
        saveHighScores();
    }

    private void showHighScores() {
        currentGameState = GameState.HIGHSCORES;
        setPanelVisibility(menuPanel, false);
        setPanelVisibility(pausePanel, false);
        setPanelVisibility(endScreenPanel, false);
        gamePanel.setVisible(false);
        setPanelVisibility(highScoresPanel, true);

        StringBuilder sb = new StringBuilder();
        if (highScores.isEmpty()) {
            sb.append("Aucun score enregistré pour le moment.\nJouez pour en ajouter !");
        } else {
            for (int i = 0; i < highScores.size(); i++) {
                HighScoreEntry entry = highScores.get(i);
                sb.append(String.format("%d. %s%n", (i + 1), entry.toString()));
            }
        }
        highScoresDisplay.setText(sb.toString());
        highScoresDisplay.setCaretPosition(0);

        this.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PacManGame game = new PacManGame();
            game.setVisible(true);
        });
    }
}