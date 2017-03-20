package es.unizar.tmdad.lab2.configuration;

public class Configuracion {
	private String query, dificultad, restriccion;
	
	public Configuracion(String query, String dificultad, String restriccion) {
		super();
		this.query = query;
		this.dificultad = dificultad;
		this.restriccion = restriccion;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getDificultad() {
		return dificultad;
	}

	public void setDificultad(String dificultad) {
		this.dificultad = dificultad;
	}

	public String getRestriccion() {
		return restriccion;
	}

	public void setRestriccion(String restriccion) {
		this.restriccion = restriccion;
	}
	
	@Override
	public String toString(){
		return query + "-" + dificultad+ "-" + restriccion;
	}
}
