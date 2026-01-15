package com.imt.api_invocations.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.imt.api_invocations.persistence.dao.ProductMongoDao;
import com.imt.api_invocations.persistence.dto.ProductMongoDto;
import com.imt.api_invocations.service.port.ProductPort;

import static java.util.UUID.randomUUID;

@Component
@RequiredArgsConstructor
public class ProductRepository implements ProductPort {

    private final ProductMongoDao productMongoDao;

    public String save(String name, double price) {
        var productMongoDto = new ProductMongoDto(
                randomUUID(),
                name,
                price);

        ProductMongoDto savedProductDto = productMongoDao.save(productMongoDto);

        return savedProductDto.getId().toString();
    }

}
