-- SPDX-License-Identifier: MIT
CREATE TABLE identity_oauth2_opaquetoken_cache
(
   opaque_token varchar(4096) not null,
   introspection_response text not null,
   created_at timestamp not null,
   duration bigint not null,
   
   expires_at timestamp not null,
   
   version integer,
   
   PRIMARY KEY (opaque_token)
);

CREATE INDEX IF NOT EXISTS i03_identity_opaque_token_cache_id
    ON identity_oauth2_opaquetoken_cache (opaque_token);

