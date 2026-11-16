package com.clashofbytes.service;

import com.clashofbytes.entity.CharacterEntity;
import com.clashofbytes.repository.CharacterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CharacterService {

    private final CharacterRepository characterRepository;

    @Autowired
    public CharacterService(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    public List<CharacterEntity> getAllCharacters() {
        return characterRepository.findAll();
    }

    public Optional<CharacterEntity> getCharacterById(Long id) {
        return characterRepository.findById(id);
    }
}
