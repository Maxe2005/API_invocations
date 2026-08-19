-- Identifiant de correlation X-Request-Id persisté sur chaque entrée du buffer d'invocation
-- (story 2.1) : stocké au moment de la création, il est re-propagé pendant le replay des entrées
-- non terminées pour que la saga rejoue dans le même contexte de corrélation.
ALTER TABLE invocation_buffer ADD COLUMN x_request_id VARCHAR(36);