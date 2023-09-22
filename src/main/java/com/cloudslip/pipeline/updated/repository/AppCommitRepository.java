package com.cloudslip.pipeline.updated.repository;

import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.model.AppCommit;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppCommitRepository extends MongoRepository<AppCommit, ObjectId> {

    Optional<AppCommit> findByApplicationIdAndGitCommitIdAndStatus(ObjectId applicationId, String commitId, Status status);

    Optional<AppCommit> findByIdAndStatus(ObjectId id, Status status);

    List<AppCommit> findAllByApplicationIdAndStatus(ObjectId applicationId, Status status);

    Integer countAllByApplicationIdAndStatus(ObjectId applicationId, Status status);
    Optional<AppCommit> findTopByApplicationIdAndStatusAndCommitDateLessThanOrderByCommitDateDesc(ObjectId applicationId, Status status, Date appCommitDate);
}
