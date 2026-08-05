-- Clé d'idempotence optionnelle sur POST /api/invocation/global-invoque/{playerId} (header
-- Idempotency-Key), pour permettre au client de retenter un appel sans consommer un nouveau
-- tirage aléatoire ni déclencher une seconde invocation en cas de perte de la réponse.
ALTER TABLE invocation_buffer ADD COLUMN idempotency_key VARCHAR(255);

-- Unique uniquement quand renseignée : plusieurs entrées sans clé (NULL) restent autorisées.
CREATE UNIQUE INDEX idx_invocation_buffer_idempotency_key
    ON invocation_buffer (idempotency_key)
    WHERE idempotency_key IS NOT NULL;
