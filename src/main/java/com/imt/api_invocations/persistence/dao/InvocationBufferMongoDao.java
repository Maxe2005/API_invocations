package com.imt.api_invocations.persistence.dao;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.imt.api_invocations.enums.InvocationStatus;
import com.imt.api_invocations.persistence.dto.InvocationBufferDto;

public interface InvocationBufferMongoDao extends JpaRepository<InvocationBufferDto, String> {

    List<InvocationBufferDto> findByStatusIn(Collection<InvocationStatus> statuses);
}
