package es.unizar.tmdad.lab2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.social.twitter.api.FilterStreamParameters;
import org.springframework.social.twitter.api.Stream;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

import org.springframework.social.twitter.api.StreamDeleteEvent;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.social.twitter.api.StreamWarningEvent;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestTemplate;

import dataBase.opsDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import es.unizar.tmdad.lab2.domain.TargetedTweet;

@Service
public class StreamSendingServiceDificultad {

	@Autowired
	private SimpMessageSendingOperations ops;

	@Autowired
	private TwitterTemplate twitterTemplate;

	@Autowired
	private TwitterLookupService lookupService;

	private Stream stream;

	@Autowired
	private StreamListener integrationStreamListener;

	@Autowired
	JdbcTemplate jdbcTemplate;

	public void sendTweet(TargetedTweet tweet) {
		// Crea un mensaje que envie un tweet a un único tópico destinatario

		Map<String, Object> mapa = new HashMap<>();
		mapa.put(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON);
		String[] claves = tweet.getFirstTarget().split("-");
		String query = claves[0];
		String dificultad = claves[1];
		String restriccion = claves[2];
		boolean aceptado = false;
		String[] palabras = tweet.getTweet().getText().split(" ");
		ArrayList<String> palabrasValidas = new ArrayList<String>();
		for (int i = 0; i < palabras.length; i++) {
			if (!palabras[i].contains("@") && !palabras[i].contains("#") && !palabras[i].contains("http"))
				palabrasValidas.add(palabras[i]);
		}
		boolean sinonimo = false;
		RestTemplate restTemplate = new RestTemplate();

		
		switch (dificultad) {
		case "facil":
			// cambiar palabra al azar por otra al azar
			String[] palabrasArecomendar = { "a", "no", "ah", "ehmmmm", "" };
			int aletarorioDiccionario = (int) (Math.random() * palabrasArecomendar.length);
			int aletaroriopalabrasValidas = (int) (Math.random() * palabrasValidas.size());
			tweet.getTweet().setUnmodifiedText(tweet.getTweet().getUnmodifiedText().replaceFirst(
					 " " + palabrasValidas.get(aletaroriopalabrasValidas) + " ", " "+ palabrasArecomendar[aletarorioDiccionario] +  " "));

			break;
		case "medio":
			// usar una api de sinonimos y cambiar una palabra al azar
			// desd ela primera palabra a la ultima palabra intentar obtener un
			// sinonimo de las palabras importantes

		
			for (int i = 0; i < palabrasValidas.size() && !sinonimo; i++) {
				int j = (int) (Math.random() * palabrasValidas.size());
				try {
					ResponseEntity<String> response = restTemplate
							.getForEntity("http://words.bighugelabs.com/api/2/c302f07e3593264f58a7366800330462/"
									+ palabrasValidas.get(j) + "/json", String.class);
					String body = response.getBody();
					// a partir de la posicion 5 estan los resultados
					// se anaden dos sugerencias si la api ha devuelto
					// resultados
					// son en posiciones impares (en las pares hay comas)
					String sugerencias[] = body.split("\"");
					if (sugerencias.length > (5)) {
						String sinonimo_recomendado = sugerencias[5];
						sinonimo = true;
						tweet.getTweet().setUnmodifiedText(tweet.getTweet().getUnmodifiedText()
								.replaceFirst(" " + palabrasValidas.get(j) + " ",  " " + sinonimo_recomendado + " "));

					}

				} catch (Exception e) {
				}
			}
			break;
		case "dificil":
			// desde la palabra mas larga hasta la mas pequena, de las palabras
			// importantes

			palabrasValidas.sort(new Comparator<String>() {
				@Override
				public int compare(String a, String b) {

					return b.length() - a.length();
				}
			});
			System.out.println(palabrasValidas.get(0));
			System.out.println(palabrasValidas.get(4));
			for (int i = 0; i < palabrasValidas.size() && !sinonimo; i++) {
				try {
					ResponseEntity<String> response = restTemplate
							.getForEntity("http://words.bighugelabs.com/api/2/c302f07e3593264f58a7366800330462/"
									+ palabrasValidas.get(i) + "/json", String.class);
					String body = response.getBody();
					// a partir de la posicion 5 estan los resultados
					// se anaden dos sugerencias si la api ha devuelto
					// resultados
					// son en posiciones impares (en las pares hay comas)
					String sugerencias[] = body.split("\"");
					if (sugerencias.length > (5)) {
						String sinonimo_recomendado = sugerencias[5];
						sinonimo = true;
						tweet.getTweet().setUnmodifiedText(tweet.getTweet().getUnmodifiedText()
								.replaceFirst( " " + palabrasValidas.get(i) + " ", " " + sinonimo_recomendado +  " "));

					}

				} catch (Exception e) {
				}
			}

			break;

		default:
			;
			break;
		}

		ops.convertAndSend("/queue/search/" + tweet.getFirstTarget(), tweet.getTweet(), mapa);
		System.out.println("sendTweet TargetedTweet: " + tweet.getTweet().getText());
		opsDatabase opsDB = new opsDatabase(jdbcTemplate);
		Long idConf = opsDB.getIdConfiguracion((tweet.getFirstTarget()).split("-")[0],
				(tweet.getFirstTarget().split("-"))[1], (tweet.getFirstTarget().split("-"))[2]);
		opsDB.addTweet(tweet.getTweet().getText(), idConf);
		System.out.println("insertado idconfig: " + idConf);

	}

}
