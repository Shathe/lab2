package es.unizar.tmdad.lab2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.social.twitter.api.FilterStreamParameters;
import org.springframework.social.twitter.api.Stream;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import dataBase.opsDatabase;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import es.unizar.tmdad.lab2.domain.TargetedTweet;

@Service
public class StreamSendingService {
	

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
	
	@PostConstruct
	public void initialize() {
		FilterStreamParameters fsp = new FilterStreamParameters();
		//fsp.addLocation(-180, -90, 180, 90);
		// Primer paso
		// Registro un gateway para recibir los mensajes
		// Ver @MessagingGateway en MyStreamListener en TwitterFlow.java
		stream = twitterTemplate.streamingOperations().sample(Collections.singletonList(integrationStreamListener));
	}

	// Cuarto paso
	// Recibe un tweet y hay que enviarlo a tantos canales como preguntas hay registradas en lookupService
	//
	public void sendTweet(Tweet tweet) {
		Map<String, Object> map = new HashMap<>();
		map.put(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON);

		// Expresión lambda: si el tweet contiene s, devuelve true
		Predicate<String> containsTopic = query -> tweet.getText().contains("-" + query + "-");
		// Expresión lambda: envia un tweet al canal asociado al tópico s
		Consumer<String> convertAndSend = s -> {ops.convertAndSend("/queue/search/" + s, tweet, map);
		opsDatabase opsDB = new opsDatabase(jdbcTemplate);
		Long idConf = opsDB.getIdConfiguracion((s).split("-")[0], 
				(s.split("-"))[1], (s.split("-"))[2]);
				opsDB.addTweet(tweet.getText(), idConf);
				System.out.println("insertado idconfig: " + idConf);

		};
		System.out.println("sendTweet Tweet: " + tweet.getText());
		
		lookupService.getClaveSubscripciones().stream().filter(containsTopic).forEach(convertAndSend);
	}

	public void sendTweet(TargetedTweet tweet) {
		//
		// CAMBIOS A REALIZAR:
		//
		// Crea un mensaje que envie un tweet a un único tópico destinatario
		//
		Map<String, Object> mapa = new HashMap<>();
		mapa.put(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON);
		ops.convertAndSend("/queue/search/" + tweet.getFirstTarget(),
				tweet.getTweet(),mapa);
		System.out.println("sendTweet TargetedTweet: " + tweet.getTweet().getText());
		opsDatabase opsDB = new opsDatabase(jdbcTemplate);
		Long idConf = opsDB.getIdConfiguracion((tweet.getFirstTarget()).split("-")[0], 
				(tweet.getFirstTarget().split("-"))[1], (tweet.getFirstTarget().split("-"))[2]);
		opsDB.addTweet(tweet.getTweet().getText(), idConf);
		System.out.println("insertado idconfig: " + idConf);


	}


	public Stream getStream() {
		return stream;
	}

}
