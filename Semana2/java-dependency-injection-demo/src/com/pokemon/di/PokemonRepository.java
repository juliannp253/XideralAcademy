package com.pokemon.di;

import java.util.List;

// Interfaz que define el contrato para el acceso a datos.
public interface PokemonRepository {
    List<Pokemon> findAll();
    void save(Pokemon pokemon);
}
