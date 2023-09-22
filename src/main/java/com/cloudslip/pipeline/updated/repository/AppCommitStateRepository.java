package com.cloudslip.pipeline.updated.repository;

import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.model.AppCommitState;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppCommitStateRepository extends MongoRepository<AppCommitState, ObjectId> {

    List<AppCommitState> findTop20ByAppCommitApplicationIdAndStatusOrderByAppCommit_CommitDateDesc(ObjectId applicationId, Status status);

    Optional<AppCommitState> findByAppCommit_IdAndStatus(ObjectId appCommitId, Status status);

    Optional<AppCommitState> findByAppCommit_GitCommitIdAndStatus(String gitCommitId, Status status);

    Optional<AppCommitState> findByAppCommit_ApplicationIdAndAppCommit_GitCommitIdAndStatus(ObjectId applicationId, String gitCommitId,  Status status);

    Optional<AppCommitState> findFirstByAppCommitApplicationIdAndStatusOrderByAppCommit_CommitDateDesc(ObjectId applicationId, Status status);

    List<AppCommitState> findAllByAppCommitApplicationIdAndStatus(ObjectId appId, Status status);

    @Query(value = "{ 'environmentStateList' : {'$elemMatch' : {'appEnvironment._id' : ?0}}, 'status._' : ?1}")
    List<AppCommitState> findAllByEnvironmentStateListAppEnvironmentIdAndStatus(ObjectId appEnvironmentId, Status status);

    @Query(value = "{ 'environmentStateList' : {'$elemMatch' : {'appEnvironment._id' : ?0, 'checkList.checkItem._id' : ?1}}, 'status._' : ?2}")
    List<AppCommitState> findAllByEnvironmentStateListAppEnvironmentIdAndCheckListCheckItemIdAndStatus(ObjectId appEnvironmentId, ObjectId checkItemId, Status status);

    @Query(value = "{ '_id' : ?0 ,'environmentStateList' : {'$elemMatch' : {'appEnvironment._id' : ?1 }}, 'status._' : ?2}")
    Optional<AppCommitState> findByIdAndEnvironmentStateListAppEnvironmentIdAndStatus(ObjectId appCommitStateId, ObjectId appEnvId, Status status);

    @Query(value = "{ 'environmentStateList.steps._id' :  ?0, 'status._' : ?1}")
    Optional<AppCommitState> findByEnvironmentStateListAppCommitPipelineStep_IdAndStatus(ObjectId appCommitPipelineStepId, Status status);
}
