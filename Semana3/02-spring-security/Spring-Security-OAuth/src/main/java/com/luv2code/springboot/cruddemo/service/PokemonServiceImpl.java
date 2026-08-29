package com.luv2code.springboot.cruddemo.service;

import com.luv2code.springboot.cruddemo.dao.PokemonRepository;
import com.luv2code.springboot.cruddemo.entity.Pokemon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PokemonServiceImpl implements PokemonService {

    /* Ya no se necesitan, Spring Data JPA realiza esto
    private PokemonRepository pokemonRepository;

    @Autowired
    public PokemonServiceImpl(PokemonRepository thePokemonRepository) {
        pokemonRepository = thePokemonRepository;
    }

    @Override
    public List<Pokemon> findAll() {
        return pokemonRepository.findAll();
    }

    @Override
    public Pokemon findById(int theId) {
        Optional<Pokemon> result = pokemonRepository.findById(theId);

        Pokemon thePokemon = null;

        if (result.isPresent()) {
            thePokemon = result.get();
        }
        else {
            // we didn't find the pokemon
            throw new RuntimeException("Did not find pokemon id - " + theId);
        }

        return thePokemon;
    }

    @Override
    public Pokemon save(Pokemon theEmployee) {
        return pokemonRepository.save(theEmployee);
    }

    @Override
    public void deleteById(int theId) {
        pokemonRepository.deleteById(theId);
    }*/
}






