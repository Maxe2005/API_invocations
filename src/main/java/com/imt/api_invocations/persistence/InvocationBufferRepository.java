package com.imt.api_invocations.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.imt.api_invocations.enums.InvocationStatus;
import com.imt.api_invocations.persistence.dao.InvocationBufferMongoDao;
import com.imt.api_invocations.persistence.dto.InvocationBufferDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InvocationBufferRepository {

    private final InvocationBufferMongoDao invocationBufferMongoDao;

    public InvocationBufferDto save(InvocationBufferDto invocationBufferDto) {
        return invocationBufferMongoDao.save(invocationBufferDto);
    }

    public InvocationBufferDto findById(String id) {
        return invocationBufferMongoDao.findById(id).orElse(null);
    }

    public List<InvocationBufferDto> findRecreatable() {
        return invocationBufferMongoDao.findByStatusIn(
                List.of(InvocationStatus.PENDING, InvocationStatus.MONSTER_CREATED, InvocationStatus.FAILED));
    }

    public void deleteAll() {
        invocationBufferMongoDao.deleteAll();
    }

    public long count() {
        return invocationBufferMongoDao.count();
    }
}
