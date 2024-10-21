-- Entities table (to keep track of our entities registered with Numeracle)
CREATE TABLE nrm_entities (
    id                INT AUTO_INCREMENT PRIMARY KEY, -- Unique identifier for each entity
    entity_code       VARCHAR(50)   UNIQUE NOT NULL,  -- Numeracle entity code
    entity_name       VARCHAR(100)  NOT NULL,         -- Name of the entity
    root_entity_code  VARCHAR(50),                    -- Code of the parent entity (null for Root entities)
    created_at        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP, -- Timestamp of when the entity was created
    updated_at        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP -- Timestamp of last update
);

-- Call Groups table (to manage call groups from calleridrep.com)
CREATE TABLE nrm_call_groups (
    id             INT           AUTO_INCREMENT PRIMARY KEY, -- Unique identifier for each call group
    cidr_group_id  INT           NOT NULL,                   -- CallerId Reputation call group ID
    name           VARCHAR(100)  NOT NULL,                   -- Name of the call group
    description    TEXT,                                     -- Description of the call group
    created_at     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,  -- Timestamp of when the call group was created
    updated_at     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP -- Timestamp of last update
);

-- Phone Numbers table (to keep track of our monitored numbers)
CREATE TABLE nrm_phone_numbers (
    id                    INT AUTO_INCREMENT PRIMARY KEY, -- Unique identifier for each phone number entry
    cidr_id               INT,                            -- CallerId Reputation phone ID
    phone_number          VARCHAR(20)  UNIQUE NOT NULL,   -- The phone number
    description           TEXT,                           -- Description of the phone number
    department            VARCHAR(100),                   -- Department associated with the number
    internal_id           VARCHAR(50),                    -- Internal ID for the number
    cnam                  VARCHAR(100),                   -- Caller ID Name
    call_group_id         INT,                            -- Foreign key to the associated call group
    user_id               INT,                            -- User ID associated with the number in CIDR
    numeracle_profile_id  VARCHAR(50),                    -- Numeracle number profile ID
    archive               BOOLEAN      DEFAULT false,     -- Flag to indicate if the number is archived
    archive_date          TIMESTAMP,                              -- Date when the number was archived
    created_at            TIMESTAMP    DEFAULT CURRENT_TIMESTAMP, -- Timestamp of when the number was added
    updated_at            TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Timestamp of last update

    FOREIGN KEY (call_group_id) REFERENCES nrm_call_groups(id)
);

-- Audit Results table (to store the latest audit results)
CREATE TABLE nrm_audit_results (
    id                       INT AUTO_INCREMENT PRIMARY KEY, -- Unique identifier for each audit result
    phone_number_id          INT NOT NULL, -- Foreign key to the associated phone number
    last_audit_date          TIMESTAMP, -- Date of the last audit
    ftc_flagged              BOOLEAN DEFAULT false, -- Flag indicating if flagged by FTC
    nomorobo_flagged         BOOLEAN DEFAULT false, -- Flag indicating if flagged by Nomorobo
    robokiller_flagged       BOOLEAN DEFAULT false, -- Flag indicating if flagged by Robokiller
    youmail_flagged          BOOLEAN DEFAULT false, -- Flag indicating if flagged by Youmail
    mcc_flagged              BOOLEAN DEFAULT false, -- Flag indicating if flagged by MCC
    ihs_flagged              BOOLEAN DEFAULT false, -- Flag indicating if flagged by IHS
    tnomo_flagged            BOOLEAN DEFAULT false, -- Flag indicating if flagged by TNOMO
    tts_flagged              BOOLEAN DEFAULT false, -- Flag indicating if flagged by TTS
    telo_flagged             BOOLEAN DEFAULT false, -- Flag indicating if flagged by Telo
    carrier_att_flagged      BOOLEAN DEFAULT false, -- Flag indicating if flagged by AT&T
    carrier_verizon_flagged  BOOLEAN DEFAULT false, -- Flag indicating if flagged by Verizon
    carrier_tmobile_flagged  BOOLEAN DEFAULT false, -- Flag indicating if flagged by T-Mobile
    carrier_sprint_flagged   BOOLEAN DEFAULT false, -- Flag indicating if flagged by Sprint
    date_first_flagged       TIMESTAMP, -- Date when first flagged
    date_last_flagged        TIMESTAMP, -- Date when last flagged
    created_at               TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Timestamp of when the audit result was created
    updated_at               TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Timestamp of last update

    FOREIGN KEY (phone_number_id) REFERENCES nrm_phone_numbers(id)
);

-- Remediations table (to track our remediation requests)
CREATE TABLE nrm_remediations (
    id               INT        AUTO_INCREMENT PRIMARY KEY, -- Unique identifier for each remediation entry
    phone_number_id  INT        NOT NULL,                   -- Foreign key to the phone number being remediated
    start_date       TIMESTAMP  DEFAULT CURRENT_TIMESTAMP,  -- Date when remediation was requested
    end_date         TIMESTAMP,                             -- Date when remediation was completed
    status           VARCHAR(50),                           -- Current status of the remediation process
    created_at       TIMESTAMP  DEFAULT CURRENT_TIMESTAMP,  -- Timestamp of when the remediation entry was created
    updated_at       TIMESTAMP  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Timestamp of last update

    FOREIGN KEY (phone_number_id) REFERENCES nrm_phone_numbers(id)
);
