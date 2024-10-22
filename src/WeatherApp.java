import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class WeatherApp {

    // Méthode principale pour obtenir les données météo en fonction du nom de la ville
    public static JSONObject getWeatherData(String locationName) {
        // Récupération des données de localisation à partir du nom du lieu
        JSONArray locationData = getLocationData(locationName);

        // Extrait les coordonnées latitude et longitude
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        // URL de l'API météo avec latitude et longitude
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude +
                "&hourly=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=auto";

        try {
            // Connexion à l'API météo
            HttpURLConnection conn = fetchApiResponse(urlString);

            // Vérifie si la connexion a réussi
            if (conn.getResponseCode() != 200) {
                System.out.println("Error: could not connect to API");
                return null;
            }

            // Lit la réponse JSON de l'API
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while (scanner.hasNext()) {
                resultJson.append(scanner.nextLine());
            }
            scanner.close();
            conn.disconnect(); // Ferme la connexion

            // Parse le résultat JSON
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            // Extrait les données horaires du JSON
            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");

            // Recherche l'index correspondant à l'heure actuelle
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            // Récupère la température, l'humidité, le code météo et la vitesse du vent
            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (double) temperatureData.get(index);

            JSONArray weather_code = (JSONArray) hourly.get("weather_code");
            String weatherCondition = convertWeathercode((long) weather_code.get(index));

            JSONArray relativeHumidity = (JSONArray) hourly.get("relative_humidity_2m");
            long humidity = (long) relativeHumidity.get(index);

            JSONArray windspeedData = (JSONArray) hourly.get("wind_speed_10m");
            double windspeed = (double) windspeedData.get(index);

            // Crée un objet JSON contenant les données météo
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windspeed", windspeed);

            // Retourne les données météo
            return weatherData;

        } catch (Exception e) {
            e.printStackTrace(); // Gère les exceptions
        }
        return null;
    }

    // Méthode pour obtenir les données de localisation en fonction du nom de la ville
    public static JSONArray getLocationData(String locationName) {
        // Remplace les espaces par des '+' pour l'URL
        locationName = locationName.replaceAll(" ", "+");

        // URL de l'API de géolocalisation
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=10&language=en&format=json";

        try {
            // Connexion à l'API de géolocalisation
            HttpURLConnection conn = fetchApiResponse(urlString);

            // Vérifie si la connexion a réussi
            if (conn.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
                return null;
            } else {
                // Lit et parse la réponse JSON
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());

                while (scanner.hasNext()) {
                    resultJson.append(scanner.nextLine());
                }
                scanner.close();
                conn.disconnect(); // Ferme la connexion

                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(resultJson.toString());

                // Retourne les résultats de localisation sous forme de JSONArray
                JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
                return locationData;
            }
        } catch (Exception e) {
            e.printStackTrace(); // Gère les exceptions
        }

        return null;
    }

    // Méthode pour envoyer une requête HTTP GET et obtenir une réponse
    private static HttpURLConnection fetchApiResponse(String urlString) {
        try {
            // Crée et configure la connexion à l'URL donnée
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            return conn; // Retourne la connexion
        } catch (Exception e) {
            e.printStackTrace(); // Gère les exceptions
        }
        return null;
    }

    // Méthode pour trouver l'index correspondant à l'heure actuelle dans les données horaires
    private static int findIndexOfCurrentTime(JSONArray timeList) {
        String currentTime = getCurrentTime(); // Obtient l'heure actuelle

        // Parcourt les heures pour trouver celle qui correspond à l'heure actuelle
        for (int i = 0; i < timeList.size(); i++) {
            String time = (String) timeList.get(i);
            if (time.equalsIgnoreCase(currentTime)) {
                return i;
            }
        }

        return 0; // Retourne 0 si l'heure n'est pas trouvée
    }

    // Méthode pour obtenir l'heure actuelle au format "yyyy-MM-dd'T'HH:00"
    public static String getCurrentTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();

        // Formatage de la date et de l'heure
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");
        return currentDateTime.format(formatter); // Retourne l'heure formatée
    }

    // Convertit un code météo en une description textuelle
    private static String convertWeathercode(long weather_code) {
        String weather_Condition = "";

        // Détermine la condition météo en fonction du code météo
        if (weather_code == 0L) {
            weather_Condition = "Clear";
        } else if (weather_code > 0L && weather_code <= 3L) {
            weather_Condition = "Cloudy";
        } else if ((weather_code >= 51L && weather_code <= 67L)
                || (weather_code >= 80L && weather_code <= 99L)) {
            weather_Condition = "Rain";
        } else if (weather_code >= 71L && weather_code <= 77L) {
            weather_Condition = "Snow";
        }

        return weather_Condition; // Retourne la description météo
    }
}
