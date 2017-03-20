package es.unizar.tmdad.lab2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import dataBase.opsDatabase;
import es.unizar.tmdad.lab2.service.TwitterLookupService;

@Controller
public class SearchController {

    @Autowired
    TwitterLookupService twitter;

	@Autowired
	JdbcTemplate jdbcTemplate;
	
    @RequestMapping("/")
    public String greeting() {
        return "index";
    }
    
    @MessageMapping("/search")
	public void search(String claveSubscripcion) {
		//twitter.search(query);
		String[] claves = claveSubscripcion.split("-");
		String q = claves[0];
		String dificultad =claves[1];
		String restriccion = claves[2];
		System.out.println("Query: " + q +", dificultad: " + dificultad +", restriccion: " + restriccion);
		opsDatabase ops = new opsDatabase(jdbcTemplate);
		// Si no esta la configuracion en la Bd, agregarla
		if (ops.getNumberConfigurations(q, dificultad, restriccion) < 1 ){
			ops.addConfiguracion(q, dificultad, restriccion);
		}
		twitter.search(q, dificultad, restriccion);
	}
}