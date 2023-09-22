package com.cloudslip.pipeline.updated.repository;

import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.model.universal.Team;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends MongoRepository<Application, ObjectId> {

    List<Application> findAllByStatus(Status status);
    Page<Application> findAllByStatus(Pageable pageable, Status status);

    List<Application> findAllByTeamInAndStatus(List<Team> teamList, Status status);
    Page<Application> findAllByTeamInAndStatus(Pageable pageable,List<Team> teamList, Status status);

    List<Application> findAllByTeamIdInAndStatus(List<ObjectId> teamIdList, Status status);
    List<Application> findAllByTeamIdInAndStatus(Pageable pageable, List<ObjectId> teamIdList, Status status);

    Optional<Application> findByIdAndStatus(ObjectId objectId, Status status);
    Optional<Application> findByWebSocketTopicAndStatus(String webSocketTopic, Status status);

    List<Application> findAllByTeamId(ObjectId teamId);


    List<Application> findAllByNameIgnoreCaseAndStatus(String name, Status status);
    Optional<Application> findByGitInfoGitAppIdIgnoreCaseAndStatus(String id, Status status);
    Optional<Application> findByGitInfoGitAppIdIgnoreCaseAndStatusAndIdNotIn(String id, Status status, ObjectId appId);
    Optional<Application> findByNameIgnoreCaseAndStatusAndTeamOrganizationCompanyId(String name, Status status, ObjectId companyId);
    Optional<Application> findByNameIgnoreCaseAndStatusAndIdNotInAndTeamOrganizationCompanyId(String name, Status status, ObjectId appId, ObjectId companyId);
}
