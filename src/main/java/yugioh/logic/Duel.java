package yugioh.logic;

import yugioh.listener.BattleListener;
import yugioh.model.Card;
import java.util.*;

public class Duel {
    private final List<Card> playerDeck;  // Cartas del jugador
    private final List<Card> aiDeck;      // Cartas de la máquina
    private int playerScore = 0;          // Rondas ganadas jugador
    private int aiScore = 0;              // Rondas ganadas máquina
    private BattleListener listener;
    private Card lastAiCard;              // Última carta usada por la máquina

    public Duel(List<Card> playerDeck, List<Card> aiDeck) {
        this.playerDeck = new ArrayList<>(playerDeck);
        this.aiDeck = new ArrayList<>(aiDeck);
    }

    public void setListener(BattleListener listener) {
        this.listener = listener;
    }

    // Verifica si el duelo terminó
    public boolean isFinished() {
        return playerScore >= 2 || aiScore >= 2;
    }

    public List<Card> getAiDeck() {
        return aiDeck;
    }

    public Card getLastAiCard() {
        return lastAiCard;
    }

    public void playRound(Card playerCard) {
        if (isFinished()) return;

        // La máquina elige una carta aleatoria
        Random random = new Random();
        Card aiCard = aiDeck.remove(random.nextInt(aiDeck.size()));
        lastAiCard = aiCard;

        resolveRound(playerCard, aiCard);
    }
    
    // Metodo centralizado para resolver la ronda ya que compara las cartas y determina el ganador
    public void resolveRound(Card playerCard, Card aiCard) {
        if (isFinished()) return;
        
        Random random = new Random();
        
        // Asignar posiciones aleatorias a las cartas es decir ATK o DEF
        playerCard.setPosition(random.nextBoolean() ? "ATK" : "DEF");
        aiCard.setPosition(random.nextBoolean() ? "ATK" : "DEF");

        int playerValue = playerCard.getPosition().equals("ATK") ? playerCard.getAtk() : playerCard.getDef();
        int aiValue = aiCard.getPosition().equals("ATK") ? aiCard.getAtk() : aiCard.getDef();
        
        // Determinar el ganador
        String winner;
        if (playerValue > aiValue) {
            playerScore++;
            winner = "Jugador";
        } else if (aiValue > playerValue) {
            aiScore++;
            winner = "Máquina";
        } else {
            // Empate, en este caso se decide de manera aleatoria
            if (random.nextBoolean()) {
                playerScore++;
                winner = "Jugador (empate)";
            } else {
                aiScore++;
                winner = "Máquina (empate)";
            }
        }

        // Usar BattleListener
        if (listener != null) {
            String playerInfo = playerCard.getName() + " [" + playerCard.getPosition() + ":" + playerValue + "]";
            String aiInfo = aiCard.getName() + " [" + aiCard.getPosition() + ":" + aiValue + "]";
            
            listener.onTurn(playerInfo, aiInfo, winner);
            listener.onScoreChanged(playerScore, aiScore);
            
            if (isFinished()) {
                listener.onDuelEnded(playerScore > aiScore ? "Jugador" : "Máquina");
            }
        }
    }
}
