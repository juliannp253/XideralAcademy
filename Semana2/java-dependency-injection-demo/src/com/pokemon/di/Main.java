package com.pokemon.di;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando aplicacion sin Spring (Inyeccion de Dependencias Manual)...\n");

        // Creamos la dependencia (Repositorio)
        PokemonRepository repository = new PokemonRepositoryImpl();

        // Inyectamos la dependencia en la clase Servicio a través de su constructor
        PokemonService service = new PokemonService(repository);

        // Uso del servicio
        service.displayAllPokemons();
        
        service.addPokemon("Bulbasaur", "Planta/Veneno");
        service.addPokemon("Squirtle", "Agua");

        System.out.println();
        service.displayAllPokemons();
    }
}
