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
      decode('bm9uZQ==', 'base64'), -- pool_test_initial_vector: as bytes of simple text: "none"
      decode('dGVzdC10ZXh0MQ==', 'base64'), -- pool_test_encrypted: "test-text1" just as plain text bytes from base64
      
      now(), -- created : SQL 92 spec,
      null, -- createdFrom: not user created
      0
);

-- Migrate unencrypted data to encrypted
-- Auto convert former unencrypted data to created NoneCipher pool entry:
-- see https://www.postgresql.org/docs/current/functions-binarystring.html#FUNCTIONS-BINARYSTRING-CONVERSIONS
update schedule_sechub_job ssj set
   encrypted_configuration = convert_to(unencrypted_configuration, 'UTF8'),
   encrypt_initial_vector = decode('bm9uZQ==', 'base64'), -- initial_vector: as bytes of simple text: "none"
   encrypt_pool_data_id = 0;
   

