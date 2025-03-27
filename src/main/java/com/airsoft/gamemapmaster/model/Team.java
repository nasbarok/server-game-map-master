package com.airsoft.gamemapmaster.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "teams")
public class Team {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    private String color;
    
    @ManyToOne
    @JoinColumn(name = "leader_id")
    private User leader;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "team_members",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    // Ajouter une méthode pour synchroniser les membres
    public void syncMembers(Set<User> newMembers) {
        this.members.clear();
        this.members.addAll(newMembers);
    }

    @ManyToOne
    @JoinColumn(name = "game_map_id")
    private GameMap gameMap;
}
