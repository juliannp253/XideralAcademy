package com.pokemon.di;

import java.util.ArrayList;
import java.util.List;

public class PokemonRepositoryImpl implements PokemonRepository {
    private List<Pokemon> pokemons = new ArrayList<>();

    public PokemonRepositoryImpl() {
        pokemons.add(new Pokemon("Pikachu", "Electrico"));
        pokemons.add(new Pokemon("Charmander", "Fuego"));
    }

    @Override
    public List<Pokemon> findAll() {
        return pokemons;
    }

    @Override
    public void save(Pokemon pokemon) {
        pokemons.add(pokemon);
        System.out.println("Pokemon guardado en la base de datos (en memoria): " + pokemon.getName());
    }
}
