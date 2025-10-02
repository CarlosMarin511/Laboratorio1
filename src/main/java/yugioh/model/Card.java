package yugioh.model;

public class Card {
    private final String name;
    private final int atk;
    private final int def;
    private final String imageUrl;
    private String position; // Es para asignar ATK o DEF

    // Constructor para crear una carta con los datos necesarios
    public Card(String name, int atk, int def, String imageUrl) {
        this.name = name;
        this.atk = atk;
        this.def = def;
        this.imageUrl = imageUrl;
        this.position = "ATK"; // Por defecto es el ATK
    }

    public String getName() { 
        return name; 
    }
    
    public int getAtk() { 
        return atk; 
    }
    
    public int getDef() { 
        return def; 
    }
    
    public String getImageUrl() { 
        return imageUrl; 
    }
    
    public String getPosition() {
        return position;
    }
    
    // Establecer posición es decir ATK o DEF
    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return name + " [ATK:" + atk + " DEF:" + def + "]";
    }
}
