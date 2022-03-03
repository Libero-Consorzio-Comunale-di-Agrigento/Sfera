--liquibase formatted sql
--changeset rdestasio:2.4.2.0_20200221_02

-- da qualche cliente sono rimaste queste tabelle, le eliminiamo definitivamente
drop table QRTZ_BLOB_TRIGGERS cascade constraints
/
drop table QRTZ_CALENDARS cascade constraints
/
drop table QRTZ_CRON_TRIGGERS cascade constraints
/
drop table QRTZ_FIRED_TRIGGERS cascade constraints
/
drop table QRTZ_JOB_DETAILS cascade constraints
/
drop table QRTZ_LOCKS cascade constraints
/
drop table QRTZ_PAUSED_TRIGGER_GRPS cascade constraints
/
drop table QRTZ_SCHEDULER_STATE cascade constraints
/
drop table QRTZ_SIMPLE_TRIGGERS cascade constraints
/
drop table QRTZ_SIMPROP_TRIGGERS cascade constraints
/
drop table QRTZ_TRIGGERS cascade constraints
/