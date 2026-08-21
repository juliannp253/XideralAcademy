package com.luv2code.springboot.cruddemo.rest;

import com.luv2code.springboot.cruddemo.repository.PokemonRepository;
import tools.jackson.databind.json.JsonMapper;
import com.luv2code.springboot.cruddemo.entity.Pokemon;
import com.luv2code.springboot.cruddemo.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PokemonRestController {

    private EmployeeService employeeService;
    private final PokemonRepository pokemonRepository;

    private JsonMapper jsonMapper;

    @Autowired
    public PokemonRestController(EmployeeService theEmployeeService, PokemonRepository pokemonRepository, JsonMapper theJsonMapper) {
        this.employeeService = theEmployeeService;
        this.pokemonRepository = pokemonRepository;
        this.jsonMapper = theJsonMapper;
    }

    // expose "/pokemons" and return a list of pokemons
    @GetMapping("/pokemons")
    public List<Pokemon> findAll() {
        return pokemonRepository.findAll();
    }

    // add mapping for GET /pokemons/{pokemonId}
    @GetMapping("/pokemons/{pokemonId}")
    public Pokemon getPokemon(@PathVariable String pokemonId) {

        Pokemon thePokemon = pokemonRepository.findById(pokemonId)
                .orElseThrow(() -> new RuntimeException("Pokemon id not found - " + pokemonId));

        return thePokemon;
    }

    // add mapping for POST /pokemon - add new pokemon
    @PostMapping("/pokemons")
    public Pokemon addPokemon(@RequestBody Pokemon thePokemon) {
        thePokemon.setId(null);

        Pokemon dbPokemon = pokemonRepository.save(thePokemon);

        return dbPokemon;
    }

    // add mapping for PUT /pokemons - update existing pokemon
    @PutMapping("/pokemons")
    public Pokemon updatePokemon(@RequestBody Pokemon thePokemon) {

        Pokemon dbPokemon = pokemonRepository.save(thePokemon);

        return dbPokemon;
    }

    // add mapping for PATCH /employees/{employeeId} - patch employee ... partial
    @PatchMapping("/pokemons/{pokemonId}")
    public Pokemon patchPokemon(@PathVariable String pokemonId,
                                @RequestBody Map<String, Object> patchPayload) {

        Pokemon tempPokemon = pokemonRepository.findById(pokemonId)
                .orElseThrow(() -> new RuntimeException("Pokemon id not found - " + pokemonId));

        if (patchPayload.containsKey("id")) {
            throw new RuntimeException(
                    "Pokemon id cannot be modified. Remove 'id' from request body.");
        }

        Pokemon patchedPokemon = jsonMapper.updateValue(tempPokemon, patchPayload);
        Pokemon dbPokemon = pokemonRepository.save(patchedPokemon);

        return dbPokemon;
    }

    // add mapping for DELETE /pokemons/{pokemonId} - delete pokemon
    @DeleteMapping("/pokemons/{pokemonId}")
    public String deletePokemon(@PathVariable String pokemonId) {

        Pokemon tempPokemon = pokemonRepository.findById(pokemonId)
                .orElseThrow(() -> new RuntimeException("Pokemon id not found - " + pokemonId));

        pokemonRepository.deleteById(pokemonId);

        return "Deleted pokemon id - " + pokemonId;
    }

}
