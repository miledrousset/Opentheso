package fr.cnrs.opentheso.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;


@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user", nullable = false)
    private Integer id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "mail", nullable = false, unique = true)
    private String mail;

    @Column(name = "passtomodify", nullable = false)
    private Boolean passToModify = false;

    @Column(name = "alertmail", nullable = false)
    private Boolean alertMail = false;

    @Column(name = "issuperadmin", nullable = false)
    private Boolean isSuperAdmin = false;

    @Column(name = "apikey")
    private String apiKey;

    @Column(name = "key_never_expire", nullable = false)
    private Boolean keyNeverExpire = false;

    @Column(name = "key_expires_at")
    @Temporal(TemporalType.DATE)
    private LocalDate keyExpiresAt;

    @Column(name = "isservice_account", nullable = false)
    private Boolean isServiceAccount = false;

    @Column(name = "key_description")
    private String keyDescription;

    @OneToMany(mappedBy = "user")
    private List<CandidatMessages> candidatMessages;

    @OneToMany(mappedBy = "user")
    private List<CandidatStatus> candidatStatuses;

    @OneToMany(mappedBy = "userAdmin")
    private List<CandidatStatus> candidatStatusesAdmin;

    @OneToMany(mappedBy = "user")
    private List<CandidatVote> candidatVotes;

}
