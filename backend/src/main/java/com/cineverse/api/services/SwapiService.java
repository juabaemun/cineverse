package com.cineverse.api.services;

import com.cineverse.api.entities.Movie;
import com.cineverse.api.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

@Service
public class SwapiService {

    @Autowired
    private MovieRepository movieRepository;

    // Mapa estático con los posters reales de cada película
    private static final Map<String, String> SW_POSTERS = Map.of(
            "A New Hope", "https://upload.wikimedia.org/wikipedia/en/8/87/StarWarsMoviePoster1977.jpg",
            "The Empire Strikes Back", "https://upload.wikimedia.org/wikipedia/en/3/3f/The_Empire_Strikes_Back_%281980_film%29.jpg",
            "Return of the Jedi", "https://upload.wikimedia.org/wikipedia/en/b/b2/Return_of_the_Jedi_poster1.jpg",
            "The Phantom Menace", "https://upload.wikimedia.org/wikipedia/en/4/40/Star_Wars_Phantom_Menace_poster.jpg",
            "Attack of the Clones", "https://upload.wikimedia.org/wikipedia/en/3/32/Star_Wars_-_Episode_II_Attack_of_the_Clones_%28movie_poster%29.jpg",
            "Revenge of the Sith", "https://upload.wikimedia.org/wikipedia/en/9/93/Star_Wars_Episode_III_Revenge_of_the_Sith_poster.jpg"
    );

    public void importStarWarsMovies() {
        try {
            RestTemplate restTemplate = createInsecureRestTemplate();
            String url = "https://swapi.dev/api/films/";

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");

            for (Map<String, Object> m : results) {
                String title = (String) m.get("title");

                if (!movieRepository.existsByTitle(title)) {
                    Movie movie = new Movie();
                    movie.setTitle(title);
                    movie.setSynopsis((String) m.get("opening_crawl"));

                    // 1. Solución Duración: Ponemos una media de 130 min
                    movie.setDuration(130);

                    // 2. Solución Imágenes: Buscamos en nuestro mapa o ponemos una genérica
                    String posterUrl = SW_POSTERS.getOrDefault(title,
                            "https://starwars-visualguide.com/assets/img/categories/films.jpg");
                    movie.setImageUrl(posterUrl);

                    movieRepository.save(movie);
                    System.out.println(">> Importada con éxito: " + title);
                }
            }
        } catch (Exception e) {
            System.err.println(">> Error en importación: " + e.getMessage());
        }
    }

    // Método mágico para saltarse la validación SSL de certificados caducados
    private RestTemplate createInsecureRestTemplate() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        return new RestTemplate();
    }
}