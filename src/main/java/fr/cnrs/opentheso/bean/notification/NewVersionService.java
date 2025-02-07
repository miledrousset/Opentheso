package fr.cnrs.opentheso.bean.notification;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import fr.cnrs.opentheso.client.github.GitHubClient;
import fr.cnrs.opentheso.models.releases.ReleaseDto;
import fr.cnrs.opentheso.models.releases.TagDto;
import fr.cnrs.opentheso.repositories.ReleaseRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import fr.cnrs.opentheso.entites.Release;


@Slf4j
@Data
@ApplicationScoped
@Named(value = "newVersionBean")
public class NewVersionService implements Serializable {

    

    @Autowired
    private ReleaseRepository releaseRepository;

    private Release release;
    private boolean isAlreadyLoaded, newVersionExist;


    @PostConstruct
    public boolean searchNew() {
        if (!isAlreadyLoaded) {
            isAlreadyLoaded = true;
            searchNewVersion();
        }
        return newVersionExist;
    }

    public void searchNewVersion() {
        try {
            release = rechercherReleases();
            if (ObjectUtils.isEmpty(release)) {
                log.info("No new release found !");
                System.out.println("Aucune nouvelle release n'est trouvé !");
                newVersionExist = false;
            } else {
                log.info("New release found {} !", release.getVersion());
                System.out.println("Nouvelle release trouvé " + release.getVersion());
                newVersionExist = true;
            }
        } catch (IOException e) {
            log.error("Erreur dans lors de la recuperation des releases");
            newVersionExist = false;
        }
    }

    private Release rechercherReleases() throws IOException {

        List<TagDto> tags = new Gson().fromJson(GitHubClient.getResponse(GitHubClient.TAGS_API_URL),
                new TypeToken<List<TagDto>>() {}.getType());

        if (CollectionUtils.isEmpty(tags)) {
            log.error("No release found in GitHub server !");
            return null;
        }

        List<Release> releasesSaved = releaseRepository.getAllReleases();

        List<ReleaseDto> releases = new Gson().fromJson(GitHubClient.getResponse(GitHubClient.RELEASES_API_URL),
                new TypeToken<List<ReleaseDto>>() {}.getType());

        if (CollectionUtils.isEmpty(releasesSaved)) {
            log.info("First project running ! Saving previous releases");
            releaseRepository.saveAllReleases(releases.stream()
                    .map(this::toRelease)
                    .collect(Collectors.toList()));
            log.info("All releases are saved in DB !");
            return null;
        } else {
            log.info("Not of first project running ! searching of latest release");
            TagDto tag = tags.stream().findFirst().get();
            log.info("Latest tag version {}", tag.getName());
            Optional<Release> oldRelease = releasesSaved.stream()
                    .filter(releaseDto -> tag.getName().equalsIgnoreCase(releaseDto.getVersion()))
                    .findFirst();
            if (oldRelease.isPresent()) {
                log.info("We have already the last tag available {}", tag.getName());
                return null;
            } else {
                Optional<ReleaseDto> release = releases.stream()
                        .filter(releaseDto -> tag.getName().equalsIgnoreCase(releaseDto.getTag_name()))
                        .findFirst();
                if (release.isPresent()) {
                    var newRelease = toRelease(release.get());
                    releaseRepository.saveRelease(newRelease);
                    log.info("Save latest release in DB !");
                    return newRelease;
                } else {
                    log.error("No information found for tag {}", tag.getName());
                    return null;
                }
            }
        }
    }

    public Release toRelease(ReleaseDto releaseDto) {
        Release release = new Release();
        release.setVersion(releaseDto.getTag_name());
        release.setUrl(releaseDto.getHtml_url());
        release.setDate(toLocalDate(releaseDto.getPublished_at()));
        release.setDescription(releaseDto.getBody());
        return release;
    }

    private LocalDate toLocalDate(String date) {
        if (StringUtils.isNotEmpty(date)) {
            Instant instant = Instant.parse(date);
            return instant.atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
            return null;
        }
    }

    public String getFormatUrl() {
        return "window.open('" + release.getUrl() + "', '_blank');";
    }

    public String formatVersion() {
        if (ObjectUtils.isNotEmpty(release)) {
            return "Opentheso-" + getTag();
        } else {
            return "";
        }
    }

    public void viewDescription() throws IOException {
        if (ObjectUtils.isNotEmpty(release)) {
            FacesContext.getCurrentInstance().getExternalContext().redirect(release.getUrl());
        }
    }

    public String getTag() {
        if (ObjectUtils.isNotEmpty(release)) {
            return release.getVersion().replace("v", "");
        } else {
            return "";
        }
    }
    public boolean newVersionFound(){
        return newVersionExist;
    }
}
