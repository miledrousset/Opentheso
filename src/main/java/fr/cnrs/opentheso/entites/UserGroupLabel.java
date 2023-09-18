package fr.cnrs.opentheso.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;


@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_group_label")
public class UserGroupLabel implements Serializable {

    @Id
    @Column(name = "id_group")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "label_group")
    private String label;

}
