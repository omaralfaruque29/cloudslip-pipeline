package com.cloudslip.pipeline.updated.repository;

import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.model.AppSecret;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppSecretRepository extends MongoRepository<AppSecret, ObjectId> {

    public List<AppSecret> findAllByStatus(Status status);

    public Page<AppSecret> findAllByStatus(Pageable pageable, Status status);

    public List<AppSecret> findAllByCompanyIdAndStatus(ObjectId companyId, Status status);

    public List<AppSecret> findByCompanyIdAndApplicationIdAndUseAsEnvironmentVariableAndStatus(ObjectId companyId, ObjectId applicationId, boolean useAsEnvironmentVariable, Status status);

    public Page<AppSecret> findAllByCompanyIdAndStatus(Pageable pageable, ObjectId companyId, Status status);

    public List<AppSecret> findByCompanyIdAndApplicationIdAndStatus(ObjectId companyId, ObjectId applicationId, Status status);

    public Page<AppSecret> findByApplicationIdAndCompanyIdAndStatus(Pageable pageable, ObjectId applicationId, ObjectId companyId, Status status);

    public Optional<AppSecret> findByIdAndStatus(ObjectId id, Status status);

    public Optional<AppSecret> findByApplicationIdAndCompanyIdAndSecretNameAndStatus(ObjectId applicationId, ObjectId companyId,String secretName, Status status);
}
