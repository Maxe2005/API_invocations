package com.imt.api_invocations.persistence.repository;

import com.imt.api_invocations.enums.InvocationStatus;
import com.imt.api_invocations.persistence.dto.InvocationBufferDto;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvocationBufferJpaRepository extends JpaRepository<InvocationBufferDto, String> {

    List<InvocationBufferDto> findByStatusIn(Collection<InvocationStatus> statuses);
}
