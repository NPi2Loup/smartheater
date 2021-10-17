-- ============================================================
--   SGBD      		  :  H2                     
-- ============================================================




-- ============================================================
--   Sequences                                      
-- ============================================================
create sequence SEQ_HEATER
	start with 1000 cache 20; 



create sequence SEQ_ROOM
	start with 1000 cache 20; 


create sequence SEQ_WEEKLY_CALENDAR
	start with 1000 cache 20; 


-- ============================================================
--   Table : HEATER                                        
-- ============================================================
create table HEATER
(
    HEA_ID      	 NUMERIC     	not null,
    NAME        	 VARCHAR(100)	,
    DNS_NAME    	 VARCHAR(100)	,
    ACTIVE      	 BOOL        	,
    AUTO        	 BOOL        	not null,
    AUTO_SWITCH 	 TIMESTAMP   	,
    WCA_ID      	 NUMERIC     	not null,
    PRO_CD      	 VARCHAR(100)	not null,
    MOD_CD      	 VARCHAR(100)	not null,
    constraint PK_HEATER primary key (HEA_ID)
);

comment on column HEATER.HEA_ID is
'Id';

comment on column HEATER.NAME is
'Nom';

comment on column HEATER.DNS_NAME is
'Nom DNS/Adresse IP';

comment on column HEATER.ACTIVE is
'Actif';

comment on column HEATER.AUTO is
'Mode Auto';

comment on column HEATER.AUTO_SWITCH is
'Retour au mode auto';

comment on column HEATER.WCA_ID is
'Calendrier';

comment on column HEATER.PRO_CD is
'Protocol';

comment on column HEATER.MOD_CD is
'Mode';

-- ============================================================
--   Table : HEATER_MODE                                        
-- ============================================================
create table HEATER_MODE
(
    MOD_CD      	 VARCHAR(100)	not null,
    LABEL       	 VARCHAR(100)	,
    constraint PK_HEATER_MODE primary key (MOD_CD)
);

comment on column HEATER_MODE.MOD_CD is
'Id';

comment on column HEATER_MODE.LABEL is
'Nom';

-- ============================================================
--   Table : PROTOCOL                                        
-- ============================================================
create table PROTOCOL
(
    PRO_CD      	 VARCHAR(100)	not null,
    LABEL       	 VARCHAR(100)	,
    constraint PK_PROTOCOL primary key (PRO_CD)
);

comment on column PROTOCOL.PRO_CD is
'Id';

comment on column PROTOCOL.LABEL is
'Nom';

-- ============================================================
--   Table : ROOM                                        
-- ============================================================
create table ROOM
(
    ROO_ID      	 NUMERIC     	not null,
    NAME        	 VARCHAR(100)	,
    constraint PK_ROOM primary key (ROO_ID)
);

comment on column ROOM.ROO_ID is
'Id';

comment on column ROOM.NAME is
'Nom';

-- ============================================================
--   Table : THERMOSTAT                                        
-- ============================================================
create table THERMOSTAT
(
    THE_CD      	 VARCHAR(100)	not null,
    NAME        	 VARCHAR(100)	,
    POWER       	 NUMERIC     	,
    OBSERVATION 	 TEXT        	,
    SIGNAL_RF   	 NUMERIC     	,
    SIGNAL_RF_LABEL	 VARCHAR(100)	,
    BLINK       	 BOOL        	,
    ROO_ID      	 NUMERIC     	not null,
    constraint PK_THERMOSTAT primary key (THE_CD)
);

comment on column THERMOSTAT.THE_CD is
'Code';

comment on column THERMOSTAT.NAME is
'Nom';

comment on column THERMOSTAT.POWER is
'Puissance';

comment on column THERMOSTAT.OBSERVATION is
'Observation';

comment on column THERMOSTAT.SIGNAL_RF is
'Signal RF';

comment on column THERMOSTAT.SIGNAL_RF_LABEL is
'Signal RF';

comment on column THERMOSTAT.BLINK is
'Clignote';

comment on column THERMOSTAT.ROO_ID is
'Piece';

-- ============================================================
--   Table : WEEKLY_CALENDAR                                        
-- ============================================================
create table WEEKLY_CALENDAR
(
    WCA_ID      	 NUMERIC     	not null,
    NAME        	 VARCHAR(100)	,
    JSON_VALUE  	 TEXT        	,
    constraint PK_WEEKLY_CALENDAR primary key (WCA_ID)
);

comment on column WEEKLY_CALENDAR.WCA_ID is
'Id';

comment on column WEEKLY_CALENDAR.NAME is
'Nom';

comment on column WEEKLY_CALENDAR.JSON_VALUE is
'Value as json';


alter table HEATER
	add constraint FK_A_HEA_MOD_HEATER_MODE foreign key (MOD_CD)
	references HEATER_MODE (MOD_CD);

create index A_HEA_MOD_HEATER_MODE_FK on HEATER (MOD_CD asc);

alter table HEATER
	add constraint FK_A_HEA_PRO_PROTOCOL foreign key (PRO_CD)
	references PROTOCOL (PRO_CD);

create index A_HEA_PRO_PROTOCOL_FK on HEATER (PRO_CD asc);

alter table HEATER
	add constraint FK_A_HEA_WCA_WEEKLY_CALENDAR foreign key (WCA_ID)
	references WEEKLY_CALENDAR (WCA_ID);

create index A_HEA_WCA_WEEKLY_CALENDAR_FK on HEATER (WCA_ID asc);

alter table THERMOSTAT
	add constraint FK_A_ROO_THE_ROOM foreign key (ROO_ID)
	references ROOM (ROO_ID);

create index A_ROO_THE_ROOM_FK on THERMOSTAT (ROO_ID asc);


