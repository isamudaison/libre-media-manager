package net.creft.lmm.repository;

import net.creft.lmm.model.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    Media findByMediaId(String mediaId);

    Page<Media> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
