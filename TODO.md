# TODO

Notes issues d'une revue de robustesse sur `API_monstres` (et de la factorisation d'`AuthInterceptor` entre `API_monstres`/`API_joueur`) qui touchent ce service sans y avoir été traitées, en attendant sa propre revue complète prévue séparément.

## 1. ~~Migrer vers gatcha-common-security~~ — Fait (Story 1.2)

`config/AuthInterceptor.java` étend désormais `AbstractAuthInterceptor`/`TokenVerificationClient` de `gatcha-common-security`, comme `API_monstres`/`API_joueur` — plus aucune vérification de token locale ni `AuthApiClient` (supprimé, avec ses DTOs et son test). Le passthrough CORS `OPTIONS` et le fallback cookie `token` restent des surcharges locales de `preHandle()` (décision AD-5 : concerns non partagés par les autres consommateurs), pas des ajouts à la lib. Voir `_bmad-output/implementation-artifacts/1-2-migration-api-invocations-vers-gatcha-common-security.md` pour le détail.

## 2. playerId / ownership sur DELETE monstre

`API_monstres` reçoit désormais un `playerId` réel à la création d'un monstre (transmis par ce service, cf. `InvocationServiceMapper.toCreateMonsterRequest`), ce qui rend son endpoint `GET /api/monsters/getByPlayerId/{playerId}` fonctionnel.

Ce qui reste ouvert : `DELETE /api/monsters/delete/{monsterId}` n'a toujours aucun contrôle de propriétaire (IDOR — n'importe quel joueur authentifié peut supprimer le monstre de n'importe quel autre joueur). Deux difficultés à trancher ici, pas côté `API_monstres` :
- L'identité vérifiée par son `AuthInterceptor` est un `username` (espace `API_authentification`), alors que la propriété d'un monstre se raisonne via `playerId` (espace `API_joueur`) — il faudrait que ce service (ou un autre point du système) fasse cette correspondance.
- La compensation de saga (`MonstersApiClient.deleteMonster`, appelée par `InvocationService` en cas d'échec d'ajout à l'inventaire) rejoue le token du joueur d'origine sur ce delete, pas un credential admin. Une restriction naïve "delete réservé au propriétaire ou à un ADMIN" casserait ce rollback automatique si le joueur n'est pas reconnu comme propriétaire au moment de la compensation (fenêtre entre la création du monstre et son ajout à l'inventaire).

**Décision (audit 2026-08, `docs/AUDIT_API_INVOCATIONS.md` phase 0 item 4) : reporté, pas corrigé dans ce cycle.**
Raisons de ne pas trancher maintenant côté `API_invocations` seul :
- Le contrôle de propriété doit vivre côté `API_monstres` (propriétaire de la ressource `monster`) ou dans un nouveau point de correspondance `username` → `playerId`, ce qui touche un autre submodule — hors périmètre d'une revue limitée à `API_invocations`.
- Une implémentation naïve casserait le rollback de saga décrit ci-dessus (le delete de compensation doit rester possible même quand le joueur n'a pas encore l'ownership enregistré côté `API_joueur`) ; le distinguo "delete utilisateur" vs "delete de compensation interne" nécessite un mécanisme (ex. credential de service dédié à la compensation, ou passage d'un flag interne vérifié côté `API_monstres`) qui n'existe dans aucun des deux services aujourd'hui.
- Traiter ce point correctement nécessite donc une revue conjointe `API_invocations`/`API_monstres`, prévue séparément (zone d'auth déjà partiellement traitée par la migration `gatcha-common-security`, item 1 de ce fichier, mais l'IDOR lui-même reste hors périmètre de cette migration).

Ce report est un choix explicite, pas un oubli : l'IDOR reste un risque connu et documenté (voir aussi le README racine, section Sécurité) jusqu'à cette revue conjointe.
