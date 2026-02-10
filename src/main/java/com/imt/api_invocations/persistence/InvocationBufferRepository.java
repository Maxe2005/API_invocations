package com.imt.api_invocations.persistence;

import com.imt.api_invocations.enums.InvocationStatus;
import com.imt.api_invocations.persistence.dto.InvocationBufferDto;
import com.imt.api_invocations.persistence.repository.InvocationBufferJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvocationBufferRepository {

  private final InvocationBufferJpaRepository invocationBufferJpaRepository;

  public InvocationBufferDto save(InvocationBufferDto invocationBufferDto) {
    return invocationBufferJpaRepository.save(invocationBufferDto);
  }

  public InvocationBufferDto findById(String id) {
    return invocationBufferJpaRepository.findById(id).orElse(null);
  }

  public List<InvocationBufferDto> findRecreatable() {
    return invocationBufferJpaRepository.findByStatusIn(List.of(InvocationStatus.PENDING,
        InvocationStatus.MONSTER_CREATED, InvocationStatus.FAILED));
  }
}
