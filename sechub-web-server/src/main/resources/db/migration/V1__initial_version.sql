-- SPDX-License-Identifier: MIT
CREATE TABLE adm_user (
                       user_id varchar(60) not null,
                       user_email_address varchar(255) not null,
                       user_roles VARCHAR(50) NOT NULL,
                       PRIMARY KEY (user_id)
);