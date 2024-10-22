import javax.swing.*;

public class AppLauncher {
    public static void main(String[] args)  {
        // Utilise SwingUtilities pour garantir que la partie GUI est lancée sur le thread d'interface graphique
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                // Crée une instance de WeatherAppGUI et la rend visible
                new WeatherAppGUI().setVisible(true);

                // Affiche l'heure actuelle obtenue depuis WeatherApp dans la console
                System.out.println(WeatherApp.getCurrentTime());
            }
        });
    }
}
