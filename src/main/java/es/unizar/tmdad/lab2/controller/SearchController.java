package es.unizar.tmdad.lab2.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import dataBase.opsDatabase;
import es.unizar.tmdad.lab2.domain.TweetBD;
import es.unizar.tmdad.lab2.domain.TweetBDRepository;
import es.unizar.tmdad.lab2.service.TwitterLookupService;

@Controller
public class SearchController {

	@Autowired
	TwitterLookupService twitter;

	@Autowired
	JdbcTemplate jdbcTemplate;


	@Autowired
	private TweetBDRepository tweetRepository;
	
	@RequestMapping("/")
	public String greeting() {
		return "index";
	}

	@RequestMapping("/search")
	public ResponseEntity search(@RequestParam("q") String query, @RequestParam("restriccion") String restriccion,
			@RequestParam("dificultad") String dificultad, Model m) {
		System.out.println("Tweets en BD: " + tweetRepository.count());
		List <TweetBD> listaTweets = tweetRepository.findByQuery(query);
		System.out.println("lista: " + listaTweets.size());
		ArrayList <Tweet> tweets = new ArrayList <Tweet> (listaTweets.size());
		Tweet newTweet = null;/*
		for (int i=0; i<listaTweets.size(); i++){
			TweetBD tweet = listaTweets.get(i);
			newTweet = new Tweet(Long.getLong(tweet.getTweetId()), tweet.getTexto(), new Date(), tweet.getFromUser(), "", new Long(0), new Long(0), "", ""); 
			
			tweets.add(newTweet);	
		}*/

		return new ResponseEntity<>(listaTweets, HttpStatus.OK);

	}

	@MessageMapping("/search")
	public void search(String claveSubscripcion) {
		// twitter.search(query);
		String[] claves = claveSubscripcion.split("-");
		String q = claves[0];
		String dificultad = claves[1];
		String restriccion = claves[2];
		System.out.println("Query: " + q + ", dificultad: " + dificultad + ", restriccion: " + restriccion);
		opsDatabase ops = new opsDatabase(jdbcTemplate);
		// Si no esta la configuracion en la Bd, agregarla
		if (ops.getNumberConfigurations(q, dificultad, restriccion) < 1) {
			ops.addConfiguracion(q, dificultad, restriccion);
		}
		twitter.search(q, dificultad, restriccion);
	}
}