package es.unizar.tmdad.lab2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import dataBase.opsDatabase;
import es.unizar.tmdad.lab2.domain.TweetBDRepository;

/**
 * Server application
 */

@Component
@SpringBootApplication
@EnableOAuth2Sso
public class Application implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String args[]) {
		SpringApplication.run(Application.class, args);
		
	}
	

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	private TweetBDRepository tweetRepository;
	
	@Override
	public void run(String... strings) throws Exception {

		log.info("Creating tables");
		// Tabla de usuarios
		jdbcTemplate.execute("CREATE TABLE Configuracion("
				+ "id SERIAL, query VARCHAR(100), juego VARCHAR(100), restriccion VARCHAR(100))");

		jdbcTemplate
				.execute("CREATE TABLE Tweets(id SERIAL, idConfiguracion long, tweet VARCHAR(300))");

		


		//opsDatabase ops = new opsDatabase(jdbcTemplate);
		//ops.addConfiguracion("hola", "facil", "normal");


	}
}