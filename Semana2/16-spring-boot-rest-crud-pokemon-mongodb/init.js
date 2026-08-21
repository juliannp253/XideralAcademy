db = db.getSiblingDB('pokemon_db');

db.createCollection('pokemon');

db.pokemon.insertMany([
    { name: 'Squirtle', type: 'Agua', level: 12 },
    { name: 'Gengar', type: 'Fantasma', level: 42 },
    { name: 'Snorlax', type: 'Normal', level: 30 }
]);