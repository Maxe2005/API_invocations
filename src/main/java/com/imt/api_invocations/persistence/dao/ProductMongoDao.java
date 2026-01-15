package com.imt.api_invocations.persistence.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.imt.api_invocations.persistence.dto.ProductMongoDto;

import java.util.UUID;

public interface ProductMongoDao extends MongoRepository<ProductMongoDto, UUID> {

}
