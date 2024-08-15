-- SPDX-License-Identifier: MIT
INSERT INTO schedule_cipher_pool_data (
    pool_id, 
    
    pool_algorithm, 
    pool_pwd_src_type, 
    pool_pwd_src_data,
    
    pool_test_text,
    pool_test_initial_vector,
    pool_test_encrypted,
    
    pool_creation_timestamp,
    pool_created_from,
    version
    )
VALUES( 
     0, -- pool_id
     
     'NONE', -- pool_algorithm: CipherAlgorithm:NONE
     'NONE', -- pool_pwd_src_type: CipherPasswordSourceType:NONE 
      null, -- pool_pwd_src_data
                  
      'test-text1', --pool_test_text: plain text
      X'6E6F6E65', -- pool_test_initial_vector: as bytes of simple text: "none"
      X'746573742d7465787431', --decode('dGVzdC10ZXh0MQ==', 'base64'), -- pool_test_encrypted: "test-text1" just as plain text bytes from base64
      
      now(), -- created : SQL 92 spec,
      null, -- createdFrom: not user created
      0
);

-- In postgres we do a migration
-- But here, for h2, we do not migrate old data, because
-- h2 is only for testing!