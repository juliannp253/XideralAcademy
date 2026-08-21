package com.luv2code.springboot.cruddemo.rest;

import com.luv2code.springboot.cruddemo.dao.PokemonRepository;
import com.luv2code.springboot.cruddemo.service.PokemonService;
import tools.jackson.databind.json.JsonMapper;
import com.luv2code.springboot.cruddemo.entity.Pokemon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class PokemonRestController {

    private PokemonService pokemonService;
    private final PokemonRepository pokemonRepository;

    private JsonMapper jsonMapper;

    @Autowired
    public PokemonRestController(PokemonService thePokemonService, PokemonRepository pokemonRepository, JsonMapper theJsonMapper) {
        this.pokemonService = thePokemonService;
        this.pokemonRepository = pokemonRepository;
        this.jsonMapper = theJsonMapper;
    }

    // expose "/pokemon" and return a list of employees
    @GetMapping("/pokemons")
    public List<Pokemon> findAll() {
        return pokemonRepository.findAll();
    }

    // add mapping for GET /pokemons/{pokemonId}

    @GetMapping("/pokemons/{pokemonId}")
    public Pokemon getPokemon(@PathVariable int pokemonId) {

        Pokemon thePokemon = pokemonRepository.findById(pokemonId)
                .orElseThrow(() -> new RuntimeException("Pokemon id not found - " + pokemonId));

        return thePokemon;
    }

    // add mapping for POST /pokemon - add new pokemon

    @PostMapping("/pokemons")
    public Pokemon addPokemon(@RequestBody Pokemon thePokemon) {

        // also just in case they pass an id in JSON ... set id to 0
        // this is to force a save of new item ... instead of update

        thePokemon.setId(0);

        Pokemon dbPokemon = pokemonRepository.save(thePokemon);

        return dbPokemon;
    }

    // add mapping for PUT /pokemons - update existing pokemon

    @PutMapping("/pokemons")
    public Pokemon updatePokemon(@RequestBody Pokemon thePokemon) {

        Pokemon dbPokemon = pokemonRepository.save(thePokemon);

        return dbPokemon;
    }

    // add mapping for PATCH /pokemons/{pokemonId} - patch pokemon ... partial
    // update

    @PatchMapping("/pokemons/{pokemonId}")
    public Pokemon patchPokemon(@PathVariable int pokemonId,
            @RequestBody Map<String, Object> patchPayload) {

        // Step 1: Retrieve the existing employee from database
        Pokemon tempPokemon = pokemonRepository.findById(pokemonId)
                .orElseThrow(() -> new RuntimeException("Pokemon id not found - " + pokemonId));

        // Step 2: Security check - prevent ID modifications
        // The ID should never change, so reject any attempts to modify it
        if (patchPayload.containsKey("id")) {
            throw new RuntimeException(
                    "Pokemon id cannot be modified. Remove 'id' from request body.");
        }

        // Step 3: Apply the partial update
        // This creates a NEW pokemon object with the updates applied
        Pokemon patchedPokemon = jsonMapper.updateValue(tempPokemon, patchPayload);

        // Step 4: Save the updated pokemon to database and return it
        Pokemon dbPokemon = pokemonRepository.save(patchedPokemon);

        return dbPokemon;
    }

    // add mapping for DELETE /pokemons/{pokemonId} - delete pokemon

    @DeleteMapping("/pokemons/{pokemonId}")
    public String deletePokemon(@PathVariable int pokemonId) {

        Pokemon tempPokemon = pokemonRepository.findById(pokemonId)
                .orElseThrow(() -> new RuntimeException("Pokemon id not found - " + pokemonId));

        pokemonRepository.deleteById(pokemonId);

        return "Deleted pokemon id - " + pokemonId;
    }

}
