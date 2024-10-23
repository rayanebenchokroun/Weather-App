import exceptions.LocationNotFoundException;
import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;

public class WeatherAppGUI extends JFrame {

    // Variable pour stocker les données météo récupérées
    private JSONObject weatherData;

    // Constructeur de l'interface graphique de l'application météo
    public WeatherAppGUI() {
        super("Weather App"); // Titre de la fenêtre
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Fermer l'application lors de la fermeture de la fenêtre
        setSize(450, 650); // Dimensions de la fenêtre
        setLocationRelativeTo(null); // Centrer la fenêtre
        setLayout(null); // Aucun layout manager pour un positionnement libre
        setResizable(false); // Fenêtre non redimensionnable
        addGuiComponents(); // Ajouter les composants de l'interface utilisateur
    }

    // Méthode pour ajouter les composants de l'interface utilisateur
    private void addGuiComponents() {
        // Champ de texte pour la recherche de la ville
        JTextField searchTextField = new JTextField();
        searchTextField.setBounds(15, 15, 351, 45); // Position et taille
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24)); // Style de police
        add(searchTextField); // Ajout au panneau

        // Image par défaut pour l'état météo
        JLabel weatherConditionImage = new JLabel(loadImage("src/assets/cloudy.png"));
        weatherConditionImage.setBounds(0, 125, 450, 217); // Position et taille
        add(weatherConditionImage);

        // Texte pour afficher la température
        JLabel temperatureText = new JLabel("10 C"); // Valeur par défaut
        temperatureText.setBounds(0, 350, 450, 54); // Position et taille
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48)); // Style de police
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER); // Centrer le texte
        add(temperatureText);

        // Texte pour afficher la description de l'état météo
        JLabel weatherConditionDesc = new JLabel("Cloudy"); // Valeur par défaut
        weatherConditionDesc.setBounds(0, 405, 450, 36); // Position et taille
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32)); // Style de police
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER); // Centrer le texte
        add(weatherConditionDesc);

        // Image pour l'humidité
        JLabel humidityImage = new JLabel(loadImage("src/assets/humidity.png"));
        humidityImage.setBounds(15, 500, 74, 66); // Position et taille
        add(humidityImage);

        // Texte pour afficher l'humidité
        JLabel humidityText = new JLabel("<html><b>Humidity</b> 100%</html>"); // Valeur par défaut
        humidityText.setBounds(90, 500, 85, 55); // Position et taille
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16)); // Style de police
        add(humidityText);

        // Image pour la vitesse du vent
        JLabel windspeedImage = new JLabel(loadImage("src/assets/windspeed.png"));
        windspeedImage.setBounds(220, 500, 74, 66); // Position et taille
        add(windspeedImage);

        // Texte pour afficher la vitesse du vent
        JLabel windspeedText = new JLabel("<html><b>Windspeed</b> 15km/h</html>"); // Valeur par défaut
        windspeedText.setBounds(310, 500, 85, 55); // Position et taille
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16)); // Style de police
        add(windspeedText);

        // Bouton de recherche avec une image
        JButton searchButton = new JButton(loadImage("src/assets/search.png"));
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Changer le curseur pour la main
        searchButton.setBounds(375, 13, 47, 45); // Position et taille
        searchButton.addActionListener(new ActionListener() { // Ajout de l'action lors du clic sur le bouton
            @Override
            public void actionPerformed(ActionEvent e) {
                String userInput = searchTextField.getText(); // Récupère le texte saisi par l'utilisateur

                // Si le champ de texte est vide, ne rien faire
                if (userInput.replaceAll("\\s", "").length() <= 0) {
                    return;
                }

                // Récupération des données météo pour la ville saisie
                try {
                    weatherData = WeatherApp.getWeatherData(userInput);
                } catch (LocationNotFoundException ex) {
                    JOptionPane.showMessageDialog(null, "This place does not exist.");
                    return;
                }

                // Si les données météo ne sont pas disponibles, afficher un message d'erreur
                if (weatherData == null) {
                    JOptionPane.showMessageDialog(null, "Unable to retrieve weather data.");
                    return;
                }

                // Mise à jour de l'image en fonction de la condition météo
                String weatherCondition = (String) weatherData.get("weather_condition");
                switch (weatherCondition) {
                    case "Clear":
                        weatherConditionImage.setIcon(loadImage("src/assets/clear.png"));
                        break;
                    case "Cloudy":
                        weatherConditionImage.setIcon(loadImage("src/assets/cloudy.png"));
                        break;
                    case "Rain":
                        weatherConditionImage.setIcon(loadImage("src/assets/rain.png"));
                        break;
                    case "Snow":
                        weatherConditionImage.setIcon(loadImage("src/assets/snow.png"));
                        break;
                    default:
                        weatherConditionImage.setIcon(loadImage("src/assets/default.png"));
                }

                // Mise à jour de la température
                double temperature = (double) weatherData.getOrDefault("temperature", 0.0);
                temperatureText.setText(temperature + " C");

                // Mise à jour de la description de la condition météo
                weatherConditionDesc.setText(weatherCondition);

                // Mise à jour de l'humidité
                long humidity = (long) weatherData.getOrDefault("humidity", 0L);
                humidityText.setText("<html><b>Humidity</b> " + humidity + "%</html>");

                // Mise à jour de la vitesse du vent
                Object windspeedObj = weatherData.get("windspeed");
                if (windspeedObj != null) {
                    double windspeed = ((Number) windspeedObj).doubleValue();
                    windspeedText.setText("<html><b>Windspeed</b> " + windspeed + " km/h</html>");
                } else {
                    windspeedText.setText("<html><b>Windspeed</b> N/A</html>");
                }
            }
        });
        add(searchButton);
    }

    // Méthode pour charger une image à partir du chemin spécifié
    private ImageIcon loadImage(String resourcePath) {
        try {
            BufferedImage image = ImageIO.read(new File(resourcePath)); // Lit l'image
            return new ImageIcon(image); // Retourne l'image sous forme d'ImageIcon
        } catch (IOException e) {
            e.printStackTrace(); // Affiche l'erreur en cas de problème
        }

        System.out.println("Could not find resource"); // Message d'erreur si l'image n'est pas trouvée
        return null; // Retourne null si l'image n'est pas chargée
    }
}
