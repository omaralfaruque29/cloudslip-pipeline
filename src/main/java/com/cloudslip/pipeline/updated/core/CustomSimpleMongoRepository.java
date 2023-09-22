package com.cloudslip.pipeline.updated.core;

import com.cloudslip.pipeline.updated.enums.Status;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.data.util.StreamUtils;
import org.springframework.data.util.Streamable;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CustomSimpleMongoRepository<T, ID> extends SimpleMongoRepository<T, ID> {

    private final MongoOperations mongoOperations;
    private final MongoEntityInformation<T, ID> entityInformation;

    public CustomSimpleMongoRepository(MongoEntityInformation<T, ID> metadata, MongoOperations mongoOperations) {
        super(metadata, mongoOperations);
        this.mongoOperations = mongoOperations;
        this.entityInformation = metadata;
    }

    public List<T> findAll() {
        Query query = new Query();
        query.addCriteria(Criteria.where("status").is(Status.V));
        return this.findAll(query);
    }

    public Iterable<T> findAllById(Iterable<ID> ids) {
        Query query = new Query();
        query.addCriteria(Criteria.where("status").is(Status.V)).addCriteria((new Criteria(this.entityInformation.getIdAttribute())).in((Collection) Streamable.of(ids).stream().collect(StreamUtils.toUnmodifiableList())));
        return this.findAll(query);
    }

    public Page<T> findAll(Pageable pageable) {
        Assert.notNull(pageable, "Pageable must not be null!");
        Long count = this.count();
        Query query = new Query();
        query.addCriteria(Criteria.where("status").is(Status.V));
        List<T> list = this.findAll(query.with(pageable));
        return new PageImpl(list, pageable, count.longValue());
    }

    public List<T> findAll(Sort sort) {
        Assert.notNull(sort, "Sort must not be null!");
        Query query = new Query();
        query.addCriteria(Criteria.where("status").is(Status.V));
        return this.findAll(query.with(sort));
    }

    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        Assert.notNull(example, "Sample must not be null!");
        Assert.notNull(pageable, "Pageable must not be null!");

        Query q = new Query();
        q.addCriteria(Criteria.where("status").is(Status.V)).addCriteria((new Criteria()).alike(example)).with(pageable);

        List<S> list = this.mongoOperations.find(q, example.getProbeType(), this.entityInformation.getCollectionName());
        return PageableExecutionUtils.getPage(list, pageable, () -> {
            return this.mongoOperations.count(q, example.getProbeType(), this.entityInformation.getCollectionName());
        });
    }

    public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
        Assert.notNull(example, "Sample must not be null!");
        Assert.notNull(sort, "Sort must not be null!");

        Query q = new Query();
        q.addCriteria(Criteria.where("status").is(Status.V)).addCriteria((new Criteria()).alike(example)).with(sort);

        return this.mongoOperations.find(q, example.getProbeType(), this.entityInformation.getCollectionName());
    }

    public <S extends T> List<S> findAll(Example<S> example) {
        return this.findAll(example, Sort.unsorted());
    }

    public Optional<T> findById(ID id) {
        Assert.notNull(id, "The given id must not be null!");
        return Optional.ofNullable(this.mongoOperations.findById(id, this.entityInformation.getJavaType(), this.entityInformation.getCollectionName()));
    }

    public void delete(T entity) {
        Assert.notNull(entity, "The given entity must not be null!");
        try {
            Field status = entity.getClass().getSuperclass().getDeclaredField("status");
            status.setAccessible(true);
            status.set(entity, Status.D);
            this.save(entity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            this.deleteById(this.entityInformation.getRequiredId(entity));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            this.deleteById(this.entityInformation.getRequiredId(entity));
        }
    }

    private List<T> findAll(@Nullable Query query) {
        return query == null ? Collections.emptyList() : this.mongoOperations.find(query, this.entityInformation.getJavaType(), this.entityInformation.getCollectionName());
    }

}
