package net.creft.lmm.repository;

import net.creft.lmm.model.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {

    MediaFile findByMediaFileId(String mediaFileId);

    List<MediaFile> findAllByMediaIdOrderByFileOrderAscIdAsc(String mediaId);

    List<MediaFile> findAllByMediaIdInOrderByMediaIdAscFileOrderAscIdAsc(Collection<String> mediaIds);
}
