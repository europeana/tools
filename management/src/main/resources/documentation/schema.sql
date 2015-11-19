

    create table LOG_ENTRIES (
        ID  bigserial not null,
        ACTION varchar(255),
        MESSAGE varchar(255),
        TIME_STAMP timestamp,
        USER_ID int8,
        primary key (ID)
    );

    create table SESSIONS (
        ID  bigserial not null,
        LAST_LOGIN timestamp,
        USER_ID int8,
        primary key (ID)
    );

    create table SYSTEM_OBJS (
        STYPE varchar(31) not null,
        ID  bigserial not null,
        PASSWORD varchar(255),
        PROFILE_TYPE varchar(255),
        URLS varchar(255),
        USERNAME varchar(255),
        SOLRCORE varchar(255),
        ZOOKEEPERURL varchar(255),
        MONGODBNAME varchar(255),
        primary key (ID)
    );

    create table USERS (
        ID  bigserial not null,
        ACTIVE boolean,
        NAME varchar(255),
        PASSWORD varchar(255),
        ROLE varchar(255),
        SURNAME varchar(255),
        USERNAME varchar(255),
        primary key (ID)
    );

    alter table SYSTEM_OBJS 
        add constraint UK_2ixolmn5hjkshct81s7bv2ors  unique (STYPE, URLS);

    alter table USERS 
        add constraint UK_h6k33r31i2nvrri9lok4r163j  unique (USERNAME);

    alter table LOG_ENTRIES 
        add constraint FK_dunk24i147x0gw7jhli8ggn5 
        foreign key (USER_ID) 
        references USERS;

    alter table SESSIONS 
        add constraint FK_1htl55ww79ia1752ki18xebnk 
        foreign key (USER_ID) 
        references USERS;
        
        
    INSERT INTO USERS(ACTIVE, NAME, PASSWORD, ROLE, SURNAME, USERNAME)
    VALUES (true, 'ymamakis', '0158212af0ba0cb770e71dacf4ef2d80', 'GOD', 'mamakis', 'ymamakis');
    
    INSERT INTO USERS(ACTIVE, NAME, PASSWORD, ROLE, SURNAME, USERNAME)
    VALUES (true, 'Haris', '41532fe0a5e95277d1b1a63fb3544751', 'GOD', 'Georgiadis', 'hgeorgiadis');

    

