package yugioh.api;

import yugioh.model.Card;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;

public class YgoApiClient {
    private static final String API_URL = "https://db.ygoprodeck.com/api/v7/randomcard.php";
    private static final int MAX_INTENTOS = 5;
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    public Card fetchRandomMonster() throws Exception {
        for (int i = 0; i < MAX_INTENTOS; i++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .timeout(TIMEOUT) // 2) timeout
                        .header("Accept", "application/json")
                        .header("User-Agent", "YuGiOh")
                        .GET()
                        .build();

                HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                // 1. El status
                if (response.statusCode() != 200) throw new Exception("HTTP " + response.statusCode());

                // 2) Si hay data toma data y solo tipo Monster
                JSONObject root = new JSONObject(response.body());
                JSONObject cardData = root.has("data")
                        ? root.getJSONArray("data").getJSONObject(0)
                        : root;

                String type = cardData.optString("type", "");
                if (!type.contains("Monster")) {
                    Thread.sleep(200);
                    continue;
                }

                String name = cardData.optString("name", "Unknown");
                int atk = cardData.optInt("atk", 0);
                int def = cardData.optInt("def", 0);
                JSONArray images = cardData.optJSONArray("card_images");
                String imageUrl = (images != null && images.length() > 0)
                        ? images.getJSONObject(0).optString("image_url", "")
                        : "";

                return new Card(name, atk, def, imageUrl);

            } catch (InterruptedException ie) {
                throw ie;
            } catch (Exception e) {
                if (i == MAX_INTENTOS - 1) throw new Exception("Error obteniendo carta: " + e.getMessage(), e);
                try { Thread.sleep(200); } catch (InterruptedException ie) { throw ie; }
            }
        }
        throw new Exception("No se pudo obtener una carta Monster");
    }

    public List<Card> fetchRandomMonsters(int cantidad) throws Exception {
        List<Card> cartas = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) cartas.add(fetchRandomMonster());
        return cartas;
    }
}
