package yugioh.ui;

import yugioh.api.YgoApiClient;
import yugioh.listener.BattleListener;
import yugioh.logic.Duel;
import yugioh.model.Card;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GameWindow extends JFrame implements BattleListener {
    
    // Componentes
    private final JTextArea log = new JTextArea(12, 50);
    private final JButton btnCargar = new JButton("Cargar cartas");
    private final JButton btnIniciar = new JButton("Iniciar duelo");
    private final JPanel playerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    private final JPanel aiPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    private final JLabel lblMarcador = new JLabel("Marcador: 0 - 0", SwingConstants.CENTER);

    // Variables
    private final YgoApiClient api = new YgoApiClient();
    private List<Card> playerDeck;
    private List<Card> aiDeck;
    private List<Card> playerDeckOriginal = new ArrayList<>();
    private List<Card> aiDeckOriginal = new ArrayList<>();
    private List<Card> playerUsedCards = new ArrayList<>();
    private List<Card> aiUsedCards = new ArrayList<>();
    private Duel duel;
    private Card cartaMaquinaEsperando; // Carta que jugó la máquina si empieza primero
    
    // Colores temáticos
    private final Color moradoOscuro = new Color(75, 0, 130);
    private final Color negro = new Color(20, 20, 30);
    private final Color azulElectrico = new Color(0, 191, 255);
    private final Color dorado = new Color(255, 215, 0);
    private final Color moradoMedio = new Color(138, 43, 226);

    public GameWindow() {
        super("⚔️ YU-GI-OH! DUEL ARENA ⚔️");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        
        // Color de fondo
        getContentPane().setBackground(negro);

        // Configuración del log de batalla
        log.setEditable(false);
        log.setFont(new Font("Consolas", Font.PLAIN, 14));
        log.setBackground(new Color(30, 30, 40));
        log.setForeground(azulElectrico);
        log.setCaretColor(azulElectrico);
        log.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        log.setLineWrap(true);
        log.setWrapStyleWord(true);

        // Panel superior para el marcador
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(moradoOscuro);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 3, 0, dorado),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        lblMarcador.setFont(new Font("Arial", Font.BOLD, 20));
        lblMarcador.setForeground(dorado);
        
        topPanel.add(lblMarcador, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);
        
        // Botones
        btnCargar.setFont(new Font("Arial", Font.BOLD, 14));
        btnCargar.setBackground(moradoMedio);
        btnCargar.setForeground(Color.WHITE);
        btnCargar.setFocusPainted(false);
        btnCargar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(dorado, 2),
            BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        btnCargar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnIniciar.setFont(new Font("Arial", Font.BOLD, 14));
        btnIniciar.setBackground(new Color(0, 100, 200));
        btnIniciar.setForeground(Color.WHITE);
        btnIniciar.setFocusPainted(false);
        btnIniciar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(dorado, 2),
            BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        btnIniciar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Panel con las cartas
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        centerPanel.setBackground(negro);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        aiPanel.setBackground(new Color(40, 40, 50));
        playerPanel.setBackground(new Color(40, 40, 50));
        
        centerPanel.add(crearPanelConTitulo("🎭 MÁQUINA", aiPanel, azulElectrico, negro));
        centerPanel.add(crearPanelConTitulo("👤 TUS CARTAS (haz clic para jugar)", playerPanel, dorado, negro));
        add(centerPanel, BorderLayout.CENTER);

        // Panel derecho con botones y el log
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(negro);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, dorado),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        rightPanel.setPreferredSize(new Dimension(450, 0));
        
        // Panel superior del lado derecho
        JPanel topRightPanel = new JPanel();
        topRightPanel.setLayout(new BoxLayout(topRightPanel, BoxLayout.Y_AXIS));
        topRightPanel.setBackground(negro);
        topRightPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        botonesPanel.setOpaque(false);
        botonesPanel.add(btnCargar);
        botonesPanel.add(btnIniciar);
        topRightPanel.add(botonesPanel);
        
        // Separador
        JPanel separador = new JPanel();
        separador.setBackground(dorado);
        separador.setPreferredSize(new Dimension(400, 2));
        separador.setMaximumSize(new Dimension(400, 2));
        topRightPanel.add(Box.createVerticalStrut(10));
        topRightPanel.add(separador);
        topRightPanel.add(Box.createVerticalStrut(10));
        
        // Título del log
        JLabel lblLogTitle = new JLabel("📜 LOG DE BATALLA", SwingConstants.CENTER);
        lblLogTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblLogTitle.setForeground(dorado);
        lblLogTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        topRightPanel.add(lblLogTitle);
        
        JScrollPane scrollLog = new JScrollPane(log);
        scrollLog.setBorder(BorderFactory.createLineBorder(moradoMedio, 2));
        scrollLog.getViewport().setBackground(new Color(30, 30, 40));
        
        rightPanel.add(topRightPanel, BorderLayout.NORTH);
        rightPanel.add(scrollLog, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        btnIniciar.setEnabled(false);

        // Eventos de botones
        btnCargar.addActionListener(e -> cargarCartas());
        btnIniciar.addActionListener(e -> iniciarDuelo());

        // Efectos para los botones
        btnCargar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btnCargar.isEnabled()) {
                    btnCargar.setBackground(new Color(160, 60, 255));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCargar.setBackground(moradoMedio);
            }
        });
        
        btnIniciar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btnIniciar.isEnabled()) {
                    btnIniciar.setBackground(new Color(0, 140, 255));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnIniciar.setBackground(new Color(0, 100, 200));
            }
        });

        setSize(1350, 850);
        setLocationRelativeTo(null);
    }

    // Metodo para crear paneles con el titulo
    private JPanel crearPanelConTitulo(String titulo, JPanel contenido, Color colorTitulo, Color colorFondo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(colorFondo);
        
        // Título personalizado
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setForeground(colorTitulo);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // Borde
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(colorTitulo, 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(contenido, BorderLayout.CENTER);
        return panel;
    }

    // Carga las cartas desde la API
    private void cargarCartas() {
        btnCargar.setEnabled(false);
        btnIniciar.setEnabled(false);
        
        // Limpiar al cargar de nuevo las cartas
        log.setText("");
        lblMarcador.setText("Marcador: 0 - 0");
        playerUsedCards.clear();
        aiUsedCards.clear();
        playerDeckOriginal.clear();
        aiDeckOriginal.clear();
        duel = null;
        
        log.append("Cargando cartas desde la API...\n");

        // Thread para no bloquear la interfaz
        new Thread(() -> {
            try {
                // Obtener 3 cartas para el jugador
                List<Card> cartasJugador = api.fetchRandomMonsters(3);
                
                // Obtener 3 cartas para la máquina
                List<Card> cartasMaquina = api.fetchRandomMonsters(3);
                
                // Guardar las cartas
                playerDeck = new ArrayList<>(cartasJugador);
                aiDeck = new ArrayList<>(cartasMaquina);
                playerDeckOriginal = new ArrayList<>(cartasJugador);
                aiDeckOriginal = new ArrayList<>(cartasMaquina);
                
                // Actualizar la interfaz
                SwingUtilities.invokeLater(() -> {
                    mostrarCartas();
                    log.append("Cartas cargadas\n");
                    log.append("Presiona el botón Iniciar Duelo para comenzar\n");
                    btnCargar.setEnabled(true);
                    btnIniciar.setEnabled(true);
                });
                
            } catch (Exception e) {
                // Mensajes de error
                SwingUtilities.invokeLater(() -> {
                    log.append("ERROR: " + e.getMessage() + "\n");
                    JOptionPane.showMessageDialog(this, 
                        "Error cargando cartas: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            btnCargar.setEnabled(true);
                });
            }
        }).start();
    }

    // Muestra las cartas con las imágenes
    private void mostrarCartas() {
        playerPanel.removeAll();
        aiPanel.removeAll();

        // Cartas jugador
        for (Card carta : playerDeckOriginal) {
            boolean usada = playerUsedCards.contains(carta);
            playerPanel.add(crearPanelCarta(carta, false, usada));
        }

        // Cartas máquina
        for (Card carta : aiDeckOriginal) {
            boolean usada = aiUsedCards.contains(carta);
            aiPanel.add(crearPanelCarta(carta, true, usada));
        }

        playerPanel.revalidate();
        playerPanel.repaint();
        aiPanel.revalidate();
        aiPanel.repaint();
    }

    // Panel para mostrar la carta con su imagen y la información
    private JPanel crearPanelCarta(Card carta, boolean esIA, boolean usada) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        if (usada) {
            panel.setBackground(new Color(60, 60, 70));
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 110), 3),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
        } else {
            panel.setBackground(new Color(40, 40, 55));
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(esIA ? azulElectrico : dorado, 3),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
        }
        
        panel.setPreferredSize(new Dimension(260, 380));

        // Cargar imagen de la carta
        JLabel lblImagen = new JLabel("Cargando...", SwingConstants.CENTER);
        lblImagen.setPreferredSize(new Dimension(240, 240));

        new Thread(() -> {
            try {
                // Descargar imagen
                Image img = ImageIO.read(new URL(carta.getImageUrl()));
                Image scaledImg = img.getScaledInstance(240, 240, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaledImg);
                
                // Actualizar la interfaz
                SwingUtilities.invokeLater(() -> {
                    if (usada) {
                        // Si está usada, poner en gris
                        Image grayImg = crearImagenGris(scaledImg);
                        lblImagen.setIcon(new ImageIcon(grayImg));
                    } else {
                        lblImagen.setIcon(icon);
                    }
                });
                
            } catch (Exception e) {
                //Por si falla
            }
        }).start();

        // Información de las cartas
        String nombreCorto = carta.getName().length() > 20 
            ? carta.getName().substring(0, 17) + "..." 
            : carta.getName();
        
        JLabel lblInfo = new JLabel("<html><center><b>" + nombreCorto + "</b><br>" +
            "<span style='color:#FF6B6B;'>⚔ ATK:" + carta.getAtk() + "</span> " +
            "<span style='color:#4ECDC4;'>🛡 DEF:" + carta.getDef() + "</span>" +
            (usada ? "<br><span style='color:#666;'>✓ USADA</span>" : "") +
            "</center></html>", SwingConstants.CENTER);
        lblInfo.setFont(new Font("Arial", Font.PLAIN, 15));
        lblInfo.setForeground(usada ? Color.GRAY : Color.WHITE);
        lblInfo.setBorder(BorderFactory.createEmptyBorder(12, 5, 12, 5));

        panel.add(lblImagen);
        panel.add(lblInfo);

        // Carta del jugador para darle clic
        if (!esIA && !usada) {
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            Color doradoBrillante = new Color(255, 235, 50);
            Color bordeOriginal = dorado;
            
            panel.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (duel != null && !duel.isFinished() && playerDeck.contains(carta)) {
                        jugarCarta(carta);
                    }
                }
                
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (duel != null && !duel.isFinished() && playerDeck.contains(carta)) {
                        panel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(doradoBrillante, 4),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)
                        ));
                    }
                }
                
                public void mouseExited(java.awt.event.MouseEvent e) {
                    panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(bordeOriginal, 3),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                    ));
                }
            });
        }

        return panel;
    }

    // Convierte una imagen a gris para saber que se usó
    private Image crearImagenGris(Image img) {
        int width = img.getWidth(null);
        int height = img.getHeight(null);
        java.awt.image.BufferedImage grayImg = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = grayImg.createGraphics();

        g2d.drawImage(img, 0, 0, null);
        java.awt.image.ColorConvertOp op = new java.awt.image.ColorConvertOp(
            java.awt.color.ColorSpace.getInstance(java.awt.color.ColorSpace.CS_GRAY), null);
        op.filter(grayImg, grayImg);
        
        g2d.dispose();
        return grayImg;
    }

    private void iniciarDuelo() {
        duel = new Duel(playerDeck, aiDeck);
        duel.setListener(this);
        btnIniciar.setEnabled(false);
        btnCargar.setEnabled(false);
        cartaMaquinaEsperando = null;
        
        // Definir turno inicial aleatoriamente
        boolean empiezaJugador = Math.random() < 0.5;
        
        log.append("\n=== DUELO INICIADO ===\n");
        log.append("Turno inicial: " + (empiezaJugador ? "JUGADOR" : "MÁQUINA") + "\n\n");
        
        if (empiezaJugador) {
            log.append("Tu turno! selecciona una de tus cartas\n\n");
        } else {
            if (!aiDeck.isEmpty()) {
                java.util.Random random = new java.util.Random();
                cartaMaquinaEsperando = aiDeck.remove(random.nextInt(aiDeck.size()));
                aiUsedCards.add(cartaMaquinaEsperando);

                duel.getAiDeck().remove(cartaMaquinaEsperando);
                
                log.append("Máquina juega: " + cartaMaquinaEsperando.getName() + 
                          " [ATK:" + cartaMaquinaEsperando.getAtk() + 
                          " DEF:" + cartaMaquinaEsperando.getDef() + "]\n\n");
                log.append("Tu turno! selecciona una de tus cartas\n\n");
                
                // Mostrar la carta de la máquina en gris
                mostrarCartas();
            }
        }
    }

    private void jugarCarta(Card cartaElegida) {
        if (duel == null || duel.isFinished()) return;

        // Mover la carta del jugador a usadas
        playerDeck.remove(cartaElegida);
        playerUsedCards.add(cartaElegida);
        
        // Si la máquina empieza primero usar la carta
        if (cartaMaquinaEsperando != null) {
            // Resolver ronda con la carta que ya eligió la máquina
            duel.resolveRound(cartaElegida, cartaMaquinaEsperando);
            cartaMaquinaEsperando = null;
        } else {
            // Jugar la ronda normal
            duel.playRound(cartaElegida);
            
            // Obtener la carta que usó la máquina y moverla a usadas
            Card cartaMaquina = duel.getLastAiCard();
            if (cartaMaquina != null) {
                aiDeck.remove(cartaMaquina);
                aiUsedCards.add(cartaMaquina);
            }
        }

        mostrarCartas();
    }

    //BattleListener
    
    @Override
    public void onTurn(String playerCard, String aiCard, String winner) {
        log.append("TURNO:\n");
        log.append("  Jugador jugó: " + playerCard + "\n");
        log.append("  Máquina jugó: " + aiCard + "\n");
        log.append("  Ganador: " + winner + "\n\n");
    }

    @Override
    public void onScoreChanged(int playerScore, int aiScore) {
        lblMarcador.setText("Marcador: Jugador " + playerScore + " - " + aiScore + " Máquina");
    }

    @Override
    public void onDuelEnded(String winner) {
        log.append("===============================\n");
        log.append("DUELO TERMINADO!\n");
        log.append("GANADOR: " + winner + "\n");
        log.append("===============================\n");
        
        JOptionPane.showMessageDialog(this, 
            "¡Duelo terminado!\n\nGanador: " + winner, 
            "Fin del Duelo", 
            JOptionPane.INFORMATION_MESSAGE);
        
        btnCargar.setEnabled(true);
    }
}
