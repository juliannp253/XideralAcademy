package com.pokemon.di;

import java.util.List;

// Depende de la interfaz PokemonRepository, NO de una clase concreta.

public class PokemonService {
    
    // Dependencia
    private final PokemonRepository pokemonRepository;

    public PokemonService(PokemonRepository pokemonRepository) {
        this.pokemonRepository = pokemonRepository;
    }

    public void displayAllPokemons() {
        List<Pokemon> pokemons = pokemonRepository.findAll();
        System.out.println("--- Lista de Pokemons ---");
        for (Pokemon p : pokemons) {
            System.out.println(p);
        }
        System.out.println("-------------------------");
    }

    public void addPokemon(String name, String type) {
        Pokemon newPokemon = new Pokemon(name, type);
        pokemonRepository.save(newPokemon);
    }
}
