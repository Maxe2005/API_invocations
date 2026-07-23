# TODO

Notes issues d'une revue de robustesse sur `API_monstres` (et de la factorisation d'`AuthInterceptor` entre `API_monstres`/`API_joueur`) qui touchent ce service sans y avoir été traitées, en attendant sa propre revue complète prévue séparément.

## 1. Migrer vers gatcha-common-security

`API_monstres` et `API_joueur` ont factorisé leur `AuthInterceptor` dans une nouvelle lib partagée (`gatcha-common-security`, nouveau submodule à la racine du repo `GatchaApi`) : `AbstractAuthInterceptor` + `TokenVerificationClient` reproduisent l'appel `POST /user/verify-token` et sa gestion d'erreurs (4xx/5xx → 401, injoignable → 500), avec un point d'extension `postAuthorize()` pour la politique d'autorisation propre à chaque service.

L'auth de ce service (`config/AuthInterceptor.java`) n'a **pas** été migrée, pour ne pas anticiper sur la revue complète à venir. Elle diverge structurellement de ce que la lib fournit aujourd'hui :
- passe par `AuthApiClient`/`ExternalApiClient` (abstraction dédiée aux appels externes de ce service) plutôt qu'un appel RestTemplate direct ;
- laisse passer les requêtes `OPTIONS` (CORS preflight) avant toute vérification — la lib ne le fait pas ;
- ignore `username`/`role` de la réponse d'`API_authentification` (ce service rejoue le token du joueur d'origine vers les services downstream plutôt que d'agir en son propre nom, donc il n'a pas besoin de les capturer) ;
- accepte un fallback cookie (`token`) en plus du header `Authorization`, que `AbstractAuthInterceptor` ne fournit pas (choix délibéré : ne pas changer le comportement d'extraction du token de chaque service lors de la factorisation initiale — voir le commit d'introduction de la lib).

À trancher pendant la revue : soit adapter `AbstractAuthInterceptor` pour couvrir ces cas (hook d'extraction du token, gestion optionnelle d'OPTIONS), soit garder l'implémentation actuelle si elle est jugée suffisamment différente pour ne pas valoir la factorisation.

## 2. playerId / ownership sur DELETE monstre

`API_monstres` reçoit désormais un `playerId` réel à la création d'un monstre (transmis par ce service, cf. `InvocationServiceMapper.toCreateMonsterRequest`), ce qui rend son endpoint `GET /api/monsters/getByPlayerId/{playerId}` fonctionnel.

Ce qui reste ouvert : `DELETE /api/monsters/delete/{monsterId}` n'a toujours aucun contrôle de propriétaire (IDOR — n'importe quel joueur authentifié peut supprimer le monstre de n'importe quel autre joueur). Deux difficultés à trancher ici, pas côté `API_monstres` :
- L'identité vérifiée par son `AuthInterceptor` est un `username` (espace `API_authentification`), alors que la propriété d'un monstre se raisonne via `playerId` (espace `API_joueur`) — il faudrait que ce service (ou un autre point du système) fasse cette correspondance.
- La compensation de saga (`MonstersApiClient.deleteMonster`, appelée par `InvocationService` en cas d'échec d'ajout à l'inventaire) rejoue le token du joueur d'origine sur ce delete, pas un credential admin. Une restriction naïve "delete réservé au propriétaire ou à un ADMIN" casserait ce rollback automatique si le joueur n'est pas reconnu comme propriétaire au moment de la compensation (fenêtre entre la création du monstre et son ajout à l'inventaire).
