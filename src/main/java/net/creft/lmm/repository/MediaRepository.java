package net.creft.lmm.repository;

import net.creft.lmm.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    Media findByMediaId(String mediaId);
}
