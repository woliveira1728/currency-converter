import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("""
                    Select an option:
                    1. Convert currency
                    2. Exit
                    """);

            int option = getIntInput(scanner);

            switch (option) {
                case 1:
                    convertCurrency(scanner);
                    break;
                case 2:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("""
                            Invalid option!
                            Select a valid option.
                            """);
            }
        }
    }

    public static void convertCurrency(Scanner scanner) {
        scanner.nextLine();

        System.out.println("Set your Origin Currency: (Ex: USD)");
        String originCurrency = scanner.nextLine().toUpperCase();

        System.out.println("Set your Destination Currency: (Ex: BRL)");
        String targetCurrency = scanner.nextLine().toUpperCase();

        double amountToConvert;
        do {
            System.out.println("Enter the value to be converted:");
            while (!scanner.hasNextDouble()) {
                System.out.println("Please enter a valid value:");
                scanner.next();
            }
            amountToConvert = scanner.nextDouble();
        } while (amountToConvert <= 0);

        double convertedAmount = convertCurrency(originCurrency, targetCurrency, amountToConvert);
        if (convertedAmount != -1) {
            System.out.printf("""
                    
                    %s %.2f is equivalent to %s %.2f
                    
                    """, originCurrency, amountToConvert, targetCurrency, convertedAmount);
        } else {
            System.out.println("Failed to perform conversion.");
        }
    }

    public static double convertCurrency(String originCurrency, String targetCurrency, double amount) {

        try {
            String uriSearch = "https://v6.exchangerate-api.com/v6/db7cd7944538c920970688f3/latest/" + originCurrency;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uriSearch))
                    .build();

            HttpResponse<String> response = client
                    .send(request, HttpResponse.BodyHandlers.ofString());
            String json = response.body();

            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parseReader(new StringReader(json));

            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                if (jsonObject.has("result") && jsonObject.get("result").getAsString().equals("success")) {
                    JsonObject ratesObject = jsonObject.getAsJsonObject("conversion_rates");

                    if (ratesObject.has(targetCurrency)) {
                        double exchangeRate = ratesObject.get(targetCurrency).getAsDouble();
                        return amount * exchangeRate;
                    } else {
                        System.out.println("Invalid destination currency.");
                        return -1;
                    }
                } else {
                    System.out.println("Failed to get conversion rates.");
                    return -1;
                }
            } else {
                System.out.println("Invalid JSON response.");
                return -1;
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error during HTTP request: " + e.getMessage());
            return -1;
        }
    }


    public static int getIntInput(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.println("Please enter an integer number:");
            scanner.next();
        }
        return scanner.nextInt();
    }
}
