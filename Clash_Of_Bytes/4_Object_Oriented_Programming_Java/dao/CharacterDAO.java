package dao;

import database.DBConnection;
import model.Character;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for selectable Fighter Characters.
 */
public class CharacterDAO {

    public List<Character> getAllCharacters() throws SQLException {
        List<Character> list = new ArrayList<>();
        String sql = "SELECT character_id, character_name, character_description, archetype, base_hp, base_attack, base_defense, speed "
                   + "FROM characters ORDER BY character_id ASC;";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToCharacter(rs));
            }
        }
        return list;
    }

    public Character getCharacterById(int characterId) throws SQLException {
        String sql = "SELECT character_id, character_name, character_description, archetype, base_hp, base_attack, base_defense, speed "
                   + "FROM characters WHERE character_id = ?;";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, characterId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToCharacter(rs);
                }
            }
        }
        return null;
    }

    public Character getCharacterByName(String name) throws SQLException {
        String sql = "SELECT character_id, character_name, character_description, archetype, base_hp, base_attack, base_defense, speed "
                   + "FROM characters WHERE LOWER(character_name) = LOWER(?);";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToCharacter(rs);
                }
            }
        }
        return null;
    }

    private Character mapRowToCharacter(ResultSet rs) throws SQLException {
        return new Character(
                rs.getInt("character_id"),
                rs.getString("character_name"),
                rs.getString("character_description"),
                rs.getString("archetype"),
                rs.getDouble("base_hp"),
                rs.getDouble("base_attack"),
                rs.getDouble("base_defense"),
                rs.getDouble("speed")
        );
    }
}
