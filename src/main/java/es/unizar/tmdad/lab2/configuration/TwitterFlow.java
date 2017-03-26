package es.unizar.tmdad.lab2.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.social.twitter.api.Tweet;

import es.unizar.tmdad.lab2.domain.MyTweet;
import es.unizar.tmdad.lab2.domain.TargetedTweet;
import es.unizar.tmdad.lab2.domain.TweetBD;
import es.unizar.tmdad.lab2.domain.TweetBDRepository;
import es.unizar.tmdad.lab2.service.TwitterLookupService;

@Configuration
@EnableIntegration
@IntegrationComponentScan
@ComponentScan
public class TwitterFlow {
	
	private Tweet example = null;
	
	@Autowired
	private TwitterLookupService twitterlookupService;
	

	@Autowired
	private TweetBDRepository tweetRepository;
	
	@Bean
	public DirectChannel requestChannel() {
		return new DirectChannel();
	}


	// Tercer paso
	// Los mensajes se leen de "requestChannel" y se envian al método "sendTweet" del
	// componente "streamSendingService"
	@Bean
	public IntegrationFlow sendTweet() {
		
		return IntegrationFlows.from(requestChannel()).filter(tuit -> tuit instanceof Tweet).// Filter --> asegurarnos que el mensaje es un Tweet
				<Tweet,TargetedTweet>transform(tuit -> 
				{	MyTweet tweet = new MyTweet(tuit); 
					example = tuit;
					List<String> topics = twitterlookupService.getClaveSubscripciones().stream().filter(clave -> tweet.getText().contains((clave.split("-"))[0])).collect(Collectors.toList());
					//topics que coinciden con la query. ahora guardar el tweet 
					ArrayList<String> queries =  new ArrayList<String>();
					topics.forEach(topic -> {
						String nuevaQuery = topic.split("-")[0];
						if(!queries.contains(nuevaQuery))queries.add(nuevaQuery);
					});
					queries.forEach(query -> {
						tweetRepository.save(new TweetBD(query, tuit.getFromUser(), tuit.getIdStr(), tuit.getText()));
						System.out.println("query saved:" + query);
						System.out.println("Tweets en BD: " + tweetRepository.count());
						
					});
					return new TargetedTweet(tweet,topics); // Transform --> convertir un Tweet en un TargetedTweet con tantos tópicos como coincida
				}).split(TargetedTweet.class, tuit -> 
					{		List<TargetedTweet> tweets = new ArrayList<TargetedTweet>(tuit.getTargets().size());
							tuit.getTargets().forEach(q -> 
								{	tweets.add(new TargetedTweet(tuit.getTweet(),q));
								}); return tweets;// Split --> dividir un TargetedTweet con muchos tópicos en tantos TargetedTweet como tópicos haya
					})/*.<TargetedTweet,TargetedTweet>transform(tuit -> 
						{		tuit.getTweet().setUnmodifiedText(tuit.getTweet().getText().replace((tuit.getFirstTarget().split("-"))[0], "<b>" + (tuit.getFirstTarget().split("-"))[0] + "</b>"));
								return tuit;
						})*/.handle("streamSendingService", "sendTweet").get();// Transform --> señalar el contenido de un TargetedTweet
		
	}

}

// Segundo paso
// Los mensajes recibidos por este @MessagingGateway se dejan en el canal	"requestChannel"
@MessagingGateway(name = "integrationStreamListener", defaultRequestChannel = "requestChannel")
interface MyStreamListener extends StreamListener {

}
